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
                        String[] info = split[1].split(",", 3);
                        int id = handleInsert(info[2], info[0], info[1]);
                        writer.writeUTF(Integer.toString(id) + " " + split[1]);
                    } else if (split[0].equals("DELETE")) {
                        handleDelete(Integer.parseInt(split[1].split(" ")[1]));
                        writer.writeUTF("Successfully deleted");
                    } else {
                        writer.writeUTF("404 Not Found");
                    }
                }
            } else {
                writer.writeUTF("Empty request");
            }

            reader.close();
            writer.flush();
            writer.close();
            socket.close();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }
    
    public int handleInsert(String street, String number, String phone) {
        return DatabaseControler.addRide(street, number, phone);
    }

    public void handleDelete(int id) {
        DatabaseControler.cancelRide(id);
    }
}
