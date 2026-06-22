package com.cours.app.util;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * Interrupteur animé (style iOS) : un rail arrondi et un bouton rond qui
 * glisse d'un côté à l'autre. Démontre comment créer un contrôle JavaFX
 * personnalisé avec animation et pseudo-classe CSS.
 */
public class ToggleSwitch extends StackPane {

    private static final PseudoClass ON = PseudoClass.getPseudoClass("on");

    private static final double WIDTH = 54;
    private static final double HEIGHT = 30;
    private static final double KNOB = 22;
    private static final double PAD = 4;

    private final BooleanProperty selected = new SimpleBooleanProperty(this, "selected", false);
    private final Region knob = new Region();

    public ToggleSwitch() {
        getStyleClass().add("toggle-switch");
        setMinSize(WIDTH, HEIGHT);
        setPrefSize(WIDTH, HEIGHT);
        setMaxSize(WIDTH, HEIGHT);
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(PAD));
        setCursor(javafx.scene.Cursor.HAND);

        knob.getStyleClass().add("knob");
        knob.setMinSize(KNOB, KNOB);
        knob.setPrefSize(KNOB, KNOB);
        knob.setMaxSize(KNOB, KNOB);
        getChildren().add(knob);

        setOnMouseClicked(e -> setSelected(!isSelected()));
        selected.addListener((obs, was, now) -> refresh(true));
        refresh(false);
    }

    private void refresh(boolean animate) {
        pseudoClassStateChanged(ON, isSelected());
        double target = isSelected() ? (WIDTH - KNOB - 2 * PAD) : 0;
        if (animate) {
            TranslateTransition slide = new TranslateTransition(Duration.millis(170), knob);
            slide.setToX(target);
            slide.setInterpolator(Interpolator.EASE_BOTH);
            slide.play();
        } else {
            knob.setTranslateX(target);
        }
    }

    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean value) {
        selected.set(value);
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }
}
