package org.lasend.api.network.impl.socket;

import org.lasend.api.LasendCallbacks;
import org.lasend.api.constant.NetworkConstants;
import org.lasend.api.dto.DeviceInfoRequestDto;
import org.lasend.api.dto.response.DeviceInfoResponse;
import org.lasend.api.model.Device;
import org.lasend.api.network.DeviceInfoGetter;
import org.lasend.api.service.SentDataProcessor;
import org.lasend.api.state.LasendStore;

import java.io.IOException;
import java.net.InetAddress;

public class DeviceInfoSocketGetter extends TcpSocketSender implements DeviceInfoGetter {
    public DeviceInfoSocketGetter(InetAddress remoteAddress, LasendStore store, LasendCallbacks callbacks) {
        super(remoteAddress, NetworkConstants.PORT_LISTEN, store, callbacks);
    }

    @Override
    public Device getDeviceInfo() throws IOException {
        if (!isConnected) {
            connect();
        }

        DeviceInfoRequestDto deviceInfoRequest = new DeviceInfoRequestDto(store.getPersistent().getMeStore().getMe().getName(), store.getPersistent().getMeStore().getMe().getIdentity());
        sendObject(deviceInfoRequest);

        String line = reader.readLine();
        DeviceInfoResponse response = responseObjectMapper.readValue(line, DeviceInfoResponse.class);

        Device device = SentDataProcessor.processDeviceInfoRequestData(deviceInfoRequest, response, remoteAddress, store, senderCallback, exceptionCallback);

        stop();

        return device;
    }
}
