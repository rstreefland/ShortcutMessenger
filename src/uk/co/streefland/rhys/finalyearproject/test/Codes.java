package uk.co.streefland.rhys.finalyearproject.test;

import uk.co.streefland.rhys.finalyearproject.message.*;

/**
 * Created by Rhys on 30/09/2016.
 */
public class Codes {

    public static void main (String[] args) {
        System.out.println("AcknowledgeMessage: " + AcknowledgeMessage.CODE);
        System.out.println("CheckUserMessage: " + CheckUserMessage.CODE);
        System.out.println("CheckUserReplyMessage: " + CheckUserReplyMessage.CODE);
        System.out.println("ConnectMessage: " + ConnectMessage.CODE);
        System.out.println("FindNodeMessage: " + FindNodeMessage.CODE);
        System.out.println("FindNodeReplyMessage:" + FindNodeReplyMessage.CODE);
        System.out.println("StoreUserMessage: " + StoreUserMessage.CODE);
        System.out.println("TextMessage: " + TextMessage.CODE);
    }
}
