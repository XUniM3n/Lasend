package org.lasend.api.network.impl.socket;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.lasend.api.LasendCallbacks;
import org.lasend.api.dto.DataDto;
import org.lasend.api.dto.response.ResponseData;
import org.lasend.api.network.Sender;
import org.lasend.api.state.LasendStore;
import org.lasend.api.util.ExceptionCallback;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public abstract class TcpSocketSender implements Sender {
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
    ;
    protected InetAddress remoteAddress;
    protected int remotePort;
    protected Socket clientSocket;
    protected boolean isConnected;
    protected OutputStream outputStream;
    protected InputStream inputStream;
    protected BufferedReader reader;
    protected PrintWriter writer;
    protected LasendStore store;
    protected SenderCallback senderCallback;
    protected ExceptionCallback exceptionCallback;

    public TcpSocketSender(InetAddress remoteAddress, int remotePort, LasendStore store, LasendCallbacks callbacks) {
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
        this.store = store;
        this.senderCallback = callbacks.getSenderCallback();
        this.exceptionCallback = callbacks.getExceptionCallback();
    }

    protected void connect() throws IOException {
        try {
            clientSocket = new Socket(remoteAddress, remotePort);
            outputStream = clientSocket.getOutputStream();
            inputStream = clientSocket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));
            writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);
        } catch (IOException e) {
            exceptionCallback.onException(e, Thread.currentThread());
            throw new IOException();
        }

        isConnected = true;
    }

    protected void sendObject(Object obj) throws IOException {
        String json = requestObjectMapper.writeValueAsString(obj);
        writer.println(json);
        writer.flush();
    }

    public void stop() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            exceptionCallback.onException(e, Thread.currentThread());
        }

        isConnected = false;
    }
}
