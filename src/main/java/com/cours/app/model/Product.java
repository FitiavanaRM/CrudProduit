package com.cours.app.model;

import javafx.beans.property.*;

public class Product {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty nom = new SimpleStringProperty();
    private final DoubleProperty prix = new SimpleDoubleProperty();
    private final IntegerProperty quantite = new SimpleIntegerProperty();
    private final BooleanProperty disponible = new SimpleBooleanProperty();

    public Product() {
    }

    public Product(int id, String nom, double prix, int quantite, boolean disponible) {
        setId(id);
        setNom(nom);
        setPrix(prix);
        setQuantite(quantite);
        setDisponible(disponible);
    }

    public int getId() {
        return id.get();
    }

    public void setId(int value) {
        id.set(value);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public String getNom() {
        return nom.get();
    }

    public void setNom(String value) {
        nom.set(value);
    }

    public StringProperty nomProperty() {
        return nom;
    }

    public double getPrix() {
        return prix.get();
    }

    public void setPrix(double value) {
        prix.set(value);
    }
    public DoubleProperty prixProperty() {
        return prix;
    }

    public int getQuantite() {
        return quantite.get();
    }

    public void setQuantite(int value) {
        quantite.set(value);
    }

    public IntegerProperty quantiteProperty() {
        return quantite;
    }

    public boolean isDisponible() {
        return disponible.get();
    }

    public void setDisponible(boolean value) {
        disponible.set(value);
    }

    public BooleanProperty disponibleProperty() {
        return disponible;
    }
}