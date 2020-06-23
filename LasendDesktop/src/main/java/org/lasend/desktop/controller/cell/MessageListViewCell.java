package org.lasend.desktop.controller.cell;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.lasend.api.exception.ApiNotInitializedException;
import org.lasend.api.model.chat.FileLinkMessage;
import org.lasend.api.model.chat.Message;
import org.lasend.api.model.chat.TextMessage;
import org.lasend.desktop.LasendDesktop;

import java.io.File;
import java.io.IOException;

public class MessageListViewCell extends ListCell<Message> {
    @FXML
    private Label text;
    @FXML
    private Label fileName;
    @FXML
    private Label fileSize;
    @FXML
    private Button downloadBtn;

    @Override
    protected void updateItem(Message item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setGraphic(null);
        } else {
            FXMLLoader loader = new FXMLLoader();
            if (item instanceof TextMessage) {
                if (item.getSenderIdentity().equals(LasendDesktop.getLasendPersistentStore().getMeStore().getMe().getIdentity())) {
                    loader.setLocation(getClass().getResource("/views/cell/TextMessageMyCell.fxml"));
                } else {
                    loader.setLocation(getClass().getResource("/views/cell/TextMessageNotMyCell.fxml"));
                }

                loader.setController(this);
                Node cell = null;
                try {
                    cell = loader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                TextMessage textMessage = (TextMessage) item;

                text.setText(textMessage.getText());
                setGraphic(cell);

            } else if (item instanceof FileLinkMessage) {
                boolean isMyMessage;
                if (item.getSenderIdentity().equals(LasendDesktop.getLasendPersistentStore().getMeStore().getMe().getIdentity())) {
                    loader.setLocation(getClass().getResource("/views/cell/FileLinkMessageMyCell.fxml"));
                    isMyMessage = true;
                } else {
                    loader.setLocation(getClass().getResource("/views/cell/FileLinkMessageNotMyCell.fxml"));
                    isMyMessage = false;
                }

                loader.setController(this);
                Node cell = null;
                try {
                    cell = loader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                FileLinkMessage fileLinkMessage = (FileLinkMessage) item;

                this.fileName.setText(fileLinkMessage.getFileName());
                this.fileSize.setText(FileUtils.byteCountToDisplaySize(fileLinkMessage.getFileSize()));

                if (!isMyMessage) {
                    this.downloadBtn.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            FileChooser fileChooser = new FileChooser();
                            fileChooser.setInitialFileName(fileLinkMessage.getFileName());
                            Stage saveFileDialogStage = new Stage();
                            File file = fileChooser.showSaveDialog(saveFileDialogStage);

                            try {
                                LasendDesktop.getLasendApi().receiveFile(fileLinkMessage, file);
                            } catch (ApiNotInitializedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

                setGraphic(cell);
            }
        }
    }
}
