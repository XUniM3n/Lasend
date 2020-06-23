package org.lasend.desktop;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.lasend.api.LasendCallbacks;
import org.lasend.api.callback.DeviceDiscoveredCallback;
import org.lasend.api.dto.ContactAcceptDto;
import org.lasend.api.dto.ContactRejectDto;
import org.lasend.api.dto.ContactRequestDto;
import org.lasend.api.dto.encrypted.FileRequestDto;
import org.lasend.api.dto.encrypted.message.MessageDecryptedPayload;
import org.lasend.api.dto.encrypted.message.MessageDto;
import org.lasend.api.model.Chat;
import org.lasend.api.model.ContactRequestReceived;
import org.lasend.api.model.Device;
import org.lasend.api.model.chat.Message;
import org.lasend.api.network.Receiver;
import org.lasend.api.network.Sender;
import org.lasend.api.util.ExceptionCallback;
import org.lasend.desktop.controller.ContactRequestReceivedController;
import org.lasend.desktop.controller.DiscoveryController;
import org.lasend.desktop.controller.MainWindowController;
import org.lasend.desktop.util.ExceptionHandler;

import java.io.IOException;

public class LasendCallbacksDesktopImpl implements LasendCallbacks {
    @Getter
    public Sender.SenderCallback senderCallback;
    @Getter
    public Receiver.ReceiverCallback receiverCallback;
    @Getter
    private ExceptionCallback exceptionCallback;
    @Getter
    private DeviceDiscoveredCallback deviceDiscoveredCallback;

    public LasendCallbacksDesktopImpl() {
        exceptionCallback = new ExceptionCallback() {
            @Override
            public void onException(Exception e, Thread t) {
                ExceptionHandler.showAlertEventThread(e.getMessage());
            }

            @Override
            public void onReceiveFileException(Exception e) {
                e.printStackTrace();
            }

            @Override
            public void onMessageSendException(Exception e) {
                e.printStackTrace();
            }

            @Override
            public void onSendContactRequestException(Exception e, Device receiver) {
                e.printStackTrace();
            }

            @Override
            public void onSendContactAcceptException(Exception e, Device receiver) {
                for (Pair<ContactRequestReceived, Stage> contactRequestPlusStage : LasendDesktop.getReceivedContactRequests()) {
                    if (contactRequestPlusStage.getLeft().getSenderIdentity().equals(receiver.getIdentity())) {
                        Platform.runLater(() -> {
                            Alert info = new Alert(Alert.AlertType.ERROR, "Error sending contact acceptance request to " + receiver.getName());
                            info.show();
                            contactRequestPlusStage.getRight().close();
                        });
                        LasendDesktop.getSentContactRequests().remove(contactRequestPlusStage);
                    }
                }
            }

            @Override
            public void onSendContactRejectException(Exception e, Device receiver) {

            }

            @Override
            public void onReceiveContactRequestException(Exception e, Device sender) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    Alert info = new Alert(Alert.AlertType.ERROR, "Error receiving contact request from " + sender.getName());
                    info.show();
                });
            }

            @Override
            public void onReceiveContactAcceptException(Exception e, Device sender) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    Alert info = new Alert(Alert.AlertType.ERROR, "Error receiving contact acceptance from " + sender.getName());
                    info.show();
                    LasendDesktop.removeSentContactRequest(sender.getIdentity());
                });
            }

            @Override
            public void onReceiveContactRejectException(Exception e, Device sender) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    Alert info = new Alert(Alert.AlertType.ERROR, "Error receiving contact reject from " + sender.getName());
                    info.show();
                    LasendDesktop.removeSentContactRequest(sender.getIdentity());
                });
            }
        };

        senderCallback = new Sender.SenderCallback() {
            @Override
            public void onChatMessageSent(MessageDto dto, MessageDecryptedPayload payload) {
                // Nothing
            }

            @Override
            public void onContactRequestSent(ContactRequestDto dto) {
                // Nothing
            }

            @Override
            public void onContactAcceptSent(ContactAcceptDto dto, ContactAcceptDto.ContactAcceptDecryptedPayload payload) {
                // Nothing
            }

            @Override
            public void onContactRejectSent(ContactRejectDto dto) {
                // Nothing
            }

            @Override
            public void onFileRequestSent(FileRequestDto dto, FileRequestDto.FileRequestDecryptedPayload payload) {
                // Nothing
            }

            @Override
            public void onDeviceInfo(Device returnedDevice) {
                // Nothing
            }
        };

        receiverCallback = new Receiver.ReceiverCallback() {
            @Override
            public void onChatMessageReceived(Message message, Chat chat) {
                MainWindowController.getController().addMessageToChat(message, chat);
            }

            @Override
            public void onContactRequestReceived(ContactRequestReceived contactRequest, Device sender, String comparableFingerprint) {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/views/ContactRequestReceivedView.fxml"));
                Parent contactRequestReceivedView = null;
                try {
                    contactRequestReceivedView = loader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Parent finalContactRequestReceivedView = contactRequestReceivedView;
                Platform.runLater(() -> {
                    Stage stage = new Stage();
                    Scene scene = new Scene(finalContactRequestReceivedView);
                    stage.setTitle("Contact request received");
                    stage.setScene(scene);

                    ContactRequestReceivedController controller = loader.<ContactRequestReceivedController>getController();
                    controller.initData(comparableFingerprint, contactRequest, sender);

                    stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                        @Override
                        public void handle(WindowEvent event) {
                            LasendDesktop.removeReceviedContactRequest(sender.getIdentity());
                        }
                    });
                    stage.show();

                    LasendDesktop.getReceivedContactRequests().add(Pair.of(contactRequest, stage));
                });
            }

            @Override
            public void onContactAcceptReceived(ContactAcceptDto dto, ContactAcceptDto.ContactAcceptDecryptedPayload payload, Device sender) {
                Platform.runLater(() -> {
                    Alert info = new Alert(Alert.AlertType.INFORMATION, sender.getName() + " accepted your contact request");
                    info.show();
                    LasendDesktop.removeSentContactRequest(sender.getIdentity());
                });

                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/views/MainView.fxml"));
                try {
                    loader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String newChatId = payload.getChatId();
                Chat newChat = LasendDesktop.getLasendPersistentStore().getChatStore().getById(newChatId);
                MainWindowController.getController().addChat(newChat);
            }

            @Override
            public void onContactRejectReceived(ContactRejectDto dto, Device sender) {
                Platform.runLater(() -> {
                    Alert info = new Alert(Alert.AlertType.INFORMATION, sender.getName() + " rejected your contact request");
                    info.show();
                    LasendDesktop.removeSentContactRequest(sender.getIdentity());
                });
            }
        };

        deviceDiscoveredCallback = new DeviceDiscoveredCallback() {
            @Override
            public void onDeviceDiscovered(Device device) {
                DiscoveryController.addDevice(device);
            }
        };
    }
}
