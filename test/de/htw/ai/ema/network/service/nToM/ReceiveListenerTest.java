package de.htw.ai.ema.network.service.nToM;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ReceiveListenerTest {

    /**
     * This Test does not have an assertion. It passes if the following is printed to console:
     * "Two: I received something! - got it?"
      */

    @Test
    public void testOnReceive() throws IOException, InterruptedException {
        int port = 7777;

        NToMConnectionHandler nToM1 = new NToMConnectionHandler("One");
        NToMConnectionHandler nToM2 = new NToMConnectionHandler("Two");

        nToM2.addReceiveListener((message) ->
                System.out.println(nToM2.getName()+": I received something! - "
                        +((String) message)));

        TCPConnector channelOne = new TCPConnector("One (sever) 1", port);
        TCPConnector channelTwo = new TCPConnector("client two 1", port);

        channelOne.accept();
        channelTwo.connect();

        channelOne.waitForConnection();
        channelTwo.waitForConnection();

        List<InputStream> ins = channelOne.getIns();
        List<OutputStream> outs = channelOne.getOuts();

        nToM1.handleConnection(channelOne.getIns().get(0), channelOne.getOuts().get(0));
        nToM2.handleConnection(channelTwo.getIns().get(0), channelTwo.getOuts().get(0));
        //nToM1.handleConnections(channelOne.getIns(), channelOne.getOuts());
        //nToM2.handleConnections(channelTwo.getIns(), channelTwo.getOuts());

        String messageString1 = "got it?";
        byte[] message1 = messageString1.getBytes("UTF-8");

        nToM1.sendMessageToAll(messageString1);
        Thread.sleep(500);
    }
}
