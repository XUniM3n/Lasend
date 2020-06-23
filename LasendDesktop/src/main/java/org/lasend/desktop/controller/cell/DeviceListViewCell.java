package org.lasend.desktop.controller.cell;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.lasend.api.exception.ApiNotInitializedException;
import org.lasend.api.exception.DeviceAlreadyInContactsException;
import org.lasend.api.exception.InvalidResponseException;
import org.lasend.api.model.ContactRequestSent;
import org.lasend.api.model.Device;
import org.lasend.desktop.LasendDesktop;
import org.lasend.desktop.controller.ContactRequestSentController;
import org.lasend.desktop.util.ExceptionHandler;

import java.io.IOException;

public class DeviceListViewCell extends ListCell<Device> {
    @FXML
    public Label username;
    @FXML
    public Label identifier;
    @FXML
    public Label ip;
    @FXML
    public Button addContactBtn;

    @Override
    protected void updateItem(Device item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setGraphic(null);
        } else {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/views/cell/DeviceCell.fxml"));
            loader.setController(this);
            Node cell = null;
            try {
                cell = loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }

            username.setText(item.getName());
            identifier.setText(item.getIdentity());
            ip.setText(item.getAddress().toString().substring(1));
            addContactBtn.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    addContact(item);
                }
            });
            setGraphic(cell);
        }

    }

    public void addContact(Device device) {
        try {
            ContactRequestSent contactRequest = LasendDesktop.getLasendApi().sendContactRequest(device);
            showContactRequest(contactRequest, LasendDesktop.getLasendApi().getComparableFingerprint(device), device);
        } catch (ApiNotInitializedException e) {
            e.printStackTrace();
        } catch (InvalidResponseException | IOException e) {
            ExceptionHandler.showAlertBlocking("Error sending request");
        } catch (DeviceAlreadyInContactsException e) {
            ExceptionHandler.showAlertBlocking("Device already in contacts");
        }
    }

    public void showContactRequest(ContactRequestSent contactRequest, String comparableFingerprint, Device device) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/views/ContactRequestSentView.fxml"));
        Parent contactRequestSentView = null;
        try {
            contactRequestSentView = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage stage = new Stage();
        Scene scene = new Scene(contactRequestSentView);
        stage.setTitle("Contact request sent, waiting for response");
        stage.setScene(scene);

        ContactRequestSentController controller = loader.<ContactRequestSentController>getController();
        controller.initData(comparableFingerprint, device.getName());

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                LasendDesktop.removeSentContactRequest(device.getIdentity());
            }
        });
        stage.show();

        LasendDesktop.getSentContactRequests().add(Pair.of(contactRequest, stage));
    }
}
