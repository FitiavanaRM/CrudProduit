package com.cours.app;

import java.io.IOException;
import java.net.URL;

import com.cours.app.controller.DashboardController;
import com.cours.app.db.SchemaInitializer;
import com.cours.app.model.Product;
import com.cours.app.ui.WindowFrame;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class MainApp extends Application {

    private static final String CSS = "/com/cours/app/css/app.css";

    private static Scene scene;
    private static Stage primaryStage;
    private static WindowFrame windowFrame;
    private static boolean darkMode = true; // thème sombre par défaut

    @Override
    public void start(Stage stage) {
        primaryStage = stage;

        try {
            SchemaInitializer.init();
        } catch (RuntimeException ex) {
            showFatal(ex);
        }

        Parent initialView = load("login");
        windowFrame = new WindowFrame(stage, initialView);

        scene = new Scene(windowFrame.getRoot(), 1180, 760);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(resource(CSS).toExternalForm());
        applyThemeTo(windowFrame.getRoot());
        applyThemeTo(initialView);

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setScene(scene);
        stage.setTitle("Cours Java — Gestion des utilisateurs (JavaFX)");
        stage.setMinWidth(1000);
        stage.setMinHeight(700);
        stage.show();
        stage.centerOnScreen();

        // Filet de sécurité : ne jamais démarrer sous la barre de menu macOS.
        Rectangle2D visual = Screen.getPrimary().getVisualBounds();
        if (stage.getY() < visual.getMinY()) {
            stage.setY(visual.getMinY());
        }

        FadeTransition intro = new FadeTransition(Duration.millis(500), windowFrame.getRoot());
        intro.setFromValue(0);
        intro.setToValue(1);
        intro.play();
    }

    // ---------- navigation ----------

    public static void goToDashboard(Product P) {
        try {
            FXMLLoader loader = new FXMLLoader(resource("/com/cours/app/view/dashboard.fxml"));
            Parent root = loader.load();
            DashboardController controller = loader.getController();
            controller.setCurrentUser(P);
            swapRoot(root);
        } catch (IOException e) {
            throw new RuntimeException("Chargement du dashboard impossible", e);
        }
    }

    public static void goToLogin() {
        swapRoot(load("login"));
    }

    private static Parent load(String view) {
        try {
            return new FXMLLoader(resource("/com/cours/app/view/" + view + ".fxml")).load();
        } catch (IOException e) {
            throw new RuntimeException("Chargement de la vue '" + view + "' impossible", e);
        }
    }

    /** Remplace la racine de la scène avec un fondu enchaîné. */
    private static void swapRoot(Parent next) {
        applyThemeTo(next);
        StackPane content = windowFrame.getContent();
        Parent current = (Parent) content.getChildren().get(0);

        FadeTransition out = new FadeTransition(Duration.millis(220), current);
        out.setToValue(0);
        out.setOnFinished(e -> {
            next.setOpacity(0);
            content.getChildren().setAll(next);
            FadeTransition in = new FadeTransition(Duration.millis(340), next);
            in.setToValue(1);
            in.play();
        });
        out.play();
    }

    // ---------- thème ----------

    public static void toggleTheme() {
        darkMode = !darkMode;
        applyThemeTo(windowFrame.getRoot());
        Node view = windowFrame.getContent().getChildren().get(0);
        if (view instanceof Parent p) {
            applyThemeTo(p);
        }
    }

    public static boolean isDarkMode() {
        return darkMode;
    }

    private static void applyThemeTo(Parent root) {
        root.getStyleClass().removeAll("dark", "light");
        root.getStyleClass().add(darkMode ? "dark" : "light");
    }

    // ---------- divers ----------

    public static Stage stage() {
        return primaryStage;
    }

    private static URL resource(String path) {
        return MainApp.class.getResource(path);
    }

    private void showFatal(RuntimeException ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Base de données");
        alert.setHeaderText("Connexion à la base impossible");
        alert.setContentText(ex.getMessage()
                + "\n\nVérifie config.xml (moteur actif) et, pour MySQL, que le serveur est démarré.");
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
