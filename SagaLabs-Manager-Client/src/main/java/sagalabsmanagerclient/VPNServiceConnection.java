package sagalabsmanagerclient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class VPNServiceConnection {
    //static variable to hold VPN user data for all the VPN servers
    public static ArrayList<JsonObject> vpnUserJsonList = new ArrayList<>();

    //method to retrieve VPN user data from multiple VPN servers
    public static void getVPNUserInformation() throws SQLException {

        //execute SQL query to retrieve names and IP addresses of all running VPN servers
        ResultSet labsWithVPN = Database.executeSql("SELECT LabName, LabVPN FROM Labs WHERE vpnRunning = TRUE;");

        //loop through each VPN server and retrieve its VPN user data
        while (labsWithVPN.next()) {
            //retrieve the name and IP address of the VPN server
            String labName = labsWithVPN.getString("LabName");
            String vpnIp = labsWithVPN.getString("LabVPN");

            //retrieve the API credentials from Azure Key Vault and encode them in base64
            String apiCredentials = "sagavpn-api:" + AzureMethods.getKeyVaultSecret("sagavpn-api-key");
            String base64ApiCredentials = Base64.getEncoder().encodeToString(apiCredentials.getBytes(StandardCharsets.UTF_8));

            try {
                //create an HTTP connection to the VPN server's /api/users/list endpoint
                URL apiUrl = new URL("http://" + vpnIp + "/api/users/list");
                HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Authorization", "Basic " + base64ApiCredentials);

                //if the HTTP response code is not 200, throw an exception
                if (connection.getResponseCode() != 200) {
                    throw new RuntimeException("Failed : HTTP error code : " + connection.getResponseCode());
                }

                //read the response from the HTTP connection
                BufferedReader responseReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String responseLine;
                while ((responseLine = responseReader.readLine()) != null) {
                    responseBuilder.append(responseLine);
                }
                connection.disconnect();

                //parse the JSON data representing the VPN user data
                String vpnUserJson = responseBuilder.toString();
                JsonParser jsonParser = new JsonParser();
                JsonArray vpnUserJsonArray = jsonParser.parse(vpnUserJson).getAsJsonArray();

                //add the lab name and the VPN user data to a JSON object
                JsonObject labVpnUsers = new JsonObject();
                labVpnUsers.addProperty("labName", labName);
                labVpnUsers.add("vpnUsers", vpnUserJsonArray);

                //add the JSON object to the list of VPN user data for all the VPN servers
                vpnUserJsonList.add(labVpnUsers.getAsJsonObject());

            } catch (Exception e) {
                //if there was an error, print the stack trace
                e.printStackTrace();
            }
        }
    }
}
