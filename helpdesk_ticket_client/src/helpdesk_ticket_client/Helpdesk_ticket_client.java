/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package helpdesk_ticket_client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Scanner;

/**
 *
 * @author afoud
 * Ticket URL Located at:
 * https://raw.githubusercontent.com/AntoineFoudy/import-tickets-for-adv-programming-CA/main/tickets.txt
 */
public class Helpdesk_ticket_client {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Server's IP Address
        String IpAddress = "localhost";
        // Server's port number
        int PORT = 1818;

        try (Socket socket = new Socket(IpAddress, PORT);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                Scanner userInput = new Scanner(System.in);
                ) {

            // User inputted Message
            String clientMessage;
            // Server's Message
            String serverMessage;

            while (true) {
                clientMessage = userInput.nextLine().trim();

                // Skips current loop iteration if client enters nothing
                if (clientMessage.isEmpty()) {
                    continue;
                }
                
                // Imported command handaling
                if(clientMessage.trim().toLowerCase().startsWith("import;")) {
                    String url = clientMessage.substring("import;".length()).trim();
                    importTickets(url, in, out);
                    continue;
                }

                // Sends message to Server and reads response
                out.println(clientMessage);
                serverMessage = in.readLine();
                System.out.println(serverMessage);

                // Ends loop if client sends "STOP" and server responds with "Terminate
                if (serverMessage.equals("TERMINATE")) {
                    break;
                }
            }

        } catch (RuntimeException | IOException e) {
            System.out.println("Error: " + e.toString());
        } catch(Exception e) {
            System.out.println("Error: " + e.toString());
        }
    }
    
    
    // Method for handling the import; command
    private static void importTickets(String ticketURL, BufferedReader in, PrintWriter out) {
        int imported = 0;
        int skipped = 0;
        HttpURLConnection conn = null;
        
        // Try to connect to the provided URL
        try {
            URL url = new URI(ticketURL).toURL();
            
            // HttpURLConnection to connect to the server hosting the .txt file
            conn = (HttpURLConnection) url.openConnection();
            
            conn.setRequestMethod("GET");
            
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            
            // Check if status code is not OK
            if (conn.getResponseCode() != 200) {
                throw new IOException("Could not import tickets: HTTP Status " +conn.getResponseCode());
            } else {
                // Read the body
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {

                    String name = null;
                    String line;
                    int lineCount = 0; // Allow system to display which line is causing an issue/skipped
                    String[] command;
                    
                    // Start going throw each line
                    while ((line = br.readLine()) != null) {
                        lineCount += 1;

                        // Get the Student name from the start of the .txt file
                        if (lineCount == 1) {
                            // Check if line 1 is formatted correctly
                            if(!line.trim().startsWith("student=")) {
                                throw new IOException("First line must be 'student=<name>'");
                            }
                            
                            name = line.trim().substring("student=".length()).trim();

                            // check if name is null
                            if (name.isEmpty()) {
                                throw new IOException("Could not find a Student name");
                            }
                            continue;
                        }
                        
                        // Check if line is empty
                        if(line.trim().isEmpty()) {
                            continue;
                        }
                        
                        // Validate lines to make sure they are not empty and contains the correct amount of fields 3 per line
                        command = line.split(";", -1);
                        
                        
                        // Check if there are 3 fields
                        if (command.length != 3) {
                            skipped += 1;
                            System.out.println("Line: " + lineCount + " was skipped because it is does not contain the correct number of fields: 3");
                            continue;
                        }
                        
                        
                        // trim any leading or trailing blank spaces
                        for (int i = 0; i < 3; i++) {
                            command[i] = command[i].trim();
                        }

                        // Check that non of the fields are empty
                        if(emptyFieldCheck(command)) {
                            skipped += 1;
                            System.out.println("Line: " + lineCount + " was skipped because one of the fields is blank");
                            continue;
                        }
                        
                        // Create the command to be sent to the server
                        String sendCommand = "open; " + command[0] + "; " + name + "; " + command[1] + "; " + command[2];
                        
                        // Sending and reciveing messages to the server
                        out.println(sendCommand);
                        System.out.println(sendCommand);
                        
                        String serverResponse = in.readLine();
                        
                        // Check connection to server is still active
                        if(serverResponse == null) {
                            System.out.println("Server connection is closed");
                            break;
                        }
                        
                        
                        // Use the server to validate commands that were sent but were invalid in the eyes of the entire system
                        if(serverResponse.trim().startsWith("InvalidCommandException")) {
                            skipped += 1;
                        }
                        else {
                            imported += 1;
                        }
                        
                        System.out.println(serverResponse);
                        

                    }
                }
            }

        } catch (MalformedURLException e) {
            System.out.println("MalformedURLException: " + e.toString());
        } catch (URISyntaxException e) {
            System.out.println("URISyntaxException: " + e.toString());
        } catch (IOException e) {
            System.out.println("IOException: " + e.toString());
        } catch (IllegalArgumentException e) {
            System.out.println("URL provided is invalid: " + ticketURL);
        } catch (Exception e) {
            System.out.println("Error : " + e.toString());
        } finally {
            if(conn != null) {
                conn.disconnect();
            }
            
            System.out.println("Imported: " + imported + "; Skipped: " + skipped);
        }
    }

    private static boolean emptyFieldCheck(String[] command) {
        for (String s : command) {
            if (s.isBlank()) {
                return true;
            }
        }
        return false;
    }
}
