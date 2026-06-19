package com.portemonnaie.model;

/**
 * Une catégorie de transaction (ex: Nourriture, Salaire...).
 * "defaultCategory" indique s'il s'agit d'une catégorie fournie par défaut
 * (non supprimable) ou d'une catégorie créée par l'utilisateur.
 */
public class Category {

    private int id;
    private String name;
    private boolean defaultCategory;

    public Category(int id, String name, boolean defaultCategory) {
        this.id = id;
        this.name = name;
        this.defaultCategory = defaultCategory;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isDefaultCategory() {
        return defaultCategory;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
