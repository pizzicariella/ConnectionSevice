package de.htw.ai.ema.network.service.nToM;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class TCPConnector {

    private String name;
    private int port;
    private List<InputStream> ins;
    private List<OutputStream> outs;
    //private InputStream in;
    //private OutputStream out;
    public final int WAIT_LOOP_IN_MILLIS = 1000;
    private long waitInMillis = WAIT_LOOP_IN_MILLIS;

    public TCPConnector(String name, int port){
        this.name = name;
        this.port = port;
        this.ins = new LinkedList<>();
        this.outs = new LinkedList<>();
    }

    public List<InputStream> getIns() {
        return ins;
    }

    public List<OutputStream> getOuts() {
        return outs;
    }

    /* public InputStream getIn() {
        return in;
    }

    public OutputStream getOut() {
        return out;
    }*/

    public void accept(){
        AcceptThread acceptThread = new AcceptThread();
        acceptThread.start();
    }

    private class AcceptThread extends Thread {

        ServerSocket serverSocket;

        public AcceptThread(){
            try {
                this.serverSocket = new ServerSocket(TCPConnector.this.port);
            } catch (IOException e) {
                System.out.println(TCPConnector.this.name+": Could not create server socket on port "+ TCPConnector.this.port);
                e.printStackTrace();
            }
        }

        public void run(){
            boolean cancel = false;
            while (!cancel){
                try{
                    Socket socket = serverSocket.accept();
                    System.out.println(TCPConnector.this.name+": connection was established");
                    ins.add(socket.getInputStream());
                    outs.add(socket.getOutputStream());
                    //TCPChannel.this.in = socket.getInputStream();
                    //TCPChannel.this.out = socket.getOutputStream();
                } catch (IOException e){
                    System.out.println(TCPConnector.this.name+": could not connect");
                    e.printStackTrace();
                    try {
                        this.serverSocket.close();
                    } catch (IOException ioException) {
                        System.out.println(TCPConnector.this.name+": Could not close server socket");
                        ioException.printStackTrace();
                    }
                }

            }
        }

    }

    public void connect(){
        new Connector();
    }

    private class Connector {

        Socket socket;

        public Connector(){
            try {
                this.socket = new Socket("localhost",port);
                TCPConnector.this.ins.add(this.socket.getInputStream());
                TCPConnector.this.outs.add(this.socket.getOutputStream());
                System.out.println(TCPConnector.this.name+": connected");
            } catch (IOException e) {
                System.out.println(TCPConnector.this.name+": Could not create Client port");
                e.printStackTrace();
                try {
                    socket.close();
                } catch (IOException ioException) {
                    System.out.println(TCPConnector.this.name+": Could not close client socket");
                    ioException.printStackTrace();
                }
            }
        }
    }

    public void waitForConnection() throws IOException {
        if (this.ins.size() == 0 || this.outs.size() == 0) {
            /* in unit tests there is a race condition between the test
            thread and those newly created tests to establish a connection.
            Thus, this call could be in the right order - give it a
            second chance
            */

            try {
                Thread.sleep(this.waitInMillis);
            } catch (InterruptedException ex) {
                // ignore
            }

            if (this.ins.size() == 0 || this.outs.size() == 0) {
                // that's probably wrong usage:
                throw new IOException("must start TCPChannel thread first by calling start()");
            }
        }
    }
}
