package com.jdocker;

import java.net.ServerSocket;
import java.net.Socket;

public class DockerServer {

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(9090)) {

            System.out.println("Serveur Docker démarré sur le port 9090");

            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("Nouveau client connecté : " + client.getInetAddress());

                new Thread(new ClientHandler(client)).start();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
