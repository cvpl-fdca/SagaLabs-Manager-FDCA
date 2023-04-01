package sagalabsmanagerclient.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sagalabsmanagerclient.AzureMethods;
import sagalabsmanagerclient.VMSnapshot;

import java.io.IOException;
import java.util.Objects;

public class CaptureLabPopUp extends Controller {

    public TextField resourceGroupNameTextField;
    public TextField vmNameTextField;
    public TextField imageDefinitionPrefixTextField;

    @FXML
    private void captureButtonClicked() {
        String resourceGroupName = resourceGroupNameTextField.getText();
        String vmName = vmNameTextField.getText();
        String imageDefinitionPrefix = imageDefinitionPrefixTextField.getText();

        if (resourceGroupName.isEmpty() || vmName.isEmpty() || imageDefinitionPrefix.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText("All fields are required!");
            alert.showAndWait();
            return;
        }

        VMSnapshot.takeGeneralizedSnapshot(resourceGroupName, vmName, imageDefinitionPrefix);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Snapshot taken and image version created!");
        alert.showAndWait();
    }

    public static void openCaptureLabPopup(String resourceGroupName) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(CaptureLabPopUp.class.getResource("/sagalabsmanagerclient/CaptureLabPopUp.fxml")));
        Scene scene = new Scene(root);

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.getIcons().add(new Image(Objects.requireNonNull(CaptureLabPopUp.class.getResourceAsStream("/sagalabsmanagerclient/Images/FDCA_icon.png"))));
        stage.showAndWait();
    }

}
