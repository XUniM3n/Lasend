package org.lasend.api.network.impl.socket;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.IOUtils;
import org.lasend.api.LasendCallbacks;
import org.lasend.api.constant.NetworkConstants;
import org.lasend.api.dto.DataDto;
import org.lasend.api.dto.encrypted.message.MessageDto;
import org.lasend.api.dto.response.FileResponse;
import org.lasend.api.dto.response.ResponseData;
import org.lasend.api.dto.response.SuccessResponse;
import org.lasend.api.dto.response.UnknownFileResponse;
import org.lasend.api.model.SharedFile;
import org.lasend.api.network.DataReceiver;
import org.lasend.api.service.ReceivedDataProcessor;
import org.lasend.api.state.LasendStore;
import org.lasend.api.util.ExceptionCallback;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class DataTcpSocketReceiver implements DataReceiver, Runnable {
    private InetAddress listenAddress;
    private int listenPort;
    private ServerSocket serverSocket;
    @Getter
    private boolean isListening;
    private LasendStore store;
    private ReceiverCallback receiverCallback;
    private ExceptionCallback exceptionCallback;
    private Thread thread;

    public DataTcpSocketReceiver(final InetAddress listenAddress, LasendStore store, LasendCallbacks callbacks) throws IOException {
        this.listenAddress = listenAddress;
        this.listenPort = NetworkConstants.PORT_LISTEN;
        this.isListening = false;
        this.store = store;
        this.exceptionCallback = callbacks.getExceptionCallback();
        this.receiverCallback = callbacks.getReceiverCallback();
    }

    @Override
    public void run() {
        try {
            this.serverSocket = new ServerSocket(listenPort, 500, listenAddress);
        } catch (IOException e) {
            exceptionCallback.onException(e, Thread.currentThread());
        }
        isListening = true;
        while (isListening) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
                DataTcpConnectionHandler connectionHandler = new DataTcpConnectionHandler(socket, store, receiverCallback, exceptionCallback);
                Thread handlerThread = new Thread(connectionHandler);
                handlerThread.start();
            } catch (IOException e) {
            }
        }
    }

    public void start() {
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void stop() {
        isListening = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            // Keep calm
        }
    }

    private static class DataTcpConnectionHandler implements Runnable {
        protected final ObjectMapper requestObjectMapper = JsonMapper.builder(JsonFactory.builder().disable(StreamReadFeature.AUTO_CLOSE_SOURCE).build())
                .activateDefaultTyping(BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType(DataDto.class)
                        .build(), ObjectMapper.DefaultTyping.NON_FINAL).build()
                .configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        protected final ObjectMapper responseObjectMapper = JsonMapper.builder(JsonFactory.builder().disable(StreamReadFeature.AUTO_CLOSE_SOURCE).build())
                .activateDefaultTyping(BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType(ResponseData.class)
                        .build(), ObjectMapper.DefaultTyping.NON_FINAL).build()
                .configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        private final LasendStore store;
        private final ReceiverCallback receiverCallback;
        private final ExceptionCallback exceptionCallback;
        @Setter
        private Socket socket;
        private BufferedReader reader;
        private PrintWriter writer;

        public DataTcpConnectionHandler(LasendStore store, ReceiverCallback receiverCallback, ExceptionCallback exceptionCallback) {
            this.store = store;
            this.receiverCallback = receiverCallback;
            this.exceptionCallback = exceptionCallback;
        }

        public DataTcpConnectionHandler(Socket socket, LasendStore store, ReceiverCallback receiverCallback, ExceptionCallback exceptionCallback) {
            this(store, receiverCallback, exceptionCallback);
            this.socket = socket;
        }

        public static DataTcpConnectionHandler getConnectionHandler(LasendStore store, ReceiverCallback receiverCallback, ExceptionCallback exceptionCallback) {
            DataTcpConnectionHandler connectionHandler = new DataTcpConnectionHandler(store, receiverCallback, exceptionCallback);
            return connectionHandler;

        }

        @Override
        public void run() {
            try {
                InputStream inputStream = socket.getInputStream();
                reader = new BufferedReader(new InputStreamReader(inputStream));

                OutputStream outputStream = socket.getOutputStream();
                writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);

                boolean socketNotNeededAnymore = true;

                String line;
                do {
                    line = reader.readLine();
                    DataDto dataDto = requestObjectMapper.readValue(line, DataDto.class);
                    try {
                        String socketAddress = socket.getRemoteSocketAddress().toString();
                        String socketIp = socketAddress.substring(1, socketAddress.indexOf(':'));
                        InetAddress remoteAddress = InetAddress.getByName(socketIp);
                        ResponseData response = ReceivedDataProcessor.processData(dataDto, remoteAddress, store, receiverCallback, exceptionCallback);

                        if (response instanceof FileResponse) {
                            FileResponse fileResponse = (FileResponse) response;
                            SharedFile sharedFile = store.getPersistent().getSharedFileStore().getById(fileResponse.getFileId());
                            if (sharedFile == null) {
                                sendObject(new UnknownFileResponse());
                            } else {
                                sendObject(new SuccessResponse());
                                InputStream fileInputStream = sharedFile.getEncryptedInputStream();
                                IOUtils.copy(fileInputStream, outputStream);
                                fileInputStream.close();
                            }
                        } else {
                            sendObject(response);
                        }

                        if (dataDto instanceof MessageDto) {
                            socketNotNeededAnymore = false;
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } while (!socketNotNeededAnymore);
            } catch (IOException ex) {
                exceptionCallback.onException(ex, Thread.currentThread());
            } finally {
                close();
            }
        }

        public void close() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendObject(Object obj) {
            String json = null;
            try {
                json = responseObjectMapper.writeValueAsString(obj);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            writer.println(json);
            writer.flush();
        }
    }
}
