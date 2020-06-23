package org.lasend.api;

import lombok.Getter;
import org.lasend.api.dto.ContactRequestDto;
import org.lasend.api.exception.*;
import org.lasend.api.model.*;
import org.lasend.api.model.chat.FileLinkMessage;
import org.lasend.api.model.chat.TextMessage;
import org.lasend.api.network.*;
import org.lasend.api.network.impl.socket.*;
import org.lasend.api.state.LasendStore;
import org.lasend.api.state.LasendTempStore;
import org.lasend.api.util.EncryptionUtil;
import org.lasend.api.util.LasendStoreUtil;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.UntrustedIdentityException;
import org.whispersystems.libsignal.state.PreKeyBundle;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

public class LasendApi {
    @Getter
    private final LasendStore store;
    @Getter
    private final LasendCallbacks callbacks;
    private AdvertisementReceiver advertisementReceiver;
    @Getter
    private InetAddress listenAddress;
    @Getter
    private boolean isInitializing;
    @Getter
    private boolean isInitialized;
    private DataReceiver dataReceiver;
    private Enumeration<NetworkInterface> networkInterfaces;

    public LasendApi(LasendPersistentStore permanentStore, LasendCallbacks callbacks) {
        try {
            this.listenAddress = InetAddress.getByName("0.0.0.0");
        } catch (UnknownHostException e) {
            // It couldn't be wrong
        }
        this.store = new LasendStore(permanentStore, new LasendTempStore());
        this.callbacks = callbacks;
    }

    public LasendApi(InetAddress listenAddress, LasendPersistentStore permanentStore, LasendCallbacks callbacks) {
        this.listenAddress = listenAddress;
        this.store = new LasendStore(permanentStore, new LasendTempStore());
        this.callbacks = callbacks;
    }

    public void init() throws IOException {
        isInitializing = true;
        dataReceiver = new DataTcpSocketReceiver(listenAddress, store, callbacks);
        dataReceiver.start();

        advertisementReceiver = new AdvertisementMulticastSocketReceiver(store, callbacks);
        advertisementReceiver.start();

        isInitializing = false;
        isInitialized = true;
    }

    public void discoverDevicesOnLan() throws IOException {
        store.getTemp().getFoundRemoteDevicesStore().deleteAll();

        AdvertisementUdpSocketSender.sendMulticast();
    }

    public Device sendDeviceInfoRequest(InetAddress remoteAddress) throws ApiNotInitializedException, IOException {
        if (!isInitialized) {
            throw new ApiNotInitializedException();
        }

        DeviceInfoGetter deviceInfoGetter = new DeviceInfoSocketGetter(remoteAddress, store, callbacks);
        Device device = deviceInfoGetter.getDeviceInfo();
        return device;
    }

    public ContactRequestSent sendContactRequest(Device remoteDevice) throws ApiNotInitializedException, InvalidResponseException, IOException, DeviceAlreadyInContactsException {
        if (!isInitialized) {
            throw new ApiNotInitializedException();
        }

        // If device already in contacts
        if (LasendStoreUtil.getContactByDevice(remoteDevice, store) != null) {
            throw new DeviceAlreadyInContactsException(remoteDevice);
        }

        Me me = store.getPersistent().getMeStore().getMe();
        PreKeyBundle preKeyBundle = me.getPreKeyBundle();
        String contactRequestUuid = UUID.randomUUID().toString();
        ContactRequestSent contactRequest = new ContactRequestSent(contactRequestUuid, remoteDevice.getIdentity(), preKeyBundle);
        ContactRequestDto contactRequestDto = new ContactRequestDto(contactRequest, me.getIdentity(), me.getName());

        ContactRequestSender contactRequestSender = new ContactRequestSocketSender(remoteDevice, store, callbacks);
        contactRequestSender.sendContactRequest(contactRequestDto);

        String comparableFingerprint = getComparableFingerprint(remoteDevice);

        return contactRequest;
    }

    public String sendContactAccept(ContactRequestReceived contactRequest) throws ApiNotInitializedException, InvalidResponseException, IOException, UnknownInviteAcceptException, UntrustedIdentityException, DeviceIsNotOnlineException, DeviceAlreadyInContactsException, InvalidKeyException {
        if (!isInitialized) {
            throw new ApiNotInitializedException();
        }

        Device senderDevice = LasendStoreUtil.getDeviceByFingerprint(contactRequest.getSenderIdentity(), store);

        if (senderDevice == null) {
            throw new DeviceIsNotOnlineException(contactRequest.getSenderIdentity());
        }

        // If device already in contacts
        if (LasendStoreUtil.getContactByDevice(senderDevice, store) != null) {
            throw new DeviceAlreadyInContactsException(senderDevice);
        }

        ContactRequestAcceptSender contactRequestAcceptSender = new ContactAcceptSocketSender(senderDevice, store, callbacks);
        String newChatId = contactRequestAcceptSender.sendContactRequestAccept(contactRequest);
        return newChatId;
    }

