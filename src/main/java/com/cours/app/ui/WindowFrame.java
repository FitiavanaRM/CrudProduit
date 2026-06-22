package com.cours.app.ui;

import com.cours.app.util.ResizeHelper;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Habillage de fenêtre personnalisé (sans bordure native).
 *
 * <p>Construit autour de la vue : une barre de titre maison (logo + titre +
 * boutons réduire / agrandir / fermer), des coins arrondis avec ombre portée,
 * le déplacement à la souris, le double-clic pour agrandir, et le
 * redimensionnement par les bords.</p>
 *
 * <pre>
 *  outer (transparent, marge pour l'ombre)
 *   └─ surface (arrondie + ombre, découpée)
 *        ├─ titleBar
 *        └─ content (StackPane : les vues login / dashboard s'y échangent)
 * </pre>
 */
public class WindowFrame {

    private static final double CORNER = 16;
    private static final double SHADOW_PAD = 24;
    private static final double MIN_W = 1000;
    private static final double MIN_H = 680;

    private final Stage stage;
    private final StackPane outer;
    private final BorderPane surface;
    private final StackPane content;
    private final Rectangle clip;
    private Button maxButton;

    private boolean maximized = false;
    private double prevX, prevY, prevW, prevH;
    private double dragOffsetX, dragOffsetY;

    public WindowFrame(Stage stage, Parent initialView) {
        this.stage = stage;

        content = new StackPane(initialView);
        content.getStyleClass().add("window-content");

        surface = new BorderPane();
        surface.getStyleClass().add("window-surface");
        surface.setTop(buildTitleBar());
        surface.setCenter(content);

        clip = new Rectangle();
        clip.setArcWidth(CORNER * 2);
        clip.setArcHeight(CORNER * 2);
        clip.widthProperty().bind(surface.widthProperty());
        clip.heightProperty().bind(surface.heightProperty());
        surface.setClip(clip);

        outer = new StackPane(surface);
        // « app-root » porte les variables de thème (couleurs) utilisées par la barre de titre.
        outer.getStyleClass().addAll("app-root", "window-root");
        outer.setPadding(new Insets(SHADOW_PAD));

        ResizeHelper.addResizeListener(stage, surface, MIN_W, MIN_H, () -> !maximized);
    }

    public StackPane getRoot() {
        return outer;
    }

    public StackPane getContent() {
        return content;
    }

    // ---------- barre de titre ----------

    private HBox buildTitleBar() {
        HBox bar = new HBox();
        bar.getStyleClass().add("title-bar");
        bar.setAlignment(Pos.CENTER_LEFT);

        Label title = label("Cours Java — Gestion des utilisateurs", "window-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button minButton = winButton("—", "win-btn");
        minButton.setOnAction(e -> stage.setIconified(true));

        maxButton = winButton("□", "win-btn");
        maxButton.setOnAction(e -> toggleMaximize());

        Button closeButton = winButton("✕", "win-btn", "win-close");
        closeButton.setOnAction(e -> {
            stage.close();
            Platform.exit();
        });

        HBox controls = new HBox(6, minButton, maxButton, closeButton);
        controls.setAlignment(Pos.CENTER);

        bar.getChildren().addAll(title, spacer, controls);

        // Déplacement de la fenêtre
        bar.setOnMousePressed(e -> {
            dragOffsetX = e.getScreenX() - stage.getX();
            dragOffsetY = e.getScreenY() - stage.getY();
        });
        bar.setOnMouseDragged(e -> {
            if (maximized) {
                return;
            }
            Rectangle2D vb = Screen.getPrimary().getVisualBounds();
            double newX = e.getScreenX() - dragOffsetX;
            double newY = e.getScreenY() - dragOffsetY;
            // Empêcher la fenêtre de passer sous la barre de menu (haut)
            // et de descendre trop bas (la barre de titre reste attrapable).
            newY = Math.max(vb.getMinY(), Math.min(newY, vb.getMaxY() - 80));
            stage.setX(newX);
            stage.setY(newY);
        });
        bar.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                toggleMaximize();
            }
        });

        return bar;
    }

    // ---------- agrandir / restaurer ----------

    private void toggleMaximize() {
        if (!maximized) {
            prevX = stage.getX();
            prevY = stage.getY();
            prevW = stage.getWidth();
            prevH = stage.getHeight();

            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());

            maximized = true;
            maxButton.setText("❐"); // restaurer
            applyMaximizedStyle(true);
        } else {
            stage.setX(prevX);
            stage.setY(prevY);
            stage.setWidth(prevW);
            stage.setHeight(prevH);

            maximized = false;
            maxButton.setText("□");
            applyMaximizedStyle(false);
        }
    }

    private void applyMaximizedStyle(boolean max) {
        if (max) {
            outer.setPadding(Insets.EMPTY);
            clip.setArcWidth(0);
            clip.setArcHeight(0);
            if (!surface.getStyleClass().contains("maximized")) {
                surface.getStyleClass().add("maximized");
            }
        } else {
            outer.setPadding(new Insets(SHADOW_PAD));
            clip.setArcWidth(CORNER * 2);
            clip.setArcHeight(CORNER * 2);
            surface.getStyleClass().remove("maximized");
        }
    }

    // ---------- helpers ----------

    private static Label label(String text, String styleClass) {
        Label l = new Label(text);
        l.getStyleClass().add(styleClass);
        return l;
    }

    private static Button winButton(String glyph, String... styleClasses) {
        Button b = new Button(glyph);
        b.getStyleClass().addAll(styleClasses);
        b.setFocusTraversable(false);
        return b;
    }
}
