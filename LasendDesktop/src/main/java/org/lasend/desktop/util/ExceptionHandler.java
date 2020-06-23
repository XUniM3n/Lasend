package org.lasend.desktop.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.lasend.desktop.LasendDesktop;

public class ExceptionHandler {
    public static void showAlertEventThread(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, message);
            alert.show();
        });
    }

    public static void showAlertEventThreadAndExit(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, message);
            alert.show();
            LasendDesktop.getLasendApi().stop();
            Platform.exit();
        });
    }

    public static void showAlertBlocking(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }

    public static void showAlertBlockingAndExit(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
        LasendDesktop.getLasendApi().stop();
        Platform.exit();
    }
}