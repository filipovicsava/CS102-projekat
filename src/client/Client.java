package client;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.awt.*;
import java.util.Scanner;

public class Client extends Application {

    public static final String TITLE = "Naruči taksi vožnju";

    public static Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Client.stage = stage;
        TaxiScene scene = new TaxiScene(new BorderPane());
        stage.setScene(scene);
        stage.setTitle(Client.TITLE);
        stage.show();
    }

}

class TaxiScene extends Scene {

    public TextField address;


    public TaxiScene(Parent root) {
        super(root, 800, 600);
    }
}