package org.lasend.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.lasend.api.LasendCallbacks;
import org.lasend.api.constant.EncryptionConstants;
import org.lasend.api.constant.NetworkConstants;
import org.lasend.api.dto.*;
import org.lasend.api.dto.encrypted.DecryptedPayload;
import org.lasend.api.dto.encrypted.EncryptedDto;
import org.lasend.api.dto.encrypted.EncryptedDtoBase;
import org.lasend.api.dto.encrypted.FileRequestDto;
import org.lasend.api.dto.encrypted.message.FileLinkMessageDto;
import org.lasend.api.dto.encrypted.message.TextMessageDto;
import org.lasend.api.dto.response.*;
import org.lasend.api.exception.DeviceAlreadyInContactsException;
import org.lasend.api.exception.InvalidChatIdException;
import org.lasend.api.model.*;
import org.lasend.api.model.chat.FileLinkMessage;
import org.lasend.api.model.chat.Message;
import org.lasend.api.model.chat.TextMessage;
import org.lasend.api.network.DeviceInfoGetter;
import org.lasend.api.network.Receiver;
import org.lasend.api.network.impl.socket.AdvertisementUdpSocketSender;
import org.lasend.api.network.impl.socket.DeviceInfoSocketGetter;
import org.lasend.api.state.LasendStore;
import org.lasend.api.util.EncryptionUtil;
import org.lasend.api.util.ExceptionCallback;
import org.lasend.api.util.LasendStoreUtil;
import org.lasend.api.util.MiscUtil;
import org.whispersystems.libsignal.*;
import org.whispersystems.libsignal.fingerprint.FingerprintGenerator;
import org.whispersystems.libsignal.fingerprint.NumericFingerprintGenerator;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.UUID;

public class ReceivedDataProcessor extends DataProcessor {
    private static FingerprintGenerator fingerprintGenerator = new NumericFingerprintGenerator(EncryptionConstants.FINGERPRINT_ITERATIONS);

    private ReceivedDataProcessor() {
    }

    public static ResponseData processData(DataDto dto, InetAddress remoteAddress, LasendStore store, Receiver.ReceiverCallback receiverCallback, ExceptionCallback exceptionCallback) {
        Device remoteDevice = getDeviceFromMessage(dto, remoteAddress, store);
        ResponseData response = null;

        if (dto instanceof EncryptedDto) {
            EncryptedDtoBase encryptedDto = (EncryptedDtoBase) dto;
            response = processEncryptedDto(encryptedDto, remoteDevice, store, receiverCallback, exceptionCallback);
        } else if (dto instanceof DeviceInfoRequestDto) {
            DeviceInfoRequestDto deviceInfoRequestDto = (DeviceInfoRequestDto) dto;
            response = processDeviceInfoRequestData(deviceInfoRequestDto, remoteDevice, store);
        } else if (dto instanceof ContactRequestDto) {
            ContactRequestDto contactRequestDto = (ContactRequestDto) dto;
            response = processContactRequestData(contactRequestDto, remoteDevice, store, receiverCallback, exceptionCallback);
        } else if (dto instanceof ContactAcceptDto) {
            ContactAcceptDto contactAcceptDto = (ContactAcceptDto) dto;
            response = processContactAcceptData(contactAcceptDto, remoteDevice, store, receiverCallback, exceptionCallback);
        } else if (dto instanceof ContactRejectDto) {
            ContactRejectDto contactRejectDto = (ContactRejectDto) dto;
            response = processContactRejectData(contactRejectDto, remoteDevice, store, receiverCallback, exceptionCallback);
        } else {
            return new FailureResponse();
        }
        return response;
    }

    public static ResponseData processEncryptedDto(EncryptedDtoBase dto, Device remoteDevice, LasendStore store, Receiver.ReceiverCallback receiverCallback, ExceptionCallback exceptionCallback) {
        try {
            SessionCipher sessionCipher = new SessionCipher(store.getPersistent().getMeStore().getMe().getSignalProtocolStore(), remoteDevice.getSignalProtocolAddress());

            DecryptedPayload payload = EncryptionUtil.decryptPayload(dto.getPayload(), sessionCipher);

            if (payload instanceof TextMessageDto.TextDecryptedPayload) {
                TextMessageDto.TextDecryptedPayload specificPayload = (TextMessageDto.TextDecryptedPayload) payload;
                String chatId = specificPayload.getChatId();
                Chat chat = store.getPersistent().getChatStore().getById(chatId);
                if (chat == null) {
                    throw new InvalidChatIdException(chatId);
                }
                Message message = new TextMessage(chatId, (TextMessageDto) dto, specificPayload);
                chat.appendMessage(message);
                receiverCallback.onChatMessageReceived(message, chat);
                return new SuccessResponse();
            } else if (payload instanceof FileLinkMessageDto.FileLinkDecryptedPayload) {
                FileLinkMessageDto.FileLinkDecryptedPayload specificPayload = (FileLinkMessageDto.FileLinkDecryptedPayload) payload;
                String chatId = specificPayload.getChatId();
                Chat chat = store.getPersistent().getChatStore().getById(chatId);
                if (chat == null) {
                    throw new InvalidChatIdException(chatId);
                }
                FileLinkMessage message = new FileLinkMessage(chatId, (FileLinkMessageDto) dto, specificPayload);
                chat.appendMessage(message);
                receiverCallback.onChatMessageReceived(message, chat);
                return new SuccessResponse();
            } else if (payload instanceof FileRequestDto.FileRequestDecryptedPayload) {
                FileRequestDto.FileRequestDecryptedPayload specificPayload = (FileRequestDto.FileRequestDecryptedPayload) payload;
                String fileId = specificPayload.getFileId();
                return new FileResponse(fileId);
            }
        } catch (LegacyMessageException | InvalidChatIdException | InvalidMessageException | JsonProcessingException e) {
            exceptionCallback.onMessageSendException(e);
            return new FailureResponse();
        }
        return new FailureResponse();
    }

