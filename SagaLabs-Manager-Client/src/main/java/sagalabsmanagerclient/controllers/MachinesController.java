package sagalabsmanagerclient.controllers;

import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.containerservice.models.OSType;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import sagalabsmanagerclient.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.ArrayList;

public class MachinesController extends MenuController {
    @FXML protected TabPane tabPane;
    @FXML protected Tab allTab;
    @FXML protected TableView<MachinesVM> allTableView;

    public static ArrayList<MachinesTab> machinesTabs = new ArrayList<MachinesTab>();

    public void initialize() throws SQLException {
        initializeTabs();
    }

    private void initializeTabs() throws SQLException {
        // Creates tab for all
        machinesTabs.add(new MachinesTab(allTab, allTableView));
        System.out.println("HERE");
        selectTab(machinesTabs.get(0));

        for(ResourceGroup lab: AzureMethods.getAllLabs(AzureLogin.azure)) {
            // Creates tab
            Tab tab = new Tab();
            tab.setText(lab.name().substring(0, 10));
            // Creates tableview under tab
            TableView<MachinesVM> tableView = new TableView<MachinesVM>();
            tab.setContent(tableView);

            // Creates columns in tableview
            initializeColumns(tableView);
            // Adds tab to pane and tabs array
            tabPane.getTabs().add(tab);
            MachinesTab machinesTab = new MachinesTab(lab, tab, tableView);
            machinesTabs.add(machinesTab);
        }
        setTabSelectionAction();
    }

    private void setTabSelectionAction() {
        for(MachinesTab tab: machinesTabs) {
            tab.getTab().setOnSelectionChanged(e -> {
                try {
                    selectTab(tab);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            });
        }
    }

    private void initializeColumns(TableView<MachinesVM> tableView) {
        TableColumn<MachinesVM, CheckBox> selectColumn = new TableColumn<MachinesVM, CheckBox>();
        selectColumn.setPrefWidth(50.0);
        selectColumn.setCellValueFactory(new PropertyValueFactory<>("select"));

        TableColumn<MachinesVM, String> idColumn = new TableColumn<MachinesVM, String>("ID");
        idColumn.setPrefWidth(400.0);
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<MachinesVM, String> vmColumn = new TableColumn<MachinesVM, String>("Machine Name");
        vmColumn.setPrefWidth(450.0);
        vmColumn.setCellValueFactory(new PropertyValueFactory<>("vmName"));

        TableColumn<MachinesVM, OSType> osColumn = new TableColumn<MachinesVM, OSType>("OS");
        osColumn.setPrefWidth(100.0);
        osColumn.setCellValueFactory(new PropertyValueFactory<>("os"));

        TableColumn<MachinesVM, String> stateColumn = new TableColumn<MachinesVM, String>("State");
        stateColumn.setPrefWidth(99.0);
        stateColumn.setCellValueFactory(new PropertyValueFactory<>("state"));

        tableView.getColumns().add(selectColumn);
        tableView.getColumns().add(idColumn);
        tableView.getColumns().add(vmColumn);
        tableView.getColumns().add(osColumn);
        tableView.getColumns().add(stateColumn);
    }

    private void selectTab(MachinesTab machinesTab) throws SQLException {
        if(machinesTab.getTableView().getItems().isEmpty()) {
            String resourceGroupName;
            try {
                resourceGroupName = machinesTab.resourceGroup.name();
            }
            catch (NullPointerException e) {
                resourceGroupName = "ALL";
            }
            for (MachinesVM machinesVM : Database.getMachines(resourceGroupName)) {
                machinesTab.getTableView().getItems().add(machinesVM);
            }
        }
    }
    private void showAllMachines() {
        for(ResourceGroup resourceGroup: AzureMethods.getAllLabs(AzureLogin.azure)) {
            for(VirtualMachine vm: AzureMethods.getVMsInLab(resourceGroup)) {
                String powerState;
                if(vm.powerState().toString() == "PowerState/deallocated") {
                    powerState = "OFF";
                }
                else {
                    powerState = "ON";
                }
                allTableView.getItems().add(new MachinesVM(vm.vmId(), vm.name(), vm.osType().toString(), powerState.substring(0, 10)));
            }
        }
    }

    // METHOD TO RETURN VMs WITH CHECKMARK. Currently returns MachinesVM objects.
    private ArrayList<MachinesVM> getSelectedVMs() {
        MachinesTab machinesTab = machinesTabs.get(0); // By default all tab
        ArrayList<MachinesVM> machineList = new ArrayList<MachinesVM>();
        for(MachinesTab tab: machinesTabs) {
            if(tab.getTab().isSelected()) {
                machinesTab = tab;
                break;
            }
        }
        for (MachinesVM vm :machinesTab.getTableView().getItems()) {
            if(vm.getSelected()) {
                machineList.add(vm);
            }
        }
        return machineList;
    }

}
