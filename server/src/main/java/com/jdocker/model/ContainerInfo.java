package com.jdocker.model;

public class ContainerInfo {

    private String id;
    private String name;
    private String image;
    private String state;
    private String status;

    public ContainerInfo() {}

    public ContainerInfo(String id, String name, String image, String state, String status) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.state = state;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public String getState() {
        return state;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return name + " | " + image + " | " + state + " | " + status;
    }
}
