package de.htw.ai.ema.network.service.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface ConnectionHandler {

    /**
     * This method takes an Input- and OutputStream of any stream based protocol and handles
     * the connection.
     * @param in InputStream
     * @param out OutputStream
     * @throws IOException
     */
    public void handleConnection(InputStream in, OutputStream out) throws IOException;

    /**
     * When this Method is called all Readers and Outputstreams are removed from the handler and thus no more messages
     * can be received or send.
     * @param closeStreams boolean: if set to true, active Input- and Outputstreams of Handler will be closed. Setting
     *                     this to false leaves them open for further use.
     */
    public void unhandleConnections(boolean closeStreams);

}
