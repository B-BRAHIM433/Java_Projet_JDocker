package com.jdocker.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jdocker.protocol.Response;

import java.util.List;
import java.util.Map;

public class ResponseFormatter {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static void formatAndPrint(Response response, String command) {
        if (response.getStatus().equals("ERROR")) {
            System.out.println("❌ Erreur: " + response.getMessage());
            return;
        }

        // Afficher le message si présent
        if (response.getMessage() != null && !response.getMessage().isEmpty()) {
            System.out.println("✓ " + response.getMessage());
        }

        // Formater le payload selon le type de commande
        if (response.getPayload() != null) {
            try {
                String action = command.toUpperCase();
                
                if (action.contains("LIST_IMAGES")) {
                    formatImages(response);
                } else if (action.contains("LIST_CONTAINERS") || action.contains("LIST_RUNNING")) {
                    formatContainers(response);
                } else {
                    if (response.getMessage() == null || response.getMessage().isEmpty()) {
                        System.out.println("✓ Opération réussie");
                    }
                }
            } catch (Exception e) {
                // En cas d'erreur de formatage, afficher le payload brut
                System.out.println(response.getPayload());
            }
        } else if (response.getMessage() == null || response.getMessage().isEmpty()) {
            System.out.println("✓ Opération réussie");
        }
    }

    
    private static void formatImages(Response response) throws Exception {
        List<Map<String, Object>> images = mapper.convertValue(
            response.getPayload(),
            new TypeReference<List<Map<String, Object>>>() {}
        );

        if (images == null || images.isEmpty()) {
            System.out.println("Aucune image trouvée.");
            return;
        }

        System.out.println("\n" + "=".repeat(100));
        System.out.printf("%-15s %-40s %-15s %-20s%n", "IMAGE ID", "REPOSITORY", "TAG", "SIZE");
        System.out.println("-".repeat(100));

        for (Map<String, Object> img : images) {
            String id = truncate(safeString(img.get("id")), 12);
            String repo = truncate(safeString(img.get("repo")), 38);
            String tag = truncate(safeString(img.get("tag")), 13);
            String size = formatSize(img.get("size") != null ? (Number) img.get("size") : null);

            System.out.printf("%-15s %-40s %-15s %-20s%n", id, repo, tag, size);
        }

        System.out.println("=".repeat(100));
        System.out.println("Total: " + images.size() + " image(s)\n");
    }

    
    private static void formatContainers(Response response) throws Exception {
        List<Map<String, Object>> containers = mapper.convertValue(
            response.getPayload(),
            new TypeReference<List<Map<String, Object>>>() {}
        );

        if (containers == null || containers.isEmpty()) {
            System.out.println("Aucun conteneur trouvé.");
            return;
        }

        System.out.println("\n" + "=".repeat(120));
        System.out.printf("%-15s %-25s %-30s %-10s %-30s%n", 
            "CONTAINER ID", "NAME", "IMAGE", "STATUS", "STATE");
        System.out.println("-".repeat(120));

        for (Map<String, Object> container : containers) {
            String id = truncate(safeString(container.get("id")), 12);
            String name = truncate(safeString(container.get("name")), 23);
            String image = truncate(safeString(container.get("image")), 28);
            String status = truncate(safeString(container.get("status")), 8);
            String state = safeString(container.get("state"));

            String stateDisplay = state;
            if (state != null && state.equals("running")) {
                stateDisplay = "🟢 " + truncate(state, 25);
            } else if (state != null && state.equals("exited")) {
                stateDisplay = "🔴 " + truncate(state, 25);
            } else {
                stateDisplay = "🟡 " + truncate(state, 25);
            }

            System.out.printf("%-15s %-25s %-30s %-10s %-30s%n", 
                id, name, image, status, stateDisplay);
        }

        System.out.println("=".repeat(120));
        System.out.println("Total: " + containers.size() + " conteneur(s)\n");
    }

    
    private static String safeString(Object obj) {
        if (obj == null) return "";
        return String.valueOf(obj);
    }

    
    private static String truncate(String str, int maxLength) {
        if (str == null || str.isEmpty()) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }

    
    private static String formatSize(Number size) {
        if (size == null) return "0 B";
        
        long bytes = size.longValue();
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
}