    public static void processAdvertisementData(String receivedString, InetAddress remoteAddress, LasendStore store, LasendCallbacks callbacks) {
        if (MiscUtil.isOwnAddress(remoteAddress)) {
            // Don't add own device
            return;
        }

        if (receivedString.equals(NetworkConstants.ADVERTISEMENT_MULTICAST_MESSAGE)) {
            try {
                AdvertisementUdpSocketSender.sendUnicast(remoteAddress);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (receivedString.equals(NetworkConstants.ADVERTISEMENT_UNICAST_MESSAGE)) {
            DeviceInfoGetter deviceInfoGetter = new DeviceInfoSocketGetter(remoteAddress, store, callbacks);
            try {
                Device device = deviceInfoGetter.getDeviceInfo();
                store.getTemp().getFoundRemoteDevicesStore().update(device);
                callbacks.getDeviceDiscoveredCallback().onDeviceDiscovered(device);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static ResponseData processDeviceInfoRequestData(DeviceInfoRequestDto deviceInfoRequestDto, Device remoteDevice, LasendStore store) {
        Me me = store.getPersistent().getMeStore().getMe();
        return new DeviceInfoResponse(me.getName(), me.getIdentity());
    }

    public static ResponseData processContactRequestData(ContactRequestDto dto, Device remoteDevice, LasendStore store, Receiver.ReceiverCallback receiverCallback, ExceptionCallback exceptionCallback) {
        try {
            if (LasendStoreUtil.getContactByDevice(remoteDevice, store) != null) {
                throw new DeviceAlreadyInContactsException(remoteDevice);
            }

            remoteDevice.setName(dto.getSenderName());

            ContactRequestReceived contactRequest = new ContactRequestReceived(UUID.randomUUID().toString(), remoteDevice.getIdentity(), dto.retrievePreKeyBundle());
            String comparableFingerprint = EncryptionUtil.getComparableFingerprint(remoteDevice, store);
            receiverCallback.onContactRequestReceived(contactRequest, remoteDevice, comparableFingerprint);

            return new SuccessResponse();
        } catch (DeviceAlreadyInContactsException | InvalidKeyException e) {
            exceptionCallback.onReceiveContactRequestException(e, remoteDevice);
            return new FailureResponse();
        }
    }

    private static ResponseData processContactAcceptData(ContactAcceptDto dto, Device remoteDevice, LasendStore store, Receiver.ReceiverCallback receiverCallback, ExceptionCallback exceptionCallback) {
        try {
            SessionCipher sessionCipher = new SessionCipher(store.getPersistent().getMeStore().getMe().getSignalProtocolStore(), remoteDevice.getSignalProtocolAddress());
            ContactAcceptDto.ContactAcceptDecryptedPayload payload = EncryptionUtil.decryptPreKeyPayload(dto.getPayload(), sessionCipher);

            Contact contact = LasendStoreUtil.getContactByDevice(remoteDevice, store);
            if (contact != null) {
                throw new DeviceAlreadyInContactsException(remoteDevice);
            } else {
                contact = new Contact(UUID.randomUUID().toString(), remoteDevice.getName(), remoteDevice.getIdentityKey());
            }

            String newChatId = payload.getChatId();
            Chat newChat = new Chat(newChatId, remoteDevice.getName(), new ArrayList<Message>());
            contact.setChatId(newChatId);
            store.getPersistent().getChatStore().update(newChat);
            store.getPersistent().getContactStore().update(contact);

            ContactOnline contactOnline = new ContactOnline(contact, remoteDevice);
            store.getTemp().getFoundOnlineContactsStore().update(contactOnline);

            receiverCallback.onContactAcceptReceived(dto, payload, remoteDevice);
            return new SuccessResponse();
        } catch (InvalidKeyIdException | InvalidMessageException | LegacyMessageException | DuplicateMessageException | UntrustedIdentityException | JsonProcessingException | InvalidVersionException | InvalidKeyException | DeviceAlreadyInContactsException e) {
            store.getPersistent().getMeStore().getMe().getSignalProtocolStore().deleteSession(remoteDevice.getSignalProtocolAddress());
            exceptionCallback.onReceiveContactAcceptException(e, remoteDevice);
            return new FailureResponse();
        }
    }

    public static ResponseData processContactRejectData(ContactRejectDto dto, Device remoteDevice, LasendStore store, Receiver.ReceiverCallback receiverCallback, ExceptionCallback exceptionCallback) {
        store.getPersistent().getMeStore().getMe().getSignalProtocolStore().deleteSession(remoteDevice.getSignalProtocolAddress());
        receiverCallback.onContactRejectReceived(dto, remoteDevice);
        return new SuccessResponse();
    }
}
