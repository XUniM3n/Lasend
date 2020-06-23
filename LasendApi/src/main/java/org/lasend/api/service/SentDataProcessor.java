package org.lasend.api.service;

import org.lasend.api.dto.ContactAcceptDto;
import org.lasend.api.dto.ContactRejectDto;
import org.lasend.api.dto.ContactRequestDto;
import org.lasend.api.dto.DeviceInfoRequestDto;
import org.lasend.api.dto.encrypted.FileRequestDto;
import org.lasend.api.dto.encrypted.message.FileLinkMessageDto;
import org.lasend.api.dto.encrypted.message.TextMessageDto;
import org.lasend.api.dto.response.DeviceInfoResponse;
import org.lasend.api.exception.DeviceAlreadyInContactsException;
import org.lasend.api.exception.UnknownInviteAcceptException;
import org.lasend.api.model.*;
import org.lasend.api.model.chat.FileLinkMessage;
import org.lasend.api.model.chat.Message;
import org.lasend.api.model.chat.TextMessage;
import org.lasend.api.network.Sender;
import org.lasend.api.state.LasendStore;
import org.lasend.api.state.StateDao;
import org.lasend.api.util.ExceptionCallback;
import org.lasend.api.util.LasendStoreUtil;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.UntrustedIdentityException;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.UUID;

public class SentDataProcessor extends DataProcessor {
    private SentDataProcessor() {
    }

    public static Device processDeviceInfoRequestData(DeviceInfoRequestDto dto, DeviceInfoResponse response, InetAddress remoteAddress, LasendStore store, Sender.SenderCallback senderCallback, ExceptionCallback exceptionCallback) {
        Device device = getDeviceFromMessage(response, remoteAddress, store);
        device.setName(response.getName());
        senderCallback.onDeviceInfo(device);
        return device;
    }

    public static void processContactRequestData(ContactRequestDto dto, Device remoteDevice, LasendStore store, Sender.SenderCallback senderCallback, ExceptionCallback exceptionCallback) throws DeviceAlreadyInContactsException {
        if (LasendStoreUtil.getContactByDevice(remoteDevice, store) != null) {
            throw new DeviceAlreadyInContactsException(remoteDevice);
        }

        senderCallback.onContactRequestSent(dto);
    }

    public static String processContactAcceptData(ContactRequestReceived invite, ContactAcceptDto dto, ContactAcceptDto.ContactAcceptDecryptedPayload payload, Device remoteDevice, LasendStore store, Sender.SenderCallback senderCallback, ExceptionCallback exceptionCallback) throws UnknownInviteAcceptException, UntrustedIdentityException, InvalidKeyException, DeviceAlreadyInContactsException {
        Contact contact = LasendStoreUtil.getContactByDevice(remoteDevice, store);
        if (contact != null) {
            throw new DeviceAlreadyInContactsException(remoteDevice);
        } else {
            contact = new Contact(UUID.randomUUID().toString(), remoteDevice.getName(), remoteDevice.getIdentityKey());
        }

        StateDao<Chat> chatStore = store.getPersistent().getChatStore();
        StateDao<Contact> contactStore = store.getPersistent().getContactStore();

        String newChatId = payload.getChatId();
        contact.setChatId(newChatId);
        Chat newChat = new Chat(newChatId, remoteDevice.getName(), new ArrayList<Message>());
        chatStore.update(newChat);
        contactStore.update(contact);

        ContactOnline contactOnline = new ContactOnline(contact, remoteDevice);
        store.getTemp().getFoundOnlineContactsStore().update(contactOnline);

        //store.getPersistent().getMeStore().getMe().getSignalProtocolStore().loadSession(remoteDevice.getSignalProtocolAddress()).getSessionState().clearUnacknowledgedPreKeyMessage();

        senderCallback.onContactAcceptSent(dto, payload);

        return newChatId;
    }

    public static void processContactRejectData(ContactRequestReceived invite, ContactRejectDto dto, Device remoteDevice, LasendStore store, Sender.SenderCallback senderCallback, ExceptionCallback exceptionCallback) {

        senderCallback.onContactRejectSent(dto);
    }

    public static void processFileRequest(FileRequestDto dto, FileRequestDto.FileRequestDecryptedPayload payload, Device remoteDevice, LasendStore store, Sender.SenderCallback senderCallback, ExceptionCallback exceptionCallback) {
        senderCallback.onFileRequestSent(dto, payload);
    }

    public static TextMessage processTextMessage(TextMessageDto dto, TextMessageDto.TextDecryptedPayload payload, Device remoteDevice, LasendStore store, Sender.SenderCallback senderCallback, ExceptionCallback exceptionCallback) {
        String chatId = payload.getChatId();
        Chat chat = store.getPersistent().getChatStore().getById(chatId);
        TextMessage message = new TextMessage(payload.getText(), chatId, payload.getMessageId(), dto.getSenderIdentity());
        chat.appendMessage(message);

        senderCallback.onChatMessageSent(dto, payload);
        return message;
    }

    public static FileLinkMessage processFileLinkMessage(FileLinkMessageDto dto, FileLinkMessageDto.FileLinkDecryptedPayload payload, Device remoteDevice, LasendStore store, Sender.SenderCallback senderCallback, ExceptionCallback exceptionCallback) {
        String chatId = payload.getChatId();
        Chat chat = store.getPersistent().getChatStore().getById(chatId);
        FileLinkMessage message = new FileLinkMessage(chatId, dto, payload);
        chat.appendMessage(message);

        senderCallback.onChatMessageSent(dto, payload);
        return message;
    }
}
