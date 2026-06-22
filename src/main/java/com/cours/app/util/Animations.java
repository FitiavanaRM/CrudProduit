package com.cours.app.util;

import java.util.List;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.Duration;

/**
 * Boîte à outils d'animations réutilisables.
 *
 * <p>JavaFX anime n'importe quelle propriété d'un nœud (opacité, position,
 * échelle, rotation...). Ces helpers regroupent les effets utilisés dans
 * l'application : apparitions en fondu, rebond, secousse, compteur animé,
 * labels flottants façon Material Design, etc.</p>
 */
public final class Animations {

    private Animations() {
    }

    /** Apparition en fondu. */
    public static void fadeIn(Node node, double ms) {
        node.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(ms), node);
        ft.setToValue(1);
        ft.setInterpolator(Interpolator.EASE_OUT);
        ft.play();
    }

    /** Disparition en fondu puis action. */
    public static void fadeOut(Node node, double ms, Runnable then) {
        FadeTransition ft = new FadeTransition(Duration.millis(ms), node);
        ft.setToValue(0);
        ft.setInterpolator(Interpolator.EASE_IN);
        if (then != null) {
            ft.setOnFinished(e -> then.run());
        }
        ft.play();
    }

    /** Apparition « pop » : léger zoom + fondu. */
    public static void popIn(Node node, double ms, double delayMs) {
        node.setOpacity(0);
        node.setScaleX(0.85);
        node.setScaleY(0.85);

        FadeTransition fade = new FadeTransition(Duration.millis(ms), node);
        fade.setToValue(1);

        ScaleTransition scale = new ScaleTransition(Duration.millis(ms), node);
        scale.setToX(1);
        scale.setToY(1);
        scale.setInterpolator(Interpolator.SPLINE(0.2, 0.9, 0.2, 1.2)); // léger rebond

        ParallelTransition pt = new ParallelTransition(node, fade, scale);
        pt.setDelay(Duration.millis(delayMs));
        pt.play();
    }

    /** Glissement vers le haut + fondu (entrée d'un élément). */
    public static void slideInUp(Node node, double distance, double ms, double delayMs) {
        node.setOpacity(0);
        node.setTranslateY(distance);

        TranslateTransition slide = new TranslateTransition(Duration.millis(ms), node);
        slide.setToY(0);
        slide.setInterpolator(Interpolator.SPLINE(0.16, 1, 0.3, 1));

        FadeTransition fade = new FadeTransition(Duration.millis(ms), node);
        fade.setToValue(1);

        ParallelTransition pt = new ParallelTransition(node, slide, fade);
        pt.setDelay(Duration.millis(delayMs));
        pt.play();
    }

    /** Fait entrer une liste de nœuds en cascade (effet « staggered »). */
    public static void staggerIn(List<? extends Node> nodes, double step, double ms) {
        double delay = 0;
        for (Node n : nodes) {
            slideInUp(n, 18, ms, delay);
            delay += step;
        }
    }

    /** Secousse horizontale (erreur de saisie). */
    public static void shake(Node node) {
        TranslateTransition t = new TranslateTransition(Duration.millis(70), node);
        t.setFromX(0);
        t.setByX(12);
        t.setCycleCount(6);
        t.setAutoReverse(true);
        t.setOnFinished(e -> node.setTranslateX(0));
        t.playFromStart();
    }

    /** Petit « battement » d'attention. */
    public static void pulse(Node node) {
        ScaleTransition t = new ScaleTransition(Duration.millis(140), node);
        t.setFromX(1);
        t.setFromY(1);
        t.setToX(1.06);
        t.setToY(1.06);
        t.setCycleCount(2);
        t.setAutoReverse(true);
        t.play();
    }

    /** Agrandit le nœud au survol de la souris (effet bouton/carte). */
    public static void hoverScale(Node node, double scale) {
        node.setOnMouseEntered(e -> scaleTo(node, scale));
        node.setOnMouseExited(e -> scaleTo(node, 1.0));
    }

    private static void scaleTo(Node node, double s) {
        ScaleTransition t = new ScaleTransition(Duration.millis(150), node);
        t.setToX(s);
        t.setToY(s);
        t.setInterpolator(Interpolator.EASE_BOTH);
        t.play();
    }

    /** Rotation continue (icône de rafraîchissement). À démarrer/arrêter par l'appelant. */
    public static RotateTransition spin(Node node) {
        RotateTransition r = new RotateTransition(Duration.millis(800), node);
        r.setByAngle(360);
        r.setCycleCount(Animation.INDEFINITE);
        r.setInterpolator(Interpolator.LINEAR);
        return r;
    }

    /** Compteur animé : fait défiler un nombre de {@code from} vers {@code to}. */
    public static void countUp(Label label, int from, int to, double ms) {
        IntegerProperty value = new SimpleIntegerProperty(from);
        value.addListener((obs, ov, nv) -> label.setText(String.valueOf(nv.intValue())));
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(ms), new KeyValue(value, to, Interpolator.EASE_OUT)));
        timeline.play();
    }

    /**
     * Label flottant (Material Design) : le libellé se place comme un
     * « placeholder » puis monte au-dessus du champ dès qu'il est actif.
     */
    public static void attachFloatingLabel(TextField field, Label label) {
        Runnable refresh = () -> moveLabel(label, isFloated(field), true);
        field.focusedProperty().addListener((o, ov, nv) -> refresh.run());
        field.textProperty().addListener((o, ov, nv) -> refresh.run());
        moveLabel(label, isFloated(field), false); // état initial sans animation
    }

    private static boolean isFloated(TextField field) {
        return field.isFocused() || (field.getText() != null && !field.getText().isEmpty());
    }

    /** Positionne un label flottant (utile quand l'état dépend de plusieurs champs). */
    public static void floatLabel(Label label, boolean floated, boolean animate) {
        moveLabel(label, floated, animate);
    }

    private static void moveLabel(Label label, boolean floated, boolean animate) {
        double targetY = floated ? -20 : 0;
        double targetScale = floated ? 0.8 : 1.0;

        if (floated) {
            if (!label.getStyleClass().contains("floating-label--active")) {
                label.getStyleClass().add("floating-label--active");
            }
        } else {
            label.getStyleClass().remove("floating-label--active");
        }

        if (!animate) {
            label.setTranslateY(targetY);
            label.setScaleX(targetScale);
            label.setScaleY(targetScale);
            return;
        }

        TranslateTransition move = new TranslateTransition(Duration.millis(150), label);
        move.setToY(targetY);
        ScaleTransition scale = new ScaleTransition(Duration.millis(150), label);
        scale.setToX(targetScale);
        scale.setToY(targetScale);
        ParallelTransition pt = new ParallelTransition(move, scale);
        pt.setInterpolator(Interpolator.EASE_BOTH);
        pt.play();
    }

    /** Enchaîne plusieurs animations l'une après l'autre. */
    public static SequentialTransition sequence(Animation... animations) {
        return new SequentialTransition(animations);
    }
}
