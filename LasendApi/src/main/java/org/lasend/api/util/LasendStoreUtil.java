package org.lasend.api.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.lasend.api.model.Chat;
import org.lasend.api.model.Contact;
import org.lasend.api.model.Device;
import org.lasend.api.state.LasendStore;
import org.lasend.api.state.StateDao;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class LasendStoreUtil {

    private LasendStoreUtil() {
    }

    public static Contact getContactByDevice(Device device, LasendStore store) {
        for (Contact contact : store.getPersistent().getContactStore().getAll()) {
            if (contact.getIdentityKey().equals(device.getIdentity())) {
                return contact;
            }
        }

        return null;
    }

    public static Contact getContactByChat(Chat chat, LasendStore store) {
        for (Contact contact : store.getPersistent().getContactStore().getAll()) {
            if (contact.getChatId().equals(chat.getId())) {
                return contact;
            }
        }

        return null;
    }

    public static Device getDeviceByContact(Contact contact, LasendStore store) {
        return getDeviceByFingerprint(contact.getIdentityKey(), store);
    }

    public static Device getDeviceByFingerprint(String identityFingerprint, LasendStore store) {
        for (Device device : store.getTemp().getFoundRemoteDevicesStore().getAll()) {
            if (device.getIdentity().equals(identityFingerprint)) {
                return device;
            }
        }
        return null;
    }

    public static Chat getChatWithContact(Contact contact, LasendStore store) {
        StateDao<Chat> chatStore = store.getPersistent().getChatStore();
        return chatStore.getById(contact.getChatId());
    }

    public static String getFileHash(File file) throws IOException {
        try (InputStream is = new FileInputStream(file)) {
            return DigestUtils.sha512Hex(is);
        }
    }

//    public static Device getDeviceFromInetAddress(InetAddress address, LasendStore store) {
//        List<Device>
//    }
}
