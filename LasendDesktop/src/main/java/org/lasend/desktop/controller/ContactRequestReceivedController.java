package org.lasend.desktop.controller;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.lasend.api.exception.DeviceAlreadyInContactsException;
import org.lasend.api.exception.DeviceIsNotOnlineException;
import org.lasend.api.model.Chat;
import org.lasend.api.model.ContactRequestReceived;
import org.lasend.api.model.Device;
import org.lasend.desktop.LasendDesktop;
import org.lasend.desktop.util.ExceptionHandler;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ContactRequestReceivedController implements Initializable {
    @FXML
    public Label username;
    @FXML
    public Label fingerprint;
    private Device sender;
    private ContactRequestReceived contactRequest;

    @Override
    public void initialize(URL location, ResourceBundle resources) {


    }

    public void initData(String displayableFingerprint, ContactRequestReceived contactRequest, Device sender) {
        this.fingerprint.setText(displayableFingerprint);
        this.username.setText(sender.getName());
        this.contactRequest = contactRequest;
        this.sender = sender;
    }

    public void rejectContactBtn(ActionEvent actionEvent) {
        try {
            LasendDesktop.getLasendApi().sendContactReject(contactRequest);
        } catch (IOException e) {
            e.printStackTrace();
            ExceptionHandler.showAlertBlocking("Error sending rejection to " + sender.getName());
        } catch (DeviceIsNotOnlineException e) {
            e.printStackTrace();
            ExceptionHandler.showAlertBlocking(sender.getName() + " is not online");
        } catch (Exception e) {
            e.printStackTrace();
            ExceptionHandler.showAlertBlocking("Error sending rejection to " + sender.getName());
        } finally {
            LasendDesktop.removeReceviedContactRequest(sender.getIdentity());
        }
    }

    public void acceptContactBtn(ActionEvent actionEvent) {
        try {
            String newChatId = LasendDesktop.getLasendApi().sendContactAccept(contactRequest);
            Chat chat = LasendDesktop.getLasendPersistentStore().getChatStore().getById(newChatId);
            MainWindowController.getController().addChat(chat);
        } catch (IOException e) {
            e.printStackTrace();
            ExceptionHandler.showAlertBlocking("Error sending acceptance " + sender.getName());
        } catch (DeviceIsNotOnlineException e) {
            e.printStackTrace();
            ExceptionHandler.showAlertBlocking(sender.getName() + " is not online");
        } catch (DeviceAlreadyInContactsException e) {
            e.printStackTrace();
            ExceptionHandler.showAlertBlocking(sender.getName() + " already in contacts");
        } catch (Exception e) {
            e.printStackTrace();
            ExceptionHandler.showAlertBlocking("Error sending acceptance " + sender.getName());
        } finally {
            LasendDesktop.removeReceviedContactRequest(sender.getIdentity());
        }
    }
}
