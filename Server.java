package Server;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;


class ClientHandler extends Thread {
    ServerMessageDispatcher messageDispatcher;
    private Socket messageSocket;
    private BufferedReader messageReader;

    public ClientHandler(Socket s, ServerMessageDispatcher serverMessageDispatcher) throws IOException {
        messageSocket = s;
        messageReader = new BufferedReader(new InputStreamReader(messageSocket.getInputStream()));
        messageDispatcher = serverMessageDispatcher;
    }

    public void run() {
        DataInputStream info = null;
        String infoClient = null;
        try {
            info = new DataInputStream(messageSocket.getInputStream());
            infoClient = info.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("User connected :" + infoClient);
        while (!isInterrupted()) {
            String message = null;
            try {
                message = messageReader.readLine();
            } catch (IOException e) {
            }
            if (message == null)
                break;


            try {
                messageDispatcher.dispatchMessage(messageSocket, message);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        //messageDispatcher.deleteClient(messageSocket);
    }
}

class ServerMessageDispatcher extends Thread {
    private Socket s;
    private Vector messageClients = new Vector();
    private Vector messageQueue = new Vector();

    public synchronized void addClient(Socket clientSocket) {
        messageClients.add(clientSocket);
    }

    public synchronized void deleteClient(Socket clientSocket) throws IOException {
        int x = messageClients.indexOf(clientSocket);
        if (x != 0) {
            messageClients.removeElementAt(x);
            try {
                clientSocket.close();
            } catch (IOException e) {
            }
        }
    }

    public synchronized void dispatchMessage(Socket messageSocket, String message) throws IOException {
        String userName = "" + new DataInputStream(messageSocket.getInputStream());
        message = userName + " : " + message + "\n\r";
        messageQueue.add(message);
        if (message.contentEquals("/changename")) {
            String split = null;
            split = split.split(" ").toString();
            userName = split;
            System.out.println("Username changed to :" + userName);
        }

        notify();
    }


    public void run() {

        while (true) {
            String message = null;
            try {
                message = getMessageFromQ();
            } catch (InterruptedException e) {

            }
            try {
                sendMessage2All(message);
            } catch (IOException e) {
            }
        }
    }

    private synchronized String getMessageFromQ() throws InterruptedException {
        while (messageQueue.size() == 0)
            wait();
        String msg = (String) messageQueue.get(0);
        messageQueue.removeElementAt(0);
        return msg;
    }


    private void sendMessage2All(String message) throws IOException {
        for (int i = 0; i < messageClients.size(); i++) {
            Socket socket = (Socket) messageClients.get(i);
            try {
                OutputStream out = socket.getOutputStream();
                out.write(message.getBytes());
                out.flush();
            } catch (IOException ioe) {
                deleteClient(socket);
            }
        }
    }
}

public class Server {

    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(12345);
        System.out.println("Waiting for a new user.....");
        ServerMessageDispatcher dispatcher;
        dispatcher = new ServerMessageDispatcher();
        dispatcher.start();

        while (true) {

            Socket s = ss.accept();
            ClientHandler clientHandler = new ClientHandler(s, dispatcher);
            dispatcher.addClient(s);
            clientHandler.start();
        }

    }
}
