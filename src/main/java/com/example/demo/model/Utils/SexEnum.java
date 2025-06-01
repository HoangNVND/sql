package com.example.demo.model.Utils;

public enum SexEnum {
    MALE("Male"),

    FEMALE("Female");


    private final String label;

    SexEnum(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
