package com.jdocker.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.AuthConfig;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DockerClientBuilder;
import com.jdocker.model.ContainerInfo;
import com.jdocker.model.ImageInfo;

import java.util.ArrayList;
import java.util.List;

public class DockerService {

    private DockerClient dockerClient;

    public DockerService() {
        // Use Unix socket (default on Linux) instead of TCP
        dockerClient = DockerClientBuilder.getInstance().build();
    }

    // Lister les images
    public List<ImageInfo> listImages() {
        List<Image> images = dockerClient.listImagesCmd().exec();
        List<ImageInfo> result = new ArrayList<>();

        for (Image img : images) {
            if (img.getRepoTags() != null) {
                for (String tag : img.getRepoTags()) {
                    String[] parts = tag.split(":");
                    String repo = parts[0];
                    String tagName = parts.length > 1 ? parts[1] : "latest";
                    String imageId = img.getId();
                    // Extract short ID (12 chars) - handle different ID formats
                    String shortId = imageId.length() > 12 ? imageId.substring(7, 19) : imageId;
                    result.add(new ImageInfo(
                            shortId,
                            repo,
                            tagName,
                            img.getSize()
                    ));
                }
            }
        }
        return result;
    }

    // Lister tous les conteneurs (ps -a)
    public List<ContainerInfo> listContainers() {
        List<Container> containers = dockerClient.listContainersCmd()
                .withShowAll(true)
                .exec();

        List<ContainerInfo> result = new ArrayList<>();

        for (Container c : containers) {
            String containerId = c.getId();
            String shortId = containerId.length() > 12 ? containerId.substring(0, 12) : containerId;
            String name = (c.getNames() != null && c.getNames().length > 0) 
                    ? c.getNames()[0].replace("/", "") 
                    : "unnamed";
            result.add(new ContainerInfo(
                    shortId,
                    name,
                    c.getImage(),
                    c.getState(),
                    c.getStatus()
            ));
        }
        return result;
    }

    // Lister uniquement les conteneurs en cours d'exécution (ps)
    public List<ContainerInfo> listRunningContainers() {
        List<Container> containers = dockerClient.listContainersCmd()
                .withShowAll(false) 
                .exec();

        List<ContainerInfo> result = new ArrayList<>();

        for (Container c : containers) {
            String containerId = c.getId();
            String shortId = containerId.length() > 12 ? containerId.substring(0, 12) : containerId;
            String name = (c.getNames() != null && c.getNames().length > 0) 
                    ? c.getNames()[0].replace("/", "") 
                    : "unnamed";
            result.add(new ContainerInfo(
                    shortId,
                    name,
                    c.getImage(),
                    c.getState(),
                    c.getStatus()
            ));
        }
        return result;
    }

    // Télécharger une image
    public void pullImage(String imageName) throws InterruptedException {
        dockerClient.pullImageCmd(imageName)
                .start()
                .awaitCompletion();
    }

    // Créer et démarrer un conteneur
    public String runContainer(String imageName) {
        return dockerClient.createContainerCmd(imageName)
                .exec()
                .getId();
    }

    // Démarrer un conteneur existant
    public void startContainer(String id) {
        dockerClient.startContainerCmd(id).exec();
    }

    // Stopper un conteneur
    public void stopContainer(String id) {
        dockerClient.stopContainerCmd(id).exec();
    }

    // Supprimer un conteneur
    public void removeContainer(String id) {
        dockerClient.removeContainerCmd(id).exec();
    }

    // Supprimer une image (rmi)
    public void removeImage(String imageId) {
        dockerClient.removeImageCmd(imageId).exec();
    }

    // Authentification Docker (login)
    public void dockerLogin(String username, String password, String registryUrl) {
        AuthConfig authConfig = new AuthConfig()
                .withUsername(username)
                .withPassword(password);
        
        if (registryUrl != null && !registryUrl.isEmpty() && !registryUrl.equals("docker.io")) {
            authConfig.withRegistryAddress(registryUrl);
        } else {
            // Default to Docker Hub
            authConfig.withRegistryAddress("https://index.docker.io/v1/");
        }
        
        // Validate credentials by attempting authentication
        try {
            dockerClient.authCmd().withAuthConfig(authConfig).exec();
        } catch (Exception e) {
            throw new RuntimeException("Authentication validation failed: " + e.getMessage(), e);
        }
    }
}
