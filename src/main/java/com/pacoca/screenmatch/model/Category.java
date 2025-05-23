package com.pacoca.screenmatch.model;

public enum Category {
    COMEDY ("comedy"),
    ACTION ("action"),
    ADVENTURE ("adventure"),
    FANTASY ("fantasy"),
    SCIENCE_FICTION ("sci-fi"),
    THRILLER ("thriller"),
    HORROR ("horror"),
    ROMANCE ("romance"),
    MYSTERY ("mystery"),
    DRAMA ("drama"),
    CRIME ("crime");

    private String categoryOmdb;

    Category(String categoryOmdb) {
        this.categoryOmdb = categoryOmdb;
    }

    public static Category fromString(String text) {
        for (Category genre : Category.values()) {
            if (genre.categoryOmdb.equalsIgnoreCase(text)) {
                return genre;
            }
        }
        throw new IllegalArgumentException("Nenhuma categoria encontrada para a string fornecida: " + text);
    }
}


