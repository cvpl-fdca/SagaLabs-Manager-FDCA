package sagalabsmanagerclient;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sagalabsmanagerclient.controllers.Controller;

import java.io.IOException;
import java.util.Objects;

public class ViewSwitcher {

    private Controller controller;
    private Scene scene;

    public ViewSwitcher(Stage stage) throws IOException {
        //Creates the initial scene
        Scene scene = new Scene(new Pane());

        setScene(scene);
        switchView(View.LOGIN);

        Image icon = new Image(Objects.requireNonNull(getClass().getResource("Images/FDCA_icon.png")).openStream());
        stage.getIcons().add(icon);
        stage.setMaximized(true);
        stage.setScene(scene);
        stage.setTitle("SagaLabs-Manager");
        stage.show();
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }
    public void switchView(View view) {
        try {
            if(controller != null) {
                controller.closeRefreshingThreads();
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource(view.getFileName()));
            Parent root = loader.load();
            controller = loader.getController();
            controller.getViewSimpleObjectProperty().addListener((observable, oldValue, newValue) -> switchView(newValue));
            scene.setRoot(root);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    public void openPopup(String fxmlFilePath, Class<? extends Controller> controllerClass) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFilePath));
            Parent root = loader.load();
            Controller controller = loader.getController();
            if (controllerClass.isInstance(controller)) {
                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();
            } else {
                throw new IllegalArgumentException("Invalid controller class for FXML file: " + fxmlFilePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void closeThreads() {
        controller.closeRefreshingThreads();
    }
}