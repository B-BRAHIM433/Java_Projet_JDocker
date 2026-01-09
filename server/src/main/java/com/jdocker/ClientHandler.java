package com.jdocker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jdocker.docker.DockerService;
import com.jdocker.protocol.Request;
import com.jdocker.protocol.Response;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Socket socket;
    private DockerService dockerService;
    private ObjectMapper mapper = new ObjectMapper();

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.dockerService = new DockerService();
    }

    @Override
    public void run() {

        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String line;

            while ((line = in.readLine()) != null) {

                Request req;
                Response res = new Response();

                try {
                    req = mapper.readValue(line, Request.class);

                    switch (req.getAction()) {

                        case "LIST_IMAGES":
                            res.setStatus("OK");
                            res.setPayload(dockerService.listImages());
                            break;

                        case "LIST_CONTAINERS":
                            res.setStatus("OK");
                            res.setPayload(dockerService.listContainers());
                            break;

                        case "LIST_RUNNING_CONTAINERS":
                            res.setStatus("OK");
                            res.setPayload(dockerService.listRunningContainers());
                            break;

                        case "PULL_IMAGE":
                            String image = req.getData().get("name");
                            new Thread(() -> {
                                try {
                                    dockerService.pullImage(image);
                                    System.out.println("Image téléchargée: " + image);
                                } catch (Exception e) {
                                    System.out.println("Erreur lors du téléchargement de l'image: " + image);
                                    e.printStackTrace();
                                }
                            }).start();
                            res.setStatus("OK");
                            res.setMessage("Téléchargement démarré en arrière-plan");
                            break;

                        case "RUN_CONTAINER":
                            String runImage = req.getData().get("name");
                            new Thread(() -> {
                                try {
                                    String containerId = dockerService.runContainer(runImage);
                                    dockerService.startContainer(containerId);
                                    System.out.println("Conteneur démarré: " + containerId);
                                } catch (Exception e) {
                                    System.out.println("Erreur lors du démarrage du conteneur: " + runImage);
                                    e.printStackTrace();
                                }
                            }).start();
                            res.setStatus("OK");
                            res.setMessage("Démarrage du conteneur en arrière-plan");
                            break;

                        case "STOP_CONTAINER":
                            String stopId = req.getData().get("id");
                            new Thread(() -> {
                                try {
                                    dockerService.stopContainer(stopId);
                                    System.out.println("Conteneur stoppé: " + stopId);
                                } catch (Exception e) {
                                    System.out.println("Erreur lors de l'arrêt du conteneur: " + stopId);
                                    e.printStackTrace();
                                }
                            }).start();
                            res.setStatus("OK");
                            res.setMessage("Arrêt du conteneur en arrière-plan");
                            break;

                        case "REMOVE_CONTAINER":
                            String rmId = req.getData().get("id");
                            new Thread(() -> {
                                try {
                                    dockerService.removeContainer(rmId);
                                    System.out.println("Conteneur supprimé: " + rmId);
                                } catch (Exception e) {
                                    System.out.println("Erreur lors de la suppression du conteneur: " + rmId);
                                    e.printStackTrace();
                                }
                            }).start();
                            res.setStatus("OK");
                            res.setMessage("Suppression du conteneur en arrière-plan");
                            break;

                        case "REMOVE_IMAGE":
                            String rmiId = req.getData().get("id");
                            try {
                                dockerService.removeImage(rmiId);
                                res.setStatus("OK");
                                res.setMessage("Image supprimée: " + rmiId);
                            } catch (Exception e) {
                                res.setStatus("ERROR");
                                res.setMessage("Erreur lors de la suppression de l'image: " + e.getMessage());
                            }
                            break;

                        case "DOCKER_LOGIN":
                            String username = req.getData().get("username");
                            String password = req.getData().get("password");
                            String registryUrl = req.getData().get("registry");
                            try {
                                dockerService.dockerLogin(username, password, registryUrl);
                                res.setStatus("OK");
                                res.setMessage("Authentification réussie");
                            } catch (Exception e) {
                                res.setStatus("ERROR");
                                res.setMessage("Erreur d'authentification: " + e.getMessage());
                            }
                            break;

                        default:
                            res.setStatus("ERROR");
                            res.setMessage("Action inconnue");
                            break;
                    }

                } catch (Exception e) {
                    res.setStatus("ERROR");
                    res.setMessage("Erreur traitement requête: " + e.getMessage());
                }

                // Envoyer la réponse au client
                out.println(mapper.writeValueAsString(res));
            }

        } catch (IOException e) {
            System.out.println("Client déconnecté: " + socket.getInetAddress());
        }
    }
}
