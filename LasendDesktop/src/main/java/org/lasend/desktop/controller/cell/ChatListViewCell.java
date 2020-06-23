package org.lasend.desktop.controller.cell;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;
import org.lasend.api.model.Chat;
import org.lasend.desktop.controller.MainWindowController;

import java.io.IOException;

public class ChatListViewCell extends ListCell<Chat> {
    @FXML
    public Label chatName;
    @FXML
    public GridPane chatNameGridPane;

    @Override
    protected void updateItem(Chat item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setGraphic(null);
        } else {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/views/cell/ChatCell.fxml"));
            loader.setController(this);
            Node cell = null;
            try {
                cell = loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }

            chatName.setText(item.getName());
            chatNameGridPane.setOnMouseClicked(event -> {
                FXMLLoader loader2 = new FXMLLoader();
                loader2.setLocation(getClass().getResource("/views/MainView.fxml"));
                try {
                    loader2.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                MainWindowController.getController().loadChat(item);
            });
            setGraphic(cell);
        }
    }
}
