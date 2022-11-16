package com.dosse.speedtest.controllers;

import com.dosse.speedtest.ServerItemCreator;
import com.dosse.speedtest.core.serverSelector.TestPoint;
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
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.Duration;
import com.dosse.speedtest.ServerItemCreator;
import com.dosse.speedtest.core.serverSelector.TestPoint;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class DialogSelectServer extends StackPane {

    private AnchorPane anchorPane;
    private DialogSelectServerController dialogSelectServerController;

    private ArrayList<TestPoint> arrayList = new ArrayList<>();

    private StackPane stackPane = new StackPane();
    private Group group = new Group();
    private Node view;

    private FadeTransition fadeIN = new FadeTransition();
    private FadeTransition fadeOut = new FadeTransition();

    private onConfirmListener onConfirmListener;

    public DialogSelectServer (AnchorPane rootLayout,ArrayList<TestPoint> arrayList) {
        this.anchorPane = rootLayout;
        this.arrayList = arrayList;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/layouts/dialog_select_server.fxml"));
        fxmlLoader.setControllerFactory(new Callback<Class<?>, Object>() {
            @Override
            public Object call(Class<?> param) {
                return dialogSelectServerController = new DialogSelectServerController();
            }
        });
        try {
            view = (Node) fxmlLoader.load();
        } catch (IOException ignored) {
        }

        anim();
        setDefsConfigs();


        dialogSelectServerController.btn_close_dialog_select_server.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                cancelDialog();
            }
        });


        ServerItemCreator serverItemCreator = new ServerItemCreator(arrayList);
        serverItemCreator.setOnItemSelectListener(new ServerItemCreator.onItemSelectListener() {
            @Override
            public void onItemSelected(TestPoint testPoint) {
                onConfirmListener.onConfirmed(testPoint);
            }
        });
        dialogSelectServerController.layout_list_dialog_select_server.getChildren().addAll(serverItemCreator.getItems());
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





    public static class DialogSelectServerController implements Initializable {

        @FXML
        JFXButton btn_close_dialog_select_server;

        @FXML
        VBox layout_list_dialog_select_server;

        @Override
        public void initialize(URL location, ResourceBundle resources) {

        }

    }


    public void setOnConfirmListener (onConfirmListener onConfirmListener) {
        this.onConfirmListener = onConfirmListener;
    }
    public interface onConfirmListener {
        void onConfirmed (TestPoint testPoint);
    }
}
