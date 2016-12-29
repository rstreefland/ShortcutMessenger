package uk.co.streefland.rhys.finalyearproject.core;

import java.util.Date;

/**
 * Created by Rhys on 29/12/2016.
 */
public class Statistics {

    private int bytesSent;
    private int bytesReceived;
    private int messagesSent;
    private int messagesReceived;
    private long lastCommunication;

    public Statistics() {
        bytesSent = 0;
        bytesReceived = 0;
        messagesSent = 0;
        messagesReceived = 0;
    }

    public void updateSent(int bytesSent) {
        this.bytesSent = this.bytesSent + bytesSent;
        messagesSent++;
        setLastCommunication();
    }

    public void updateReceived(int bytesReceived) {
        this.bytesReceived = this.bytesReceived + bytesReceived;
        messagesReceived++;
        setLastCommunication();
    }

    private void setLastCommunication() {
        lastCommunication = new Date().getTime();
    }

    public int getBytesSent() {
        return bytesSent;
    }

    public int getBytesReceived() {
        return bytesReceived;
    }

    public int getMessagesSent() {
        return messagesSent;
    }

    public int getMessagesReceived() {
        return messagesReceived;
    }

    public long getLastCommunication() {
        return lastCommunication;
    }
}
