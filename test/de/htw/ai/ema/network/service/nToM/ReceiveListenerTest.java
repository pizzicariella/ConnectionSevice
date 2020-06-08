package de.htw.ai.ema.network.service.nToM;

import org.junit.Test;
import java.io.IOException;

public class ReceiveListenerTest {

    /**
     * Server receiving different messages from all three clients. No assertion but all three messages should be
     * printed to console. "One: I received something! - " + message
     */
    @Test
    public void testOnReceive() throws IOException, InterruptedException {
        int port = 7777;

        NToMConnectionHandler nToM1 = new NToMConnectionHandler("One");
        NToMConnectionHandler nToM2 = new NToMConnectionHandler("Two");
        NToMConnectionHandler nToM3 = new NToMConnectionHandler("Three");
        NToMConnectionHandler nToM4 = new NToMConnectionHandler("Four");

        nToM1.addReceiveListener((message) ->
                System.out.println(nToM1.getName()+": I received something! - "
                        +((String) message)));

        TCPConnector channelOne = new TCPConnector("One (sever)", port);
        TCPConnector channelTwo = new TCPConnector("client two", port);
        TCPConnector channelThree = new TCPConnector("client three", port);
        TCPConnector channelFour = new TCPConnector("client four", port);

        channelOne.accept();
        channelTwo.connect();
        channelThree.connect();
        channelFour.connect();

        channelOne.waitForConnection();
        channelTwo.waitForConnection();
        channelThree.waitForConnection();
        channelFour.waitForConnection();

        for(int i = 0; i<channelOne.getIns().size(); i++){
            nToM1.handleConnection(channelOne.getIns().get(i), channelOne.getOuts().get(i));
        }
        nToM2.handleConnection(channelTwo.getIns().get(0), channelTwo.getOuts().get(0));
        nToM3.handleConnection(channelThree.getIns().get(0), channelThree.getOuts().get(0));
        nToM4.handleConnection(channelFour.getIns().get(0), channelFour.getOuts().get(0));

        String message1 = "Client 1 saying hi";
        String message2 = "Client 2 saying hello";
        String message3 = "Greetings from client 3";

        nToM2.sendMessage(message1);
        nToM2.sendMessage(message2);
        nToM3.sendMessage(message3);

        Thread.sleep(500);
    }
}
