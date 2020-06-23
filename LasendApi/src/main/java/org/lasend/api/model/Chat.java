package org.lasend.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.lasend.api.model.chat.FileLinkMessage;
import org.lasend.api.model.chat.Message;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class Chat implements GetIdAble {
    private final List<ChatCallback> callbacks = new ArrayList<ChatCallback>();
    @Getter
    private String id;
    @Getter
    private String name;
    @Getter
    private List<Message> messages;

    public Chat(String chatId) {
        this.id = chatId;
        this.messages = new ArrayList<>();
    }

    public void appendMessage(Message message) {
        messages.add(message);
        for (ChatCallback callback : callbacks) {
            callback.onNewMessage(message);
        }
    }

    public List<FileLinkMessage> getSharedFiles() {
        List<FileLinkMessage> fileLinks = new ArrayList<>();
        for (Message message : messages) {
            if (message instanceof FileLinkMessage) {
                fileLinks.add((FileLinkMessage) message);
            }
        }
        return fileLinks;
    }

    public void registerCallback(ChatCallback callback) {
        this.callbacks.add(callback);
    }

    public interface ChatCallback {
        void onNewMessage(Message message);
    }

}
