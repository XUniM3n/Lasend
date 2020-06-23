package org.lasend.api.service;

import org.lasend.api.dto.DataDto;
import org.lasend.api.model.Contact;
import org.lasend.api.model.ContactOnline;
import org.lasend.api.model.Device;
import org.lasend.api.state.LasendStore;
import org.lasend.api.util.LasendStoreUtil;

import java.net.InetAddress;

public abstract class DataProcessor {
    protected DataProcessor() {
    }

    public static Device getDeviceFromMessage(DataDto dataDto, InetAddress remoteAddress, LasendStore store) {
        Device device = store.getTemp().getFoundRemoteDevicesStore().getById(dataDto.getSenderIdentity());
        if (store.getTemp().getFoundRemoteDevicesStore().contains(dataDto.getSenderIdentity())) {
            return device;
        }

        device = Device.builder()
                .identity(dataDto.getSenderIdentity())
                .address(remoteAddress)
                .build();

        store.getTemp().getFoundRemoteDevicesStore().update(device);

        Contact contact = LasendStoreUtil.getContactByDevice(device, store);
        if (contact != null) {
            ContactOnline contactOnline = new ContactOnline(contact, device);
            store.getTemp().getFoundOnlineContactsStore().update(contactOnline);
        }

        return device;
    }
}
