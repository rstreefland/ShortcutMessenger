package uk.co.streefland.rhys.finalyearproject.unused;

import uk.co.streefland.rhys.finalyearproject.message.AcknowledgeMessage;
import uk.co.streefland.rhys.finalyearproject.message.content.TextMessage;
import uk.co.streefland.rhys.finalyearproject.message.node.ConnectMessage;
import uk.co.streefland.rhys.finalyearproject.message.node.FindNodeMessage;
import uk.co.streefland.rhys.finalyearproject.message.node.FindNodeMessageReply;
import uk.co.streefland.rhys.finalyearproject.message.user.StoreUserMessage;
import uk.co.streefland.rhys.finalyearproject.message.user.VerifyUserMessage;
import uk.co.streefland.rhys.finalyearproject.message.user.VerifyUserMessageReply;

/**
 * Created by Rhys on 30/09/2016.
 */
class Codes {

    public static void main(String[] args) {
        System.out.println("AcknowledgeMessage: " + AcknowledgeMessage.CODE);
        System.out.println("ConnectMessage: " + ConnectMessage.CODE);
        System.out.println("FindNodeMessage: " + FindNodeMessage.CODE);
        System.out.println("FindNodeMessageReply:" + FindNodeMessageReply.CODE);
        System.out.println("StoreUserMessage: " + StoreUserMessage.CODE);
        System.out.println("VerifyUserMessage: " + VerifyUserMessage.CODE);
        System.out.println("VerifyUserMessageReply: " + VerifyUserMessageReply.CODE);
        System.out.println("TextMessage: " + TextMessage.CODE);
    }
}
