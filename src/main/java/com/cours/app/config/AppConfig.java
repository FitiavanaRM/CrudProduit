package com.cours.app.config;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

// lire le fichier config.xml
public class AppConfig {

    private static final String path = "config.xml";

    private final String host;
    private final String port;
    private final String dbName;
    private final String username;
    private final String password;

    private static AppConfig instance;

    private AppConfig() {
        try {
            File xmlFile = new File(path);

            if (!xmlFile.exists()) {
                throw new RuntimeException("Fichier config.xml introuvable!\n" + "Chemin: " + xmlFile.getAbsolutePath());
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);
            document.getDocumentElement().normalize();

            // valeur correspondant
            this.host = getTag(document,"host");
            this.port = getTag(document,"port");
            this.dbName = getTag(document,"name");
            this.username = getTag(document,"username");
            this.password = getTag(document,"password");

        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new RuntimeException("Erreur lors de la lecture de config.xml : " + exception.getMessage(), exception);
        }
    }

    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    // construit l'URL

    public String buildJdbcUrl() {
        return "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    }

    public AppConfig(String host, String port, String dbName, String username, String password) {
        this.host = host;
        this.port = port;
        this.dbName = dbName;
        this.username = username;
        this.password = password;
    }

    private String getTag(Document document, String tagName) {
        Element element = (Element) document.getElementsByTagName(tagName).item(0);
        if (element == null) {
            throw new RuntimeException("<" + tagName + "> manquante dans config.xml");
        }
        return element.getTextContent().trim();
    }
}