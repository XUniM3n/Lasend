package org.lasend.desktop;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;
import org.lasend.api.LasendApi;
import org.lasend.api.LasendCallbacks;
import org.lasend.api.LasendPersistentStore;
import org.lasend.api.model.ContactRequestReceived;
import org.lasend.api.model.ContactRequestSent;
import org.lasend.api.model.Me;
import org.lasend.api.state.LasendTempStore;
import org.lasend.api.state.impl.InMemoryLasendPersistentStore;
import org.lasend.desktop.util.ExceptionHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LasendDesktop extends Application {
    @Getter
    @Setter
    private static Stage primaryStage;
    @Getter
    @Setter
    private static LasendApi lasendApi;
    @Getter
    private static LasendPersistentStore lasendPersistentStore;
    @Getter
    private static LasendTempStore lasendTempStore;
    private static LasendCallbacks callbacks;
    @Getter
    private static List<Pair<ContactRequestReceived, Stage>> receivedContactRequests;
    @Getter
    private static List<Pair<ContactRequestSent, Stage>> sentContactRequests;

    public static void main(String[] args) {
        lasendPersistentStore = new InMemoryLasendPersistentStore();
        callbacks = new LasendCallbacksDesktopImpl();
        lasendApi = new LasendApi(lasendPersistentStore, callbacks);
        lasendTempStore = lasendApi.getStore().getTemp();
        receivedContactRequests = new ArrayList<>();
        sentContactRequests = new ArrayList<>();
        Application.launch(args);
    }

    public static void initializeApi(String username) {
        Me me = new Me(username);
        lasendPersistentStore.getMeStore().storeMe(me);
        if (!(lasendApi.isInitialized() || lasendApi.isInitializing())) {
            try {
                lasendApi.init();
            } catch (IOException e) {
                ExceptionHandler.showAlertEventThread("Error initialising API");
                Platform.exit();
            }
        }
    }

    public static synchronized void removeReceviedContactRequest(String deviceId) {
        for (int i = 0; i < receivedContactRequests.size(); i++) {
            Pair<ContactRequestReceived, Stage> contactRequestPlusStage = receivedContactRequests.get(i);
            if (contactRequestPlusStage.getLeft().getSenderIdentity().equals(deviceId)) {
                contactRequestPlusStage.getRight().close();
                receivedContactRequests.remove(i);
                break;
            }
        }
    }

    public static synchronized void removeSentContactRequest(String deviceId) {
        for (int i = 0; i < sentContactRequests.size(); i++) {
            Pair<ContactRequestSent, Stage> contactRequestPlusStage = sentContactRequests.get(i);
            if (contactRequestPlusStage.getLeft().getReceiverIdentity().equals(deviceId)) {
                contactRequestPlusStage.getRight().close();
                sentContactRequests.remove(i);
                break;
            }
        }
    }

    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/views/StartView.fxml"));
        Parent window = loader.load();
        Scene scene = new Scene(window);
        setPrimaryStage(primaryStage);
        primaryStage.setOnCloseRequest(event -> {
            getLasendApi().stop();
            Platform.exit();
        });
        primaryStage.setScene(scene);
        primaryStage.setTitle("Lasend");
        primaryStage.show();
    }
}
