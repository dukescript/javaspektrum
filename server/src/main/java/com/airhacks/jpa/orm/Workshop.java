package com.airhacks.jpa.orm;

public class Workshop {

    private long id;
    private String name;

    public Workshop() {
    }

    public Workshop(long id, String name) {
        this.id = id;
        this.name = name;
    }
}