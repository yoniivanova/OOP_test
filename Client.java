package Client;

import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Client {

    private static BufferedReader messageReader;
    private static PrintWriter messageWriter;

    public static void main(String[] arg) throws IOException, ClassNotFoundException, InterruptedException {
        final Set<String> userName = new HashSet<>();
        final String str = "abcdefghijklmnopqrstuvwxyz";
        final Random random = new Random();
        try {
            Socket socketConnection = new Socket("127.0.0.1", 12345);
            StringBuilder builder = new StringBuilder();
            DataOutputStream outToServer = new DataOutputStream(socketConnection.getOutputStream());
            while (builder.toString().length() == 0) {
                int length = random.nextInt(5) + 5;
                for (int i = 0; i < length; i++) {
                    builder.append(str.charAt(random.nextInt(str.length())));
                }
                if (userName.contains(builder.toString())) {
                    builder = new StringBuilder();

                }

                outToServer.writeUTF(builder.toString());
                System.out.println("Random username generated: " + builder.toString());

                messageReader = new BufferedReader(new InputStreamReader(socketConnection.getInputStream()));
                messageWriter = new PrintWriter(new OutputStreamWriter(socketConnection.getOutputStream()));
                PrintWriter consoleWriter = new PrintWriter(System.out);
                TextDataTransmitter socketToConsoleTransmitter = new TextDataTransmitter(messageReader, consoleWriter);
                socketToConsoleTransmitter.setDaemon(false);
                socketToConsoleTransmitter.start();


                BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
                TextDataTransmitter consoleToSocketTransmitter = new TextDataTransmitter(consoleReader, messageWriter);
                consoleToSocketTransmitter.setDaemon(false);
                consoleToSocketTransmitter.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


class TextDataTransmitter extends Thread {
    private BufferedReader messageReader;
    private PrintWriter messageWriter;

    public TextDataTransmitter(BufferedReader mReader, PrintWriter mWriter) {
        messageReader = mReader;
        messageWriter = mWriter;

    }

    public void run() {
        try {
            while (!isInterrupted()) {
                String data = messageReader.readLine();
                messageWriter.println(data);
                messageWriter.flush();

            }
        } catch (IOException ioe) {
            System.err.println("Connection lost.");
            System.exit(-1);
        }
    }
}
