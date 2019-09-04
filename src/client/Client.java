package client;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Optional;

public class Client extends Application {

    public static final String TITLE = "Order a taxi";

    public static Stage stage;

    @Override
    public void start(Stage stage) throws Exception {
        Client.stage = stage;
        Client.setScene(new OrderScene(new BorderPane()));
        stage.setTitle(Client.TITLE);
        stage.show();
    }

    public static void setScene(TaxiScene scene) {
        stage.setScene(scene);
    }

}

abstract class TaxiScene extends Scene {

    public VBox order;
    public FlowPane header;

    public TextField street;
    public TextField number;
    public TextField phone;

    public Label title;
    public Label lbStreet;
    public Label lbNumber;
    public Label lbPhone;

    public Button btn;

    Socket socket = null;
    DataOutputStream out = null;
    DataInputStream input = null;

    public TaxiScene(Parent root, double width, double height, String title) {
        super(root, width, height);

        this.title = new Label(title);
        this.title.setFont(new Font(36));

        this.header = new FlowPane();
        this.header.setAlignment(Pos.CENTER);

        this.header.getChildren().add(this.title);

        this.order = new VBox();

        this.order.setPadding(new Insets(100,300,100,300));
        this.order.setSpacing(10);

        this.lbStreet = new Label("Street");
        this.lbNumber = new Label("Street no.");
        this.lbPhone = new Label("Phone");

        this.street = new TextField();
        this.number = new TextField();
        this.phone = new TextField();

        this.order.getChildren().addAll(
                this.lbStreet, this.street,
                this.lbNumber, this.number,
                this.lbPhone, this.phone
        );

        this.btn = new Button();

        this.order.getChildren().add(btn);

        ((BorderPane) this.getRoot()).setTop(header);
        ((BorderPane) this.getRoot()).setCenter(order);

        try {
            socket = new Socket("127.0.0.1", 7999);

            out = new DataOutputStream(socket.getOutputStream());
            input = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Connected");
    }

    public static Optional<ButtonType> showMessage(String title, String header, String text, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(text);

        return alert.showAndWait();
    }
}

class OrderScene extends TaxiScene {

    public OrderScene(Parent root) {
        super(root, 800, 600, "Order a taxi");

        this.btn.setText("Order");

        this.street.setDisable(false);
        this.number.setDisable(false);
        this.phone.setDisable(false);

        this.btn.setOnMouseClicked(event -> {
            String line =
                    "ADD "
                    + number.getText().replace(" ", "") + " "
                    + phone.getText().replace(" ", "") + " "
                    + street.getText();


            try {
                out.writeUTF(line);

                while(input.available()>0) {
                    String response = input.readUTF();

                    Client.setScene(new CancelScene(new BorderPane(), response));

                }
            } catch (Exception e) {
                showMessage("Error", "Couldn't make a order", "Check your input", Alert.AlertType.ERROR);
            }
        });

    }
}

class CancelScene extends TaxiScene {

    int id;
    String[] info;

    public CancelScene(Parent root, String info) {
        super(root, 800, 600,"Your ride will be here shortly");

        this.info = info.split(" ", 4);

        this.id = Integer.parseInt(this.info[0]);

        this.btn.setText("Cancel");
        this.street.setText(this.info[3]);
        this.number.setText(this.info[1]);
        this.phone.setText(this.info[2]);

        this.street.setDisable(true);
        this.number.setDisable(true);
        this.phone.setDisable(true);

        this.btn.setOnMouseClicked(event -> {
            String line = "DELETE " + id;
            try {
                out.writeUTF(line);
                Client.setScene(new OrderScene(new BorderPane()));
            } catch (Exception e) {
                showMessage("Error", "Couldn't delete your order", "Try again later", Alert.AlertType.ERROR);
            }
        });

    }
}