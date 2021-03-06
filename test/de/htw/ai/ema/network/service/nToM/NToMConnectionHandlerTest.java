package de.htw.ai.ema.network.service.nToM;

import org.junit.Test;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static org.junit.Assert.*;

public class NToMConnectionHandlerTest {

    int port = 7777;
    NToMConnectionHandler nToM1 = new NToMConnectionHandler("One");
    NToMConnectionHandler nToM2 = new NToMConnectionHandler("Two");
    NToMConnectionHandler nToM3 = new NToMConnectionHandler("Three");
    NToMConnectionHandler nToM4 = new NToMConnectionHandler("Four");

    @Test
    public void testConstruct(){
        assertEquals("Name of nToM1 did not equal 'One'", nToM1.getName(), "One");
    }

    @Test
    public void testHandleConnection() throws IOException {
        port+=2;
        TCPConnector channelOne = new TCPConnector("One (sever) 2", port);
        TCPConnector channelTwo = new TCPConnector("client two 2", port);
        channelOne.accept();
        channelTwo.connect();
        channelOne.waitForConnection();
        channelTwo.waitForConnection();
        InputStream in = channelOne.getIns().get(0);
        OutputStream out = channelOne.getOuts().get(0);
        int readNextBefore = nToM1.getReadNext();
        int numOutStreamsBefore = nToM1.getActiveOutputStreams().size();
        int numReadersBefore = nToM1.getActiveReaders().size();
        nToM1.handleConnection(in, out);
        int readNextAfter = nToM1.getReadNext();
        int numOutStreamsAfter = nToM1.getActiveOutputStreams().size();
        int numReadersAfter = nToM1.getActiveReaders().size();
        assertEquals("id was not increased", readNextAfter, readNextBefore + 1);
        assertEquals("given output stream was not added to list", numOutStreamsAfter, numOutStreamsBefore + 1);
        assertEquals("reader not created or not added to list: readers before: " + numReadersBefore +
                ", readers after: " + numReadersAfter, numReadersAfter, numReadersBefore + 1);
    }

    @Test
    public void testSendMessage() throws InterruptedException {
        port+=3;
        TCPConnector channelOne = new TCPConnector("One (sever) 3", port);
        TCPConnector channelTwo = new TCPConnector("client two 3", port);
        TCPConnector channelThree = new TCPConnector("client three", port);
        TCPConnector channelFour = new TCPConnector("client four", port);
        channelOne.accept();
        channelTwo.connect();
        channelThree.connect();
        channelFour.connect();

        try {
            channelOne.waitForConnection();
            channelTwo.waitForConnection();
            channelThree.waitForConnection();
            channelFour.waitForConnection();

            String message1 = "hello all";
            String message2 = "hello on the other side";
            String message3 = "I forgot to tell you...";

            String receivedBy1 = null;
            String receivedBy2 = null;
            String receivedBy3 = null;
            String receivedBy4 = null;

            for(int i = 0; i<channelOne.getIns().size(); i++){
                nToM1.handleConnection(channelOne.getIns().get(i), channelOne.getOuts().get(i));
            }

            nToM2.handleConnection(channelTwo.getIns().get(0), channelTwo.getOuts().get(0));
            nToM3.handleConnection(channelThree.getIns().get(0), channelThree.getOuts().get(0));
            nToM4.handleConnection(channelFour.getIns().get(0), channelFour.getOuts().get(0));

            //server to clients
            nToM1.sendMessage(message1);
            Thread.sleep(500);
            receivedBy2 = (String) nToM2.getLastMessage();
            receivedBy3 = (String) nToM3.getLastMessage();
            receivedBy4 = (String) nToM4.getLastMessage();

            assertEquals("nToM2 didn't receive the correct message",
                    message1, receivedBy2);
            assertEquals("nToM3 didn't receive the correct message",
                    message1, receivedBy3);
            assertEquals("nToM4 didn't receive the correct message",
                    message1, receivedBy4);

            //client 3 to server and other clients
            nToM3.sendMessage(message2);
            Thread.sleep(500);
            receivedBy1 = (String) nToM1.getLastMessage();
            receivedBy2 = (String) nToM2.getLastMessage();
            receivedBy4 = (String) nToM4.getLastMessage();

            assertEquals("server nToM1 didn't receive the correct message",
                    message2, receivedBy1);
            assertEquals("client nToM2 didn't receive the correct message",
                    message2, receivedBy2);
            assertEquals("client nToM4 didn't receive the correct message",
                    message2, receivedBy4);


            //server sending second message
            nToM1.sendMessage(message3);
            Thread.sleep(500);
            receivedBy2 = (String) nToM2.getLastMessage();
            receivedBy3 = (String) nToM3.getLastMessage();
            receivedBy4 = (String) nToM4.getLastMessage();

            assertEquals("nToM2 didn't receive the correct message the second time",
                    message3, receivedBy2);
            assertEquals("nToM3 didn't receive the correct message the second time",
                    message3, receivedBy3);
            assertEquals("nToM4 didn't receive the correct message the second time",
                    message3, receivedBy4);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
