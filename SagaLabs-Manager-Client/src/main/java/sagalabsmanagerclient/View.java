package sagalabsmanagerclient;

public enum View {
    //ENUM types for opening fxml files with the ViewSwitcher function
    LOGIN("Login.fxml"),
    HOME("Home.fxml"),
    SQLSCENE("Database.fxml"),
    VPN("Vpn.fxml"),
    MACHINES("Machines.fxml"),
    CAPTURE_LAB_POPUP("CaptureLabPopUp.fxml");

    final private String fileName;

    View(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}

