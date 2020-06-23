package org.lasend.desktop.controller;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.lasend.api.exception.ApiNotInitializedException;
import org.lasend.api.model.Device;
import org.lasend.desktop.LasendDesktop;
import org.lasend.desktop.util.ExceptionHandler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

public class ManualIpController implements Initializable {
    public TextField ipTextfield;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void discoverDeviceByIp(ActionEvent actionEvent) throws IOException, ApiNotInitializedException {
        String ipString = ipTextfield.getText();
        discoverIp(ipString);
    }

    public void onEnter(KeyEvent keyEvent) throws IOException, ApiNotInitializedException {
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            String ipString = ipTextfield.getText();
            discoverIp(ipString);
        }
    }

    private void discoverIp(String ipString) throws IOException {
        InetAddress address = null;
        try {
            address = InetAddress.getByName(ipString);
        } catch (UnknownHostException e) {
            ExceptionHandler.showAlertBlocking("Invalid IP");
        }
        Device device = null;
        try {
            device = LasendDesktop.getLasendApi().sendDeviceInfoRequest(address);
        } catch (ApiNotInitializedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            ExceptionHandler.showAlertEventThread("Device with ip " + address.toString() + " is not available");
        }
        if (device == null) {
            ExceptionHandler.showAlertBlocking("Device with ip " + address.toString() + " is not available");
        } else {
            DiscoveryController.addDevice(device);
            Stage currentStage = (Stage) ipTextfield.getScene().getWindow();
            currentStage.close();
        }
    }
}
