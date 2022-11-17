package com.dosse.speedtest.controllers;

import com.jfoenix.controls.JFXButton;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class DialogShowPrivacy extends StackPane {

    private AnchorPane anchorPane;
    private DialogShowPrivacyController dialogShowPrivacyController;


    private StackPane stackPane = new StackPane();
    private Group group = new Group();
    private Node view;

    private FadeTransition fadeIN = new FadeTransition();
    private FadeTransition fadeOut = new FadeTransition();


    public DialogShowPrivacy (AnchorPane rootLayout) {
        this.anchorPane = rootLayout;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/layouts/dialog_privacy.fxml"));
        fxmlLoader.setControllerFactory(new Callback<Class<?>, Object>() {
            @Override
            public Object call(Class<?> param) {
                return dialogShowPrivacyController = new DialogShowPrivacyController();
            }
        });
        try {
            view = (Node) fxmlLoader.load();
        } catch (IOException ignored) {
        }

        anim();
        setDefsConfigs();


        dialogShowPrivacyController.btn_close_dialog_show_privacy.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                cancelDialog();
            }
        });


        try {
            String data = readFileFromResources("/configs/privacy_en.html");
            dialogShowPrivacyController.webView_privacy.getEngine().loadContent(data);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }

    }


    private void setDefsConfigs () {
        stackPane.prefWidthProperty().bind(anchorPane.widthProperty());
        stackPane.prefHeightProperty().bind(anchorPane.heightProperty());
        stackPane.setStyle("-fx-background-color: #00000099");
        stackPane.setAlignment(Pos.CENTER);
        stackPane.setOpacity(0);
    }

    private void anim () {
        fadeIN.setNode(stackPane);
        fadeIN.setDuration(Duration.millis(250));
        fadeIN.setFromValue(0);
        fadeIN.setToValue(1);

        fadeOut.setToValue(0);
        fadeOut.setFromValue(1);
        fadeOut.setDuration(Duration.millis(250));
        fadeOut.setNode(stackPane);
        fadeOut.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                anchorPane.getChildren().remove(stackPane);
            }
        });
    }

    public void setCancelable (boolean cancelable) {
        if (cancelable) {
            stackPane.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    fadeOut.play();
                }
            });
        }
    }

    public void showDialog () {
        group.getChildren().add(view);
        stackPane.getChildren().add(group);
        anchorPane.getChildren().add(stackPane);
        fadeIN.play();
    }

    public void cancelDialog () {
        fadeOut.play();
    }



    public static class DialogShowPrivacyController implements Initializable {

        @FXML
        JFXButton btn_close_dialog_show_privacy;

        @FXML
        WebView webView_privacy;

        @Override
        public void initialize(URL location, ResourceBundle resources) {

        }

    }



    public String readFileFromResources(String filename) throws URISyntaxException, IOException {
        InputStream inputStream = anchorPane.getClass().getResourceAsStream(filename);
        java.util.Scanner s = new java.util.Scanner(inputStream).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
