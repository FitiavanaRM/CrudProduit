package com.cours.app.db;

import com.cours.app.config.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Database — Gère la connexion unique à MySQL (pattern Singleton).
 *
 * Responsabilité : ouvrir et fournir une connexion JDBC réutilisable.
 * Les paramètres viennent d'AppConfig (qui lit config.xml).
 *
 * Utilisé par : ProduitDAO, SchemaInitializer
 */
public class Database {

    // La connexion est gardée ouverte pendant toute la session
    private static Connection connection = null;

    /**
     * Retourne la connexion active.
     * Si elle est nulle ou fermée, en crée une nouvelle automatiquement.
     *
     * @return Connection JDBC vers MySQL
     * @throws Exception si la connexion échoue (mauvais paramètres, serveur absent, etc.)
     */
    public static Connection getConnection() throws Exception {

        // Réutiliser la connexion existante si elle est encore ouverte
        if (connection != null && !connection.isClosed()) {
            return connection;
        }

        // Charger le driver MySQL (nécessaire pour certaines versions de JDK)
        Class.forName("com.mysql.cj.jdbc.Driver");

        // Récupérer l'URL et les identifiants depuis AppConfig
        AppConfig config = AppConfig.getInstance();

        // Ouvrir la connexion
        connection = DriverManager.getConnection(
            config.buildJdbcUrl(),
            config.getUsername(),
            config.getPassword()
        );

        System.out.println("[Database] Connexion MySQL établie sur : " + config.buildJdbcUrl());
        return connection;
    }

    /**
     * Ferme proprement la connexion.
     * À appeler dans MainApp lors de la fermeture de l'application.
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("[Database] Connexion MySQL fermée.");
            } catch (Exception e) {
                System.err.println("[Database] Erreur fermeture : " + e.getMessage());
            }
        }
    }
}