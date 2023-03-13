package sagalabsmanagerclient;

import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.resources.models.ResourceGroup;

import javax.xml.transform.Result;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {

    // Database credentials
    private static final String DB_URL = "jdbc:mysql://sagadb.sagalabs.dk:42069/sagadb";
    private static final String dbUsername = "sagalabs-manager";
    static String dbPassword = AzureMethods.getKeyVaultSecret("sagalabs-manager-SQL-pw");
    public static Connection conn;

    static {
        try {
            conn = DriverManager.getConnection(DB_URL, dbUsername, dbPassword);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean login() throws SQLException {
            // Open a connection
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL, dbUsername, dbPassword);
            if (conn != null) {
                return true;
            }
            return false;
    }
    public static ArrayList<MachinesVM> getMachines(String resourceGroup) throws SQLException {
        ArrayList<MachinesVM> machinesVMs = new ArrayList<MachinesVM>();
        String sql;
        if(resourceGroup.equals("ALL")) {
            sql = "SELECT * FROM sagadb.vm";
        }
        else {
            sql = "SELECT * FROM sagadb.vm WHERE resource_group = '"+ resourceGroup + "'";
        }
        ResultSet resultSet = executeSql(sql);

        ResultSetMetaData metaData = resultSet.getMetaData();
        while (resultSet.next()) {
            machinesVMs.add(new MachinesVM(
                    resultSet.getObject("id").toString(),
                    resultSet.getObject("vm_name").toString(),
                    resultSet.getObject("ostype").toString(),
                    resultSet.getObject("powerstate").toString().substring(11)));
        }
        return machinesVMs;
    }
    public static ResultSet executeSql(String sql) throws SQLException {
        Statement statement = conn.createStatement();
        return statement.executeQuery(sql);
    }
}
