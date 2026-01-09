package com.jdocker.client;

import com.jdocker.protocol.Request;
import com.jdocker.protocol.Response;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;

public class DockerClientCLI {

    public static void main(String[] args) {

        Socket socket = null;
        Scanner scanner = null;

        try {
            socket = new Socket("localhost", 9090);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            ObjectMapper mapper = new ObjectMapper();
            scanner = new Scanner(System.in);

            System.out.println("Client connecté au serveur Docker");

            while (true) {
                System.out.print("docker> ");
                String cmd = scanner.nextLine().trim();

                // Handle empty commands
                if (cmd.isEmpty()) {
                    continue;
                }

                Request req = new Request();
                String[] parts = cmd.split("\\s+");

                // Commandes simples
                if (cmd.equals("images")) {
                    req.setAction("LIST_IMAGES");
                    req.setData(Map.of());

                } else if (cmd.equals("ps")) {
                    req.setAction("LIST_RUNNING_CONTAINERS");
                    req.setData(Map.of());

                } else if (cmd.equals("ps -a") || cmd.equals("ps -A")) {
                    req.setAction("LIST_CONTAINERS");
                    req.setData(Map.of());

                } else if (cmd.startsWith("pull ")) {
                    if (parts.length < 2) {
                        System.out.println("Usage: pull <image_name>");
                        continue;
                    }
                    req.setAction("PULL_IMAGE");
                    req.setData(Map.of("name", parts[1]));

                } else if (cmd.startsWith("run ")) {
                    if (parts.length < 2) {
                        System.out.println("Usage: run <image_name>");
                        continue;
                    }
                    req.setAction("RUN_CONTAINER");
                    req.setData(Map.of("name", parts[1]));

                } else if (cmd.startsWith("stop ")) {
                    if (parts.length < 2) {
                        System.out.println("Usage: stop <container_id>");
                        continue;
                    }
                    req.setAction("STOP_CONTAINER");
                    req.setData(Map.of("id", parts[1]));

                } else if (cmd.startsWith("rm ")) {
                    if (parts.length < 2) {
                        System.out.println("Usage: rm <container_id>");
                        continue;
                    }
                    req.setAction("REMOVE_CONTAINER");
                    req.setData(Map.of("id", parts[1]));

                } else if (cmd.startsWith("rmi ")) {
                    if (parts.length < 2) {
                        System.out.println("Usage: rmi <image_id>");
                        continue;
                    }
                    req.setAction("REMOVE_IMAGE");
                    req.setData(Map.of("id", parts[1]));

                } else if (cmd.equals("login")) {
                    // Interactive login
                    System.out.print("Username: ");
                    String username = scanner.nextLine();
                    System.out.print("Password: ");
                    String password = scanner.nextLine();
                    System.out.print("Registry URL (optional, press Enter for Docker Hub): ");
                    String registry = scanner.nextLine();
                    
                    req.setAction("DOCKER_LOGIN");
                    if (registry.isEmpty()) {
                        req.setData(Map.of("username", username, "password", password));
                    } else {
                        req.setData(Map.of("username", username, "password", password, "registry", registry));
                    }

                } else if (cmd.equals("exit") || cmd.equals("quit")) {
                    System.out.println("Déconnexion...");
                    break;

                } else {
                    System.out.println("Commande inconnue. Commandes disponibles:");
                    System.out.println("  images       - Lister les images");
                    System.out.println("  ps           - Lister les conteneurs en cours d'exécution");
                    System.out.println("  ps -a        - Lister tous les conteneurs");
                    System.out.println("  pull <img>   - Télécharger une image");
                    System.out.println("  run <img>    - Créer et démarrer un conteneur");
                    System.out.println("  stop <id>    - Arrêter un conteneur");
                    System.out.println("  rm <id>      - Supprimer un conteneur");
                    System.out.println("  rmi <id>     - Supprimer une image");
                    System.out.println("  login        - S'authentifier à Docker");
                    System.out.println("  exit/quit    - Quitter");
                    continue;
                }

                // Envoyer la requête au serveur 
                out.println(mapper.writeValueAsString(req));

                // Lire la réponse 
                Response res = mapper.readValue(in.readLine(), Response.class);
                
                // Formater et afficher la réponse de manière lisible
                ResponseFormatter.formatAndPrint(res, req.getAction());
            }

        } catch (Exception e) {
            System.out.println("Impossible de se connecter au serveur");
            e.printStackTrace();
        } finally {
            // Cleanup resources
            if (scanner != null) {
                scanner.close();
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }
}
