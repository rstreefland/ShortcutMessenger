package uk.co.streefland.rhys.finalyearproject.test;

import uk.co.streefland.rhys.finalyearproject.message.*;
import uk.co.streefland.rhys.finalyearproject.message.connect.ConnectMessage;
import uk.co.streefland.rhys.finalyearproject.message.user.StoreUserMessage;
import uk.co.streefland.rhys.finalyearproject.message.user.VerifyUserMessage;
import uk.co.streefland.rhys.finalyearproject.message.user.VerifyUserReplyMessage;

/**
 * Created by Rhys on 30/09/2016.
 */
public class Codes {

    public static void main (String[] args) {
        System.out.println("AcknowledgeMessage: " + AcknowledgeMessage.CODE);
        System.out.println("VerifyUserMessage: " + VerifyUserMessage.CODE);
        System.out.println("VerifyUserReplyMessage: " + VerifyUserReplyMessage.CODE);
        System.out.println("ConnectMessage: " + ConnectMessage.CODE);
        System.out.println("FindNodeMessage: " + FindNodeMessage.CODE);
        System.out.println("FindNodeReplyMessage:" + FindNodeReplyMessage.CODE);
        System.out.println("StoreUserMessage: " + StoreUserMessage.CODE);
        System.out.println("TextMessage: " + TextMessage.CODE);
    }
}
