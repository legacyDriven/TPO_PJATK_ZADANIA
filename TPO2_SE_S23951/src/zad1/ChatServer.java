package zad1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class ChatServer {
    private final String host;
    private final int port;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private final StringBuilder serverLog;
    private final Map<SocketChannel, ChatClient> clients;
    private Thread serverThread;

    public ChatServer(String host, int port) {
        this.host = host;
        this.port = port;
        this.serverLog = new StringBuilder();
        this.clients = new ConcurrentHashMap<>();
    }

    public void startServer() {
        if (serverThread != null && serverThread.isAlive()) {
            return;
        }

        serverThread = new Thread(() -> {
            try {
                serverSocketChannel = ServerSocketChannel.open();
                serverSocketChannel.bind(new InetSocketAddress(host, port));
                serverSocketChannel.configureBlocking(false);

                selector = Selector.open();
                serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
                System.out.println("Server started on port " + port);

                while (true) {
                    int readyChannels = selector.select();

                    if (readyChannels == 0) {
                        continue;
                    }

                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();
                        keyIterator.remove();

                        if (key.isAcceptable()) {
                            ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                            SocketChannel clientChannel = serverChannel.accept();
                            clientChannel.configureBlocking(false);
                            clientChannel.register(selector, SelectionKey.OP_READ);
                            System.out.println("Connection accepted from " + clientChannel.getRemoteAddress());
                        }

                        if (key.isReadable()) {
                            SocketChannel clientChannel = (SocketChannel) key.channel();
                            ByteBuffer buffer = ByteBuffer.allocate(1024);
                            int bytesRead = clientChannel.read(buffer);

                            if (bytesRead > 0) {
                                String message = new String(buffer.array()).trim();
                                System.out.println("Received message: " + message);

                                buffer.flip();
                                clientChannel.write(buffer);
                                buffer.clear();
                            } else if (bytesRead == -1) {
                                clientChannel.close();
                                System.out.println("Connection closed by client");
                            }
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Error starting server: " + e.getMessage());
            } finally {
                if (serverSocketChannel != null) {
                    try {
                        serverSocketChannel.close();
                    } catch (IOException e) {
                        System.out.println("Error closing serverSocketChannel: " + e.getMessage());
                    }
                }
                if (selector != null) {
                    try {
                        selector.close();
                    } catch (IOException e) {
                        System.out.println("Error closing selector: " + e.getMessage());
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        int port = 8000; // You can change the port number as needed
        String host = "localhost"; // You can change the host as needed

        NonBlockingServer server = new NonBlockingServer(host, port);
        server.start();
    }
}




