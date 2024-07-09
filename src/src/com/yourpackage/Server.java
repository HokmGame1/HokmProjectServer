package com.yourpackage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());
    private static final int PORT = 12345;
    private final List<com.yourpackage.Room> rooms;

    public Server() {
        rooms = new ArrayList<>();
        // Initialize with some empty room slots
        for (int i = 0; i < 10; i++) {
            rooms.add(new com.yourpackage.Room(null, 4, 7));  // Assuming a default of 4 players per room
        }
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            LOGGER.info("Server is listening on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                new com.yourpackage.ClientHandler(socket, this).start();
            }

        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Server exception", ex);
        }
    }

    public synchronized com.yourpackage.Room createRoom(String creator, int maxPlayers, int rounds) {
        LOGGER.log(Level.INFO, "round numbers: " + rounds);
        for (int i = 0; i < rooms.size(); i++) {
            if (rooms.get(i).getCreator() == null) {
                com.yourpackage.Room room = new com.yourpackage.Room(creator, maxPlayers, rounds);
                rooms.set(i, room);
                notifyAll();  // Notify any waiting threads that the room list has been updated
                return room;
            }
        }
        com.yourpackage.Room newRoom = new com.yourpackage.Room(creator, maxPlayers, rounds);
        rooms.add(newRoom);  // Add new room to the list
        notifyAll();  // Notify any waiting threads that the room list has been updated
        return newRoom;
    }

    public synchronized void removeRoom(com.yourpackage.Room room) {
        int index = rooms.indexOf(room);
        if (index != -1) {
            rooms.set(index, new com.yourpackage.Room(null, 4, 7));
        }
    }

    public synchronized List<com.yourpackage.Room> getRooms() {
        return new ArrayList<>(rooms);
    }

    public synchronized com.yourpackage.Room getRoomByCreator(String creator) {
        for (com.yourpackage.Room room : rooms) {
            if (room.getCreator() != null && room.getCreator().equals(creator)) {
                return room;
            }
        }
        return null;
    }

    public synchronized com.yourpackage.Room getRoomByPlayer(String player) {
        for (com.yourpackage.Room room : rooms) {
            if (room.getPlayers().contains(player)) {
                return room;
            }
        }
        return null;
    }
}
