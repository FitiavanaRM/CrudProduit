package com.cours.app.dao;

import com.cours.app.db.Database;
import com.cours.app.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/* :
 *   - findAll() :liste tous les produits
 *   - findById(id) : retourne un produit par son id
 *   - insert(produit) : ajoute un nouveau produit
 *   - update(produit) : modifie un produit existant
 *   - delete(id) : supprime un produit par son id
 *   - count() : retourne le nombre total de produits
 */
public class ProduitDAO {

    public List<Product> findAll() throws Exception {
        List<Product> liste = new ArrayList<>();

        // triés par id
        String sql = "SELECT id, nom, prix, quantite, disponible FROM produits ORDER BY id";

        try (Statement stmt = Database.getConnection().createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Construire un objet Produit pour chaque ligne retournée
                Product p = new Product(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getDouble("prix"),
                    rs.getInt("quantite"),
                    rs.getBoolean("disponible")
                );
                liste.add(p);
            }
        }
        return liste;
    }

    // Lire
    public Product findById(int id) throws Exception {
        String sql = "SELECT id, nom, prix, quantite, disponible FROM produits WHERE id = ?";

        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Product(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getDouble("prix"),
                        rs.getInt("quantite"),
                        rs.getBoolean("disponible")
                    );
                }
            }
        }
        return null;
    }

    // creation
    public void insert(Product p) throws Exception {
        String sql = "INSERT INTO produits (nom, prix, quantite, disponible) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            // Chaque "?" correspond à un paramètre dans l'ordre
            ps.setString(1,  p.getNom());
            ps.setDouble(2,  p.getPrix());
            ps.setInt(3,     p.getQuantite());
            ps.setBoolean(4, p.isDisponible());

            int lignesAffectees = ps.executeUpdate();
            System.out.println("[ProduitDAO] INSERT → " + lignesAffectees + " ligne(s) ajoutée(s).");
        }
    }

    // modification
    public void update(Product p) throws Exception {
        String sql = "UPDATE produits SET nom = ?, prix = ?, quantite = ?, disponible = ? WHERE id = ?";

        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setString(1,  p.getNom());
            ps.setDouble(2,  p.getPrix());
            ps.setInt(3,     p.getQuantite());
            ps.setBoolean(4, p.isDisponible());
            ps.setInt(5,     p.getId());

            int lignesAffectees = ps.executeUpdate();
            System.out.println("[ProduitDAO] UPDATE id=" + p.getId() + " : " + lignesAffectees + " ligne(s) modifiée(s).");
        }
    }

    // delete
    public void delete(int id) throws Exception {
        String sql = "DELETE FROM produits WHERE id = ?";

        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);

            int lignesAffectees = ps.executeUpdate();
            System.out.println("[ProduitDAO] DELETE id=" + id + " → " + lignesAffectees + " ligne(s) supprimée(s).");
        }
    }

    // compter
    public int count() throws Exception {
        String sql = "SELECT COUNT(*) FROM produits";

        try (Statement stmt = Database.getConnection().createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    // compte des produits disponible
    public int countDisponibles() throws Exception {
        String sql = "SELECT COUNT(*) FROM produits WHERE disponible = TRUE";

        try (Statement stmt = Database.getConnection().createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}