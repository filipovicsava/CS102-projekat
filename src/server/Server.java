package server;

import server.db.DatabaseControler;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements AutoCloseable {

    static private final int THREAD_TIMEOUT = 1000;
    public int port;
    private ServerSocket serverSocket;
    private volatile boolean running;
    private Thread workerThread;

    private Server(ServerSocket socket) {
        this.serverSocket = socket;
        this.port = socket.getLocalPort();
        this.running = true;
        this.workerThread = new Thread(this::doWork);
        this.workerThread.start();
    }

    public static void main(String[] args) {
        DatabaseControler.initDatabase();
        try {
            Server.start(7999);
        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }

    public static Server start(int port) throws IOException {
        return new Server(new ServerSocket(port));
    }

    private void doWork() {
        while (this.running) {
            try {
                new Thread(new HandlerThread(serverSocket.accept())).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void stop() throws InterruptedException, IOException {
        this.running = false;
        this.serverSocket.close();
        this.workerThread.join(Server.THREAD_TIMEOUT);
    }

    @Override
    public void close() throws Exception {

    }
}

class HandlerThread implements Runnable {
    private Socket socket;

    HandlerThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            DataOutputStream writer = new DataOutputStream(socket.getOutputStream());

            String str;

            if((str = reader.readLine()) != null) {
                if (!str.isEmpty()) {
                    String[] split = str.split(" ", 2);
                    if (split[0].equals("ADD")) {
                        String[] address = split[1].split(",", 2);
                        handleInsert(address[1], address[0]);
                        writer.writeBytes("Successfully added");
                    } else if (split[0].equals("DELETE")) {
                        handleDelete(1);
                        writer.writeBytes("Successfully deleted");
                    } else {
                        writer.writeBytes("404 Not Found");
                    }
                }
            } else {
                writer.writeBytes("Empty request");
            }

            reader.close();
            writer.flush();
            writer.close();
            socket.close();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }
    
    public void handleInsert(String street, String number) {
        DatabaseControler.addRide(street, number);
    }

    public void handleDelete(int id) {
        DatabaseControler.cancelRide(id);
    }
}
