package org.lasend.api.network.impl.socket;

import org.apache.commons.io.IOUtils;
import org.lasend.api.LasendCallbacks;
import org.lasend.api.constant.NetworkConstants;
import org.lasend.api.dto.DataDto;
import org.lasend.api.dto.encrypted.FileRequestDto;
import org.lasend.api.dto.response.ResponseData;
import org.lasend.api.exception.InvalidFileReceivedException;
import org.lasend.api.exception.InvalidResponseException;
import org.lasend.api.model.Device;
import org.lasend.api.model.chat.FileLinkMessage;
import org.lasend.api.network.FileRequestSender;
import org.lasend.api.service.SentDataProcessor;
import org.lasend.api.state.LasendStore;
import org.lasend.api.util.EncryptionUtil;
import org.lasend.api.util.LasendStoreUtil;
import org.whispersystems.libsignal.SessionCipher;
import org.whispersystems.libsignal.UntrustedIdentityException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileRequestSocketSender extends TcpSocketSender implements FileRequestSender {
    private final Device remoteDevice;
    private final SessionCipher sessionCipher;

    public FileRequestSocketSender(Device remoteDevice, LasendStore store, LasendCallbacks callbacks) throws IOException {
        super(remoteDevice.getAddress(), NetworkConstants.PORT_LISTEN, store, callbacks);
        this.remoteDevice = remoteDevice;
        this.sessionCipher = new SessionCipher(store.getPersistent().getMeStore().getMe().getSignalProtocolStore(), remoteDevice.getSignalProtocolAddress());
    }

    /**
     * @param file - where to write received file
     */
    public void receiveFile(FileLinkMessage fileLink, File file) throws UntrustedIdentityException, InvalidFileReceivedException, IOException, InvalidResponseException {
        FileRequestDto.FileRequestDecryptedPayload payload = new FileRequestDto.FileRequestDecryptedPayload(fileLink.getFileId());
        FileRequestDto fileRequest = new FileRequestDto(payload, sessionCipher, store.getPersistent().getMeStore().getMe().getIdentity());
        sendFileRequest(fileRequest, payload, fileLink, file);
    }

    private void sendFileRequest(FileRequestDto fileRequestDto, FileRequestDto.FileRequestDecryptedPayload payload, FileLinkMessage fileLink, File file) throws IOException, InvalidResponseException, InvalidFileReceivedException {
        try {
            if (!isConnected) {
                connect();
            }

            sendObject(fileRequestDto);

            String line = reader.readLine();
            DataDto response = requestObjectMapper.readValue(line, DataDto.class);
            if (!(response instanceof ResponseData)) {
                throw new InvalidResponseException("Unsuccessful");
            } else {
                SentDataProcessor.processFileRequest(fileRequestDto, payload, remoteDevice, store, senderCallback, exceptionCallback);
            }

            FileReceiverThread fileReceiverThread = new FileReceiverThread(inputStream, file, fileLink);
            fileReceiverThread.start();

        } catch (InvalidResponseException | IOException e) {
            exceptionCallback.onException(e, Thread.currentThread());
            throw e;
        }
    }

    private class FileReceiverThread extends Thread {
        private InputStream inputStream;
        private File file;
        private FileLinkMessage fileLink;

        public FileReceiverThread(InputStream encryptedFileStream, File file, FileLinkMessage fileLink) {
            this.inputStream = encryptedFileStream;
            this.file = file;
            this.fileLink = fileLink;
        }

        @Override
        public void run() {
            InputStream fileInputStream = EncryptionUtil.decryptInputStream(inputStream, fileLink.getEncryptionKey(), fileLink.getIv());
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                IOUtils.copy(fileInputStream, fileOutputStream);
                String fileResultingHash = LasendStoreUtil.getFileHash(file);
                if (!fileResultingHash.equals(fileLink.getFileHash()) || file.length() != fileLink.getFileSize()) {
                    exceptionCallback.onReceiveFileException(new InvalidFileReceivedException());
                }
                fileOutputStream.close();
            } catch (IOException e) {
                exceptionCallback.onReceiveFileException(e);
            }

            close();
        }
    }
}
