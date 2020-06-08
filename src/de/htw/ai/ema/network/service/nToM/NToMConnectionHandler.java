package de.htw.ai.ema.network.service.nToM;

import de.htw.ai.ema.network.service.handler.ConnectionHandler;
import de.htw.ai.ema.network.service.listener.ReceiveListener;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class NToMConnectionHandler implements ConnectionHandler {

    private final String name;
    private Map<Integer,NToMReader> activeReaders = new HashMap<>();
    private Map<Integer, OutputStream> activeOutputStreams = new HashMap<>();
    private int readNext = 0;
    private List<String> usedIds = new ArrayList<>();
    private List<ReceiveListener> receiveListeners;
    //TODO do i still need this after implementing listener
    private Object lastMessage;

    public NToMConnectionHandler(String name){
        this.name = name;
        this.receiveListeners = new ArrayList<>();
    }

    public Object getLastMessage() {
        return lastMessage;
    }

    public String getName() {
        return name;
    }

    public Map<Integer, OutputStream> getActiveOutputStreams() {
        return activeOutputStreams;
    }

    public Map<Integer, NToMReader> getActiveReaders() {
        return activeReaders;
    }

    public int getReadNext() {
        return readNext;
    }

    @Override
    public void addReceiveListener(ReceiveListener listener){
        this.receiveListeners.add(listener);
    }

    @Override
    public void sendMessage(Object message){
        if(activeOutputStreams.size()>0){
            long time = System.currentTimeMillis();
            int random = new Random().nextInt();
            StringBuilder sb = new StringBuilder();
            sb.append(time);
            sb.append(random);
            String id = sb.toString();
            this.usedIds.add(id);
            PDU pdu = new PDU(id, message);
            List<Integer> deadIds = new ArrayList<>();
            for(Entry<Integer, OutputStream> entry: activeOutputStreams.entrySet()){
                try {
                    pdu.writePDU(entry.getValue());
                } catch (IOException e) {
                    System.out.println("Error writing pdu");
                    e.printStackTrace();
                    deadIds.add(entry.getKey());
                }
            }
            removeDeadConnections(deadIds);
        }
    }

    private void removeDeadConnections(List<Integer> deadIds){
        for(Integer outId: deadIds){
            activeOutputStreams.remove(outId);
            NToMReader reader = this.activeReaders.remove(outId);
            reader.cancel(true);
        }
    }

    @Override
    public void handleConnection(InputStream in, OutputStream out) throws IOException {
        int id = readNext++;
        this.activeOutputStreams.put(id, out);
        this.activeReaders.put(id, new NToMReader(id, in));
        this.activeReaders.get(this.activeReaders.size()-1).start();
    }

    /*@Override
    public void unhandleConnections(boolean closeStreams) {
        if(closeStreams){
            for(OutputStream out: this.activeOutputStreams.values()){
                try {
                    out.close();
                } catch (IOException e) {
                    System.out.println("Could not close outputstream");
                    e.printStackTrace();
                }
            }
        }
        this.activeOutputStreams.clear();
        for (NToMReader reader : this.activeReaders.values()){
            reader.cancel(closeStreams);
        }
        this.activeReaders.clear();
    }*/

    private synchronized void handlePDU(PDU pdu, NToMReader reader){
        if(this.usedIds.contains(pdu.id)){
            System.out.println("pdu id already exists");
        } else {
            Object message = pdu.getMessage();
            //for Test:
            //String received = new String(message, StandardCharsets.UTF_8).trim();
            System.out.println(this.name+" received message of type: "+ message.getClass());
            this.lastMessage = message;
            for(ReceiveListener rl: this.receiveListeners){
                rl.onReceive(message);
            }
            List<Integer> deadIds = new ArrayList<>();
            for(Entry<Integer, OutputStream> entry: activeOutputStreams.entrySet()){
                if(reader.id != entry.getKey()){
                    try {
                        pdu.writePDU(entry.getValue());
                    } catch (IOException e) {
                        System.out.println("Error writing pdu");
                        e.printStackTrace();
                        deadIds.add(entry.getKey());
                    }
                }
            }
            removeDeadConnections(deadIds);
        }
    }

    private class NToMReader extends Thread{
        private final InputStream in;
        //private ObjectInputStream objectIn;
        private final int id;
        private boolean handle;

        public NToMReader(int id, InputStream in){
            this.id = id;
            this.in = in;
            this.handle = true;
        }

        public void run(){
            while(this.handle){
                try {
                    if(in == null){
                        System.out.println(NToMConnectionHandler.this.name+": inputstream was null");
                    }
                    PDU pdu = new PDU(in);
                    NToMConnectionHandler.this.handlePDU(pdu, this);
                } catch (IOException e) {
                    System.out.println("Error creating PDU");
                    e.printStackTrace();
                    this.handle = false;
                }
            }
        }

        public void cancel(boolean closeStream){
            this.handle = false;
            if(closeStream) {
                try {
                    this.in.close();
                } catch (IOException e) {
                    System.out.println("Error closing input stream");
                    e.printStackTrace();
                }
            }
            this.interrupt();
        }
    }

    private class PDU{
        private String id;
        private Object message;

        public PDU(String id, Object message){
            this.id = id;
            this.message = message;
        }

        public PDU(InputStream in) throws IOException{
            try {
                ObjectInputStream objectIn = new ObjectInputStream(in);
                this.message = (Object) objectIn.readObject();
            } catch (ClassNotFoundException e) {
                System.out.println("Class was not found.");
                e.printStackTrace();
            }
        }

        public void writePDU(OutputStream out) throws IOException{
            ObjectOutputStream objectOut = new ObjectOutputStream(out);
            objectOut.writeObject(message);
        }

        public Object getMessage() {
            return message;
        }
    }
}
