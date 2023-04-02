/**
 *
 *  @author Śnieżko Eugeniusz S23951
 *
 */

package zad1;


import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServerTwo {
    private static final String ENCODING = "UTF-8";
    private static final int BUF_SIZE = 1024;
    private static final long DELAY = 500;
    private String host;
    private int port;
    private Map<String, SocketChannel> loggedInClients;
    private volatile boolean stopped;
    private BlockingQueue<String> serverLog;
    private Selector selector;
    private ExecutorService clientHandler;

    public ChatServerTwo(String host, int port) {
        this.host = host;
        this.port = port;
        this.loggedInClients = new ConcurrentHashMap<>();
        this.serverLog = new LinkedBlockingQueue<>();
        this.clientHandler = Executors.newFixedThreadPool(10);
    }

    public void startServer() {
        try (ServerSocketChannel serverSocket = ServerSocketChannel.open()) {
            serverSocket.configureBlocking(false);
            serverSocket.bind(new InetSocketAddress(host, port));
            selector = Selector.open();
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Server started");
            while (!stopped) {
                int n = selector.select(DELAY);
                if (n == 0) {
                    continue;
                }
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> it = keys.iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();
                    if (key.isValid() && key.isAcceptable()) {
                        handleAccept(serverSocket, selector);
                    }
                    if (key.isValid() && key.isReadable()) {
                        handleRead(key);
                    }
                }
            }
            selector.close();
            System.out.println("Server stopped");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopServer() {
        stopped = true;
        clientHandler.shutdown();
    }

    private void handleAccept(ServerSocketChannel serverSocket, Selector selector) throws IOException {
        SocketChannel client = serverSocket.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(BUF_SIZE));
    }

    private void handleRead(SelectionKey key) {
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        try {
            if (client.read(buffer) < 0) {
                removeClient(key);
                return;
            }
            buffer.flip();
            String message = StandardCharsets.UTF_8.decode(buffer).toString().trim();
            buffer.clear();
            clientHandler.execute(() -> processMessage(key, message));
        } catch (IOException e) {
            removeClient(key);
        }
    }

    private void processMessage(SelectionKey key, String message) {
        try {
            String[] tokens = message.split("\\s+");
            if (tokens.length == 0) {
                return;
            }
            SocketChannel client = (SocketChannel) key.channel();
            if (!loggedInClients.containsKey(client)) {
                if (tokens.length != 1) {
                    client.write(StandardCharsets.UTF_8.encode("login failed\n"));
                    return;
                }
                String clientId = tokens[0];
                if (loggedInClients.containsKey(clientId)) {
                    client.write(StandardCharsets.UTF_8.encode("login failed\n"));
                    return;
                }
                loggedInClients.put(clientId, client);
                serverLog.offer(getTime() + " " + clientId + " logged in\n");
                client.write(StandardCharsets.UTF_8.encode("login success\n"));
                return;
            }
// handle incoming chat messages
            if (tokens.length == 2 && tokens[0].equals("chat")) {
                String message = tokens[1];
                String senderId = null;
// search for the sender of the message
                for (Map.Entry<String, AsynchronousSocketChannel> entry : loggedInClients.entrySet()) {
                    if (entry.getValue().equals(client)) {
                        senderId = entry.getKey();
                        break;
                    }
                }
// broadcast the message to all logged in clients
                for (AsynchronousSocketChannel receiver : loggedInClients.values()) {
                    receiver.write(StandardCharsets.UTF_8.encode("[" + getTime() + "] " + senderId + ": " + message + "\n"));
                }
                return;
            }
// handle logging out
            if (tokens.length == 1 && tokens[0].equals("logout")) {
                String clientId = null;
// search for the client who wants to log out
                for (Map.Entry<String, AsynchronousSocketChannel> entry : loggedInClients.entrySet()) {
                    if (entry.getValue().equals(client)) {
                        clientId = entry.getKey();
                        break;
                    }
                }
// remove the client from the logged in list
                loggedInClients.remove(clientId);
                serverLog.offer(getTime() + " " + clientId + " logged out\n");
                client.write(StandardCharsets.UTF_8.encode("logout success\n"));
                return;
            }
// handle unknown command
            client.write(StandardCharsets.UTF_8.encode("unknown command\n"));
        }
    }
}

