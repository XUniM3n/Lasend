package org.lasend.desktop.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Getter;
import org.lasend.api.exception.ChatHasNoOnlineUsersException;
import org.lasend.api.model.Chat;
import org.lasend.api.model.chat.FileLinkMessage;
import org.lasend.api.model.chat.Message;
import org.lasend.api.model.chat.TextMessage;
import org.lasend.desktop.LasendDesktop;
import org.lasend.desktop.controller.cell.ChatListViewCell;
import org.lasend.desktop.controller.cell.MessageListViewCell;
import org.lasend.desktop.util.ExceptionHandler;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class MainWindowController implements Initializable {
    @Getter
    private static MainWindowController controller;
    @FXML
    public VBox chatPane;
    @FXML
    public ListView<Message> messageListView;
    @FXML
    public Label chatName;
    @FXML
    public TextArea messageBox;
    @FXML
    public Button sentTextBtn;
    @FXML
    public Button fileBtn;
    @Getter
    private ObservableList<Chat> chatObservableList;
    @Getter
    private ObservableList<Message> messageObservableList;
    @Getter
    private Chat loadedChat;
    @FXML
    private ListView<Chat> chatList;
    @FXML
    private Label usernameLabel;
    @FXML
    private Label fingerprintLabel;

    public synchronized void addChat(Chat chat) {
        boolean found = false;
        for (Chat chatFromList : chatObservableList) {
            if (chatFromList.getId().equals(chat.getId())) {
                found = true;
                break;
            }
        }
        if (!found) {
            chatObservableList.add(chat);
        }
    }

    public void loadChat(Chat chat) {
        chatPane.setVisible(true);
        chatName.setText(chat.getName());
        loadedChat = chat;
        messageObservableList = FXCollections.observableArrayList(new ArrayList<>(chat.getMessages()));
        messageListView.setCellFactory(messageListView -> new MessageListViewCell());
        messageListView.setItems(messageObservableList);
    }

    public void addMessageToChat(Message message, Chat chat) {
        if (loadedChat != null) {
            if (!chat.getId().equals(loadedChat.getId())) {
                return;
            }
            messageObservableList.add(message);
        }

    }

    public void sendFileBtn(ActionEvent actionEvent) {
        Stage fileChooserStage = new Stage();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("File to send");
        File fileToSend = fileChooser.showOpenDialog(fileChooserStage);
        if (fileToSend == null) {
            return;
        }

        try {
            Chat chat = loadedChat;
            FileLinkMessage message = LasendDesktop.getLasendApi().sendFileMessage(fileToSend, loadedChat);
            addMessageToChat(message, chat
            );
        } catch (ChatHasNoOnlineUsersException e) {
            ExceptionHandler.showAlertBlocking("User is not online");
            e.printStackTrace();
        } catch (Exception e) {
            ExceptionHandler.showAlertBlocking("Error sending message");
            e.printStackTrace();
        }
    }

    public void sendTextBtn(ActionEvent actionEvent) {
        String textToSend = messageBox.getText();
        try {
            Chat chat = loadedChat;
            TextMessage message = LasendDesktop.getLasendApi().sendTextMessage(textToSend, chat);
            addMessageToChat(message, chat);
            messageBox.clear();
        } catch (ChatHasNoOnlineUsersException e) {
            ExceptionHandler.showAlertBlocking("User is not online");
            e.printStackTrace();
        } catch (Exception e) {
            ExceptionHandler.showAlertBlocking("Error sending message");
            e.printStackTrace();
        }
    }

    public void discoveryBtn(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/views/DiscoveryView.fxml"));
        Parent discoveryView = loader.load();
        Scene scene = new Scene(discoveryView);
        Stage discoveryStage = DiscoveryController.getStage();
        discoveryStage.setScene(scene);
        discoveryStage.setTitle("LAN and Contact Request");
        discoveryStage.show();

        DiscoveryController controller = loader.<DiscoveryController>getController();
        controller.initData();
    }

    public void initData() {
        chatPane.setVisible(false);
        chatObservableList = FXCollections.observableArrayList(new ArrayList<>(LasendDesktop.getLasendPersistentStore().getChatStore().getAll()));
        chatList.setCellFactory(chatListView -> new ChatListViewCell());
        chatList.setItems(chatObservableList);
        controller = this;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        usernameLabel.setText(LasendDesktop.getLasendPersistentStore().getMeStore().getMe().getName());
        fingerprintLabel.setText(LasendDesktop.getLasendPersistentStore().getMeStore().getMe().getIdentity());
    }
}
