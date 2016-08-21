package uk.co.streefland.rhys.finalyearproject.irrelevant;

import java.io.*;
import java.net.*;

public class ChatServer {

    public static void main(String[] args) {
        try
        {
            TestNode node = new TestNode("testrhysnode");
            ServerSocket serverSocket = new ServerSocket(9999);
            Socket socket = serverSocket.accept();
            try
            {
                ObjectOutputStream objectOutput = new ObjectOutputStream(socket.getOutputStream());
                objectOutput.writeObject(node);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}