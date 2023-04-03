package sagalabsmanagerclient;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import com.google.gson.*;

public class VPNServiceConnection {
    //static variable to hold VPN user data for all the VPN servers
    private ArrayList<JsonObject> vpnUserJsonList = new ArrayList<>();
    private final AzureMethods azureMethods = new AzureMethods();

    //method to retrieve VPN user data from multiple VPN servers
    public void getVPNUserInformation() throws SQLException {

        vpnUserJsonList = new ArrayList<>(); // create a new ArrayList to reset the list

        //execute SQL query to retrieve names and IP addresses of all running VPN servers
        ResultSet labsWithVPN = Database.executeSql("SELECT LabName, LabVPN FROM Labs WHERE vpnRunning = TRUE;");

        //loop through each VPN server and retrieve its VPN user data
        while (labsWithVPN.next()) {
            //retrieve the name and IP address of the VPN server
            String labName = labsWithVPN.getString("LabName");
            String vpnIp = labsWithVPN.getString("LabVPN");

            //retrieve the API credentials from Azure Key Vault and encode them in base64
            String apiCredentials = "sagavpn-api:" + azureMethods.getKeyVaultSecret("sagavpn-api-key");
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

                //add the lab name and the VPN user data array to a JSON object
                JsonObject labVpnUsers = new JsonObject();
                labVpnUsers.addProperty("labName", labName);
                JsonArray vpnUserJsonArrayWithIp = new JsonArray();
                for (JsonElement vpnUserJsonElement : vpnUserJsonArray) {
                    JsonObject vpnUserJsonObject = vpnUserJsonElement.getAsJsonObject();
                    vpnUserJsonObject.addProperty("vpnIp", vpnIp);
                    vpnUserJsonObject.addProperty("labName", labName);
                    vpnUserJsonArrayWithIp.add(vpnUserJsonObject);
                }
                labVpnUsers.add("vpnUsers", vpnUserJsonArrayWithIp);

                //add the JSON object to the list of VPN user data for all the VPN servers
                vpnUserJsonList.add(labVpnUsers);



            } catch (Exception e) {
                //if there was an error, print the stack trace
                e.printStackTrace();
            }
        }
    }
    public void createUser(String vpnIp, String username) throws IOException {
        String apiUrl = "http://" + vpnIp + "/api/user/create";
        System.out.println(sendPostRequestWithUsername(apiUrl, username));
    }

    public String downloadConfig(String vpnIp, String username) throws IOException {
        String apiUrl = "http://" + vpnIp + "/api/user/config/show";
        String configFileContent = sendPostRequestWithUsername(apiUrl, username);
        return saveConfigFile(username, configFileContent);
    }


    public void revokeCertificate(String vpnIp, String username) throws IOException {
        String apiUrl = "http://" + vpnIp + "/api/user/revoke";
        sendPostRequestWithUsername(apiUrl, username);
        System.out.println(sendPostRequestWithUsername(apiUrl, username));
    }

    public void deleteUser(String vpnIp, String username) throws IOException {
        String apiUrl = "http://" + vpnIp + "/api/user/delete";
        sendPostRequestWithUsername(apiUrl, username);
        System.out.println(sendPostRequestWithUsername(apiUrl, username));
    }

    public void rotateCertificate(String vpnIp, String username) throws IOException {
        String apiUrl = "http://" + vpnIp + "/api/user/rotate";
        sendPostRequestWithUsername(apiUrl, username);
        System.out.println(sendPostRequestWithUsername(apiUrl, username));
    }

    public void unrevokeCertificate(String vpnIp, String username) throws IOException {
        String apiUrl = "http://" + vpnIp + "/api/user/unrevoke";
        sendPostRequestWithUsername(apiUrl, username);
        System.out.println(sendPostRequestWithUsername(apiUrl, username));
    }

    private String sendPostRequestWithUsername(String apiUrl, String username) throws IOException {
        // Retrieve and encode the API credentials
        String apiCredentials = "sagavpn-api:" + azureMethods.getKeyVaultSecret("sagavpn-api-key");
        String base64ApiCredentials = Base64.getEncoder().encodeToString(apiCredentials.getBytes(StandardCharsets.UTF_8));

        // Create an HTTP connection to the specified API URL
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Authorization", "Basic " + base64ApiCredentials);
        connection.setDoOutput(true);

        // Send the username as the request body
        String requestBody = "username=" + username;
        OutputStream os = connection.getOutputStream();
        os.write(requestBody.getBytes(StandardCharsets.UTF_8));
        os.flush();
        os.close();

        // Check the HTTP response code
        if (connection.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + connection.getResponseCode());
        }

        // Read the response from the HTTP connection
        ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
        InputStream is = connection.getInputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            responseStream.write(buffer, 0, bytesRead);
        }
        is.close();
        connection.disconnect();

        return responseStream.toString(StandardCharsets.UTF_8);
    }

    private String saveConfigFile(String username, String configFileContent) throws IOException {
        String userHome = System.getProperty("user.home");
        String downloadsPath;
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("win")) {
            downloadsPath = userHome + "\\Downloads\\" + username + ".ovpn";
        } else if (osName.contains("mac")) {
            downloadsPath = userHome + "/Downloads/" + username + ".ovpn";
        } else if (osName.contains("linux") || osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            downloadsPath = userHome + "/Downloads/" + username + ".ovpn";
        } else {
            throw new UnsupportedOperationException("Unsupported operating system: " + osName);
        }

        File outputFile = new File(downloadsPath);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            writer.write(configFileContent);
        }
        return outputFile.getAbsolutePath();
    }



    public ArrayList<JsonObject> getVpnUserJsonList() {
        return vpnUserJsonList;
    }
}
