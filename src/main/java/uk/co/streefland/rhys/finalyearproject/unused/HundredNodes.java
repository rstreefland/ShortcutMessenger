package uk.co.streefland.rhys.finalyearproject.unused;

import uk.co.streefland.rhys.finalyearproject.core.LocalNode;

import java.io.IOException;

class HundredNodes {
    public static void main(String[] args) throws IOException, InterruptedException {

        LocalNode localNodes[] = new LocalNode[100];

        for (int i = 0; i < 100; i++) {
            System.out.println("Creating node number: " + i);
           // localNodes[i] = new LocalNode(new KeyId(), 40000 + i);
        }

        // init first node
        localNodes[0].first();

        // bootstrap the rest
        for (int i = 1; i < 100; i++) {
            Thread.sleep(100);
            localNodes[i].bootstrap(localNodes[0].getNode());
        }

        Thread.sleep(60 * 1000);
        System.out.println(localNodes[48].getRoutingTable());
    }
}
