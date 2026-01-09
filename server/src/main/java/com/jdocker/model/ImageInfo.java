package com.jdocker.model;

public class ImageInfo {

    private String id;
    private String repo;
    private String tag;
    private long size;

    public ImageInfo() {}

    public ImageInfo(String id, String repo, String tag, long size) {
        this.id = id;
        this.repo = repo;
        this.tag = tag;
        this.size = size;
    }

    public String getId() {
        return id;
    }

    public String getRepo() {
        return repo;
    }

    public String getTag() {
        return tag;
    }

    public long getSize() {
        return size;
    }

    @Override
    public String toString() {
        return repo + ":" + tag + " | ID=" + id + " | Size=" + size;
    }
}
