package ru.otus.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Server {
    private int port;
    private Map< String, ClientHandler> clients;

    public Server(int port) {
        this.port = port;
        clients = new HashMap<>();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту: " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                subscribe(new ClientHandler(this, socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        clients.put(clientHandler.getUsername(), clientHandler);
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler.getUsername());
    }

    public synchronized void broadcastMessage(String message) {
        for (Map.Entry<String, ClientHandler> client : clients.entrySet()) {
            client.getValue().sendMessage(message);
        }
    }
    public synchronized void privateMessage(ClientHandler clientHandler, String name, String message) {
        if (clients.containsKey(name)) {
            clients.get(name).sendMessage(clientHandler.getUsername() + " private message: " + message);
            clientHandler.sendMessage("Отправлено сообщение " + name + " " + message);
        } else {
            clientHandler.sendMessage("Пользователя " + name + " нет в сети или неверно введен никнейм");
        }
    }
}
