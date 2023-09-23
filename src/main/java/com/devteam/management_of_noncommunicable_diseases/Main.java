package com.devteam.management_of_noncommunicable_diseases;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class Main extends Application {
    double x,y = 0;
    @Override
    public void start(Stage stage) throws IOException, Exception {
        Parent root = FXMLLoader.load(getClass().getResource("View/Dashboard.fxml"));
        stage.initStyle(StageStyle.TRANSPARENT);

        root.setOnMousePressed(event -> {
                x = event.getSceneX();
                y = event.getSceneY();
        });

        root.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - x);
            stage.setY(event.getScreenY() - y);
        });

        stage.setScene(new Scene(root));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}