    public void sendContactReject(ContactRequestReceived contactRequest) throws ApiNotInitializedException, InvalidResponseException, IOException, UnknownInviteAcceptException, UntrustedIdentityException, DeviceIsNotOnlineException {
        if (!isInitialized) {
            throw new ApiNotInitializedException();
        }

        Device senderDevice = LasendStoreUtil.getDeviceByFingerprint(contactRequest.getSenderIdentity(), store);

        if (senderDevice == null) {
            throw new DeviceIsNotOnlineException(contactRequest.getSenderIdentity());
        }

        // If senderDevice already in contacts
        if (LasendStoreUtil.getContactByDevice(senderDevice, store) != null) {
            return;
        }

        ContactRequestRejectSender contactRequestRejectSender = new ContactRejectSocketSender(senderDevice, store, callbacks);
        contactRequestRejectSender.sendContactRequestReject(contactRequest);
    }

    public List<Chat> getAllChats() {
        return store.getPersistent().getChatStore().getAll();
    }

    public TextMessage sendTextMessage(String text, Chat chat) throws ChatWithNoContactsException, ChatHasNoOnlineUsersException, IOException, UntrustedIdentityException, ApiNotInitializedException {
        if (!isInitialized) {
            throw new ApiNotInitializedException();
        }

        Contact contact = LasendStoreUtil.getContactByChat(chat, store);
        if (contact == null) {
            throw new ChatWithNoContactsException();
        }

        ContactOnline contactOnline = store.getTemp().getFoundOnlineContactsStore().getById(contact.getIdentityKey());
        if (contactOnline == null) {
            throw new ChatHasNoOnlineUsersException();
        }

        if (contactOnline.getMessageSender() == null) {
            MessageSender messageSender = new MessageSocketSender(contactOnline, store, callbacks);
            contactOnline.setMessageSender(messageSender);
        }

        TextMessage message = contactOnline.getMessageSender().sendTextMessage(text, chat.getId());
        return message;
    }

    public FileLinkMessage sendFileMessage(File file, Chat chat) throws IOException, ChatHasNoOnlineUsersException, ChatWithNoContactsException, UntrustedIdentityException, ApiNotInitializedException {
        if (!isInitialized) {
            throw new ApiNotInitializedException();
        }

        Contact contact = LasendStoreUtil.getContactByChat(chat, store);
        if (contact == null) {
            throw new ChatWithNoContactsException();
        }

        ContactOnline contactOnline = store.getTemp().getFoundOnlineContactsStore().getById(contact.getIdentityKey());
        if (contactOnline == null) {
            throw new ChatHasNoOnlineUsersException();
        }

        if (contactOnline.getMessageSender() == null) {
            MessageSender messageSender = new MessageSocketSender(contactOnline, store, callbacks);
            contactOnline.setMessageSender(messageSender);
        }

        SharedFile sharedFile = new SharedFile(file);
        store.getPersistent().getSharedFileStore().update(sharedFile);

        FileLinkMessage message = contactOnline.getMessageSender().sendFileLinkMessage(sharedFile, chat.getId());
        return message;
    }

    public void receiveFile(FileLinkMessage fileLink, File file) throws ApiNotInitializedException {
        if (!isInitialized) {
            throw new ApiNotInitializedException();
        }

        try {
            Chat chat = store.getPersistent().getChatStore().getById(fileLink.getChatId());

            Contact contact = LasendStoreUtil.getContactByChat(chat, store);
            if (contact == null) {
                throw new ChatWithNoContactsException();
            }

            ContactOnline contactOnline = store.getTemp().getFoundOnlineContactsStore().getById(contact.getIdentityKey());
            if (contactOnline == null) {
                throw new ChatHasNoOnlineUsersException();
            }

            if (contactOnline.getMessageSender() == null) {
                MessageSender messageSender = new MessageSocketSender(contactOnline, store, callbacks);
                contactOnline.setMessageSender(messageSender);
            }

            FileRequestSender fileRequestSender = new FileRequestSocketSender(contactOnline.getDevice(), store, callbacks);
            fileRequestSender.receiveFile(fileLink, file);
        } catch (Exception e) {
            callbacks.getExceptionCallback().onReceiveFileException(e);
        }
    }

    public String getComparableFingerprint(Device remoteDevice) {
        return EncryptionUtil.getComparableFingerprint(remoteDevice, store);
    }

    public void stop() {
        if (isInitialized) {
            dataReceiver.stop();
            isInitialized = false;
        }
    }
}
