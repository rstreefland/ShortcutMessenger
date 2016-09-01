package uk.co.streefland.rhys.finalyearproject.irrelevant;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Peer {
    public static void main(String[] args)
    {
        try {
            Socket socket = new Socket("localhost",9999);
            try {
                ObjectInputStream objectInput = new ObjectInputStream(socket.getInputStream());
                try {
                    Object object = objectInput.readObject();
                    TestNode node = (TestNode) object;
                    System.out.println(node.toString());
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}