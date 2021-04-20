package com.dosse.speedtest;

import com.dosse.speedtest.core.serverSelector.TestPoint;
import com.jfoenix.controls.JFXRippler;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import com.dosse.speedtest.core.serverSelector.TestPoint;

import java.util.ArrayList;

public class ServerItemCreator {

    private ArrayList<TestPoint> arrayList = new ArrayList<>();

    private onItemSelectListener onItemSelectListener;

    public ServerItemCreator(ArrayList<TestPoint> arrayList) {
        this.arrayList = arrayList;
    }

    public ArrayList<Node> getItems () {
        ArrayList<Node> list = new ArrayList<>();
        for (int i = 0; i < arrayList.size(); i ++) {
            if (arrayList.get(i).getPing() != -1) {
                list.add(getItem(arrayList.get(i)));
            }
        }
        return list;
    }


    public Node getItem (TestPoint testPoint) {
        Label label = new Label(testPoint.getName());

        VBox vBox = new VBox(label);
        vBox.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 6");
        vBox.setMargin(label,new Insets(12));
        vBox.setEffect(new DropShadow());

        JFXRippler rippler = new JFXRippler();
        rippler.setControl(vBox);
        rippler.setRipplerFill(Color.valueOf("#212121"));
        rippler.setMaskType(JFXRippler.RipplerMask.FIT);

        VBox vBoxRoot = new VBox(rippler);
        vBoxRoot.setMargin(rippler,new Insets(10));

        vBoxRoot.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                onItemSelectListener.onItemSelected(testPoint);
            }
        });

        return vBoxRoot;
    }



    public void setOnItemSelectListener (onItemSelectListener onItemSelectListener) {
        this.onItemSelectListener = onItemSelectListener;
    }
    public interface onItemSelectListener {
        void onItemSelected (TestPoint testPoint);
    }
}
