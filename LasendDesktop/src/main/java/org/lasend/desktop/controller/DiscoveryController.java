package org.lasend.desktop.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import lombok.Getter;
import org.lasend.api.model.Device;
import org.lasend.desktop.LasendDesktop;
import org.lasend.desktop.controller.cell.DeviceListViewCell;
import org.lasend.desktop.util.ExceptionHandler;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class DiscoveryController implements Initializable {

    private static Stage stage;
    @Getter
    private static ObservableList<Device> deviceObservableList;
    @FXML
    private ListView<Device> deviceList;
    @Getter
    private static DiscoveryController controller;

    public static Stage getStage() {
        if (stage == null) {
            stage = new Stage();
        }
        return stage;
    }

    public static synchronized void addDevice(Device device) {
        boolean found = false;
        for (Device deviceFromList : deviceObservableList) {
            if (deviceFromList.getIdentity().equals(device.getIdentity())) {
                found = true;
            }
        }
        if (!found) {
            deviceObservableList.add(device);
        }
    }

    public void initData(){
        controller = this;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        deviceObservableList = FXCollections.observableArrayList(new ArrayList<Device>(LasendDesktop.getLasendTempStore().getFoundRemoteDevicesStore().getAll()));
        deviceList.setCellFactory(deviceListView -> new DeviceListViewCell());
        deviceList.setItems(deviceObservableList);
    }

    public void manuallyEnterIpBtn(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/views/ManualIpView.fxml"));
        Parent manualIpView = loader.load();
        Stage stage = new Stage();
        Scene scene = new Scene(manualIpView);
        stage.setTitle("Add Contact by IP");
        stage.setScene(scene);
        stage.show();
    }

    public void discoverDevicesOnLan(ActionEvent actionEvent) {
        try {
            deviceObservableList.clear();
            LasendDesktop.getLasendApi().discoverDevicesOnLan();
        } catch (IOException e) {
            ExceptionHandler.showAlertBlocking("Error finding devices");
        }
    }
}
