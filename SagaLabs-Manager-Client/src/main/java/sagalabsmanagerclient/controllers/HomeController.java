package sagalabsmanagerclient.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import sagalabsmanagerclient.AzureMethods;
import sagalabsmanagerclient.Database;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HomeController extends MenuController {

    @FXML
    private AnchorPane anchorHome;
    private final AzureMethods azureMethods = new AzureMethods();

    public void initialize() throws SQLException {
        VBox vbox = createLabsVBox();
        anchorHome.getChildren().add(vbox);


        super.initialize();
    }

    public void refresh() throws SQLException {
        anchorHome.getChildren().clear();
        anchorHome.getChildren().add(createLabsVBox());
    }

    private VBox createLabsVBox() throws SQLException {
        // Execute a SQL query to retrieve information about labs
        ResultSet labs = Database.executeSql("SELECT LabName, vpnRunning, VmCount, LabVPN FROM Labs");
        // Create a VBox to hold all the lab information boxes
        VBox vbox = new VBox();
        vbox.setSpacing(10);
        // Loop through the labs ResultSet
        while (labs.next()) {
            // Create an HBox for each lab containing lab information and controls
            HBox hbox = createLabHBox(labs);
            // Add the created HBox to the VBox
            vbox.getChildren().add(hbox);
        }
        // Return the created VBox containing lab information boxes
        return vbox;
    }


    private HBox createLabHBox(ResultSet labs) throws SQLException {
        // Retrieve lab information from the ResultSet
        String labName = labs.getString("LabName");
        String vpnIp = labs.getString("LabVPN");
        boolean vpnRunning = labs.getBoolean("vpnRunning");
        int vmCount = labs.getInt("VmCount");
        // Create an HBox to hold all the lab information elements
        HBox hbox = new HBox();
        hbox.setSpacing(10);
        hbox.setStyle("-fx-background-color: gray; -fx-border-color: #333333; -fx-border-width: 2px; -fx-border-style: solid;");
        // Create a colored Rectangle to represent the VPN status
        Rectangle box = createVpnStatusRectangle(vpnRunning);
        // Create a VBox to hold all the lab labels
        VBox labelBox = createLabelsVBox(labName, vpnIp, vpnRunning, vmCount);
        // Create buttons to turn on/off all VMs for this lab
        Button turnOnButton = createTurnOnButton(labName);
        Button turnOffButton = createTurnOffButton(labName);
        // Add all the elements to the HBox
        hbox.getChildren().addAll(box, labelBox, turnOnButton, turnOffButton);

        // Create a button to show the popup
        Button showPopupButton = new Button("Capture Lab");
        showPopupButton.setOnAction(event -> {
            try {
                CaptureLabPopUp.openCaptureLabPopup(labName); // Pass the resource group name to the method
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });


        // Add the button to the HBox
        hbox.getChildren().add(showPopupButton);

        // Return the created HBox containing lab information and controls
        return hbox;
    }




    private Rectangle createVpnStatusRectangle(boolean vpnRunning) {
        return new Rectangle(100, 100, vpnRunning ? Color.GREEN : Color.BLUE);
    }

    private VBox createLabelsVBox(String labName, String vpnIp, boolean vpnRunning, int vmCount) {
        // Create a VBox to hold all the lab labels
        VBox labelBox = new VBox();
        labelBox.setSpacing(5);
        // Create a label for the lab name and set its style
        Label nameLabel = new Label(labName);
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        // Create a label for the lab name and set its style
        Label vpnIpLabel = new Label("IP: " + vpnIp);
        nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        // Create a label for the number of VMs and set its style
        Label vmLabel = new Label("VM's: " + vmCount);
        vmLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        // Create a label for the VPN status and set its color
        Label vpnStatusLabel = new Label(vpnRunning ? "VPN: Running" : "VPN: Down");
        vpnStatusLabel.setTextFill(vpnRunning ? Color.GREEN : Color.RED);
        // Add all the labels to the VBox
        labelBox.getChildren().addAll(nameLabel,vpnIpLabel, vpnStatusLabel, vmLabel);
        // Return the created VBox containing lab labels
        return labelBox;
    }


    private Button createTurnOnButton(String labName) {
        // Create a button to turn on all machines in the lab
        Button turnOnButton = new Button("Turn On all machines");
        // Set up the event handler for the button
        turnOnButton.setOnAction(event -> {
            // Create a Runnable task to turn on all VMs in the specified lab
            Runnable task = () -> azureMethods.turnOnInLab(labName);
            // Start a new thread to execute the task
            Thread thread = new Thread(task);
            thread.start();
        });
        // Return the created button with the event handler
        return turnOnButton;
    }

    private Button createTurnOffButton(String labName) {
        // Create a button to turn off all machines in the lab
        Button turnOffButton = new Button("Turn Off all machines");
        // Set up the event handler for the button
        turnOffButton.setOnAction(event -> {
            // Create a Runnable task to turn off all VMs in the specified lab
            Runnable task = () -> azureMethods.turnOffVMsInLab(labName);
            // Start a new thread to execute the task
            Thread thread = new Thread(task);
            thread.start();
        });
        // Return the created button with the event handler
        return turnOffButton;
    }






}
