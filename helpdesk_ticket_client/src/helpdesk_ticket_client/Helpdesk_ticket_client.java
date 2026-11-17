/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package helpdesk_ticket_client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 *
 * @author afoud
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
        
        try(Socket socket = new Socket(IpAddress, PORT);
                BufferedReader in = new BufferedReader( new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
                Scanner userInput = new Scanner(System.in);
                ) {
            
            // User inputted Message
            String clientMessage;
            // Server's Message
            String serverMessage;
            
            while(true) {
                clientMessage = userInput.nextLine().trim();
                
                // Skips current loop iteration if client enters nothing
                if(clientMessage.isEmpty()) {
                    continue;
                }
                
                // Sends message to Server and reads response
                out.println(clientMessage);
                serverMessage = in.readLine();
                System.out.println(serverMessage);
                
                
                // Ends loop if client sends "STOP" and server responds with "Terminate
                if(serverMessage.equals("Terminate")) {
                    break;
                }
            }
            
        }
        catch(Exception e) {
            System.out.println("Error: " + e);
        }
    }
    
}
