package uk.co.streefland.rhys.finalyearproject.irrelevant;

import java.io.Serializable;

/**
 * Created by Rhys on 06/07/2016.
 */
public class TestNode implements Serializable {

    String id;
    String ip;
    int port;

    public TestNode(String id) {
        this.id = id;
        this.ip =  "127.0.0.1";
        this.port = 8080;
    }

    @Override
    public String toString() {
        String output = "ID: " + id + "\n" + "IP: " + ip + "\n" + "PORT: " + port + "\n";
        return output;
    }
}
