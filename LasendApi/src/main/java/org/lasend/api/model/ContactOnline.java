package org.lasend.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lasend.api.network.MessageSender;

@AllArgsConstructor
@NoArgsConstructor
public class ContactOnline implements GetIdAble {
    @Getter
    private Contact contact;
    @Getter
    private Device device;
    @Getter
    @Setter
    private MessageSender messageSender;

    public ContactOnline(Contact contact, Device device) {
        this.contact = contact;
        this.device = device;
    }

    @Override
    public String getId() {
        return contact.getIdentityKey();
    }
}
