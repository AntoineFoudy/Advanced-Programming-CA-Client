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
import java.net.URLConnection;
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

                // Sends message to Server and reads response
                out.println(clientMessage);
                serverMessage = in.readLine();
                System.out.println(serverMessage);

                // Ends loop if client sends "STOP" and server responds with "Terminate
                if (serverMessage.equals("TERMINATE")) {
                    break;
                }
            }

        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }

    private static void importTickets(String ticketURL, BufferedReader in, PrintWriter out) {
        int imported = 0;
        int skipped = 0;
        HttpURLConnection conn = null;

        try {
            URL url = new URI(ticketURL).toURL();

            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            if (conn.getResponseCode() != 200) {
                throw new IOException("Could not import tickets: HTTP Status " +conn.getResponseCode());
            } else {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {

                    String name = null;
                    String line;
                    int lineCount = 0;
                    String[] command;

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
                            imported -= 1;
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
                        
                        
                        

                    }
                }
            }

        } catch (MalformedURLException e) {
            System.out.println("MalformedURLException: " + e.getMessage());
        } catch (URISyntaxException e) {
            System.out.println("URISyntaxException: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } finally {
            if(conn != null) {
                conn.disconnect();
            }
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
