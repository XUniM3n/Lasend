package org.lasend.desktop.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.whispersystems.libsignal.fingerprint.DisplayableFingerprint;

import java.net.URL;
import java.util.ResourceBundle;

public class ContactRequestSentController implements Initializable {
    @FXML
    public Label username;
    @FXML
    public Label fingerprint;

    @Override
    public void initialize(URL location, ResourceBundle resources) {


    }

    public void initData(String displayableFingerprint, String remoteUsername){
        this.fingerprint.setText(displayableFingerprint);
        this.username.setText(remoteUsername);
    }
}
