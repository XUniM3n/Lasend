package org.lasend.desktop.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.apache.commons.codec.digest.DigestUtils;
import org.lasend.api.model.Chat;
import org.lasend.desktop.LasendDesktop;
import sun.applet.Main;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.UUID;

public class StartController implements Initializable {

    @FXML
    public TextField usernameTextfield;

    public void onStartBtnClick(ActionEvent actionEvent) throws IOException {
        proceed();
    }

    public void onEnter(KeyEvent keyEvent) throws IOException {
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            proceed();
        }
    }

    private void proceed() throws IOException {
        String username = usernameTextfield.getText();
        LasendDesktop.initializeApi(username);
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/views/MainView.fxml"));
        Parent chatView = loader.load();
        Scene scene = new Scene(chatView);
        Stage primaryStage = LasendDesktop.getPrimaryStage();
        primaryStage.setTitle("Lasend");
        primaryStage.setScene(scene);

        MainWindowController controller = loader.<MainWindowController>getController();
        controller.initData();

        primaryStage.show();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
