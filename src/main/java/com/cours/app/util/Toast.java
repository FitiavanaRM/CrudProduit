package com.cours.app.util;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * Notifications « toast » animées, affichées en bas de l'écran puis
 * disparaissant automatiquement.
 */
public final class Toast {

    public enum Type {
        SUCCESS, ERROR, INFO
    }

    private Toast() {
    }

    public static void success(StackPane host, String message) {
        show(host, message, Type.SUCCESS);
    }

    public static void error(StackPane host, String message) {
        show(host, message, Type.ERROR);
    }

    public static void info(StackPane host, String message) {
        show(host, message, Type.INFO);
    }

    public static void show(StackPane host, String message, Type type) {
        Label toast = new Label(message);
        toast.getStyleClass().addAll("toast", "toast--" + type.name().toLowerCase());
        toast.setMouseTransparent(true);
        toast.setOpacity(0);
        toast.setTranslateY(40);

        StackPane.setAlignment(toast, Pos.BOTTOM_CENTER);
        StackPane.setMargin(toast, new Insets(0, 0, 32, 0));
        host.getChildren().add(toast);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(280), toast);
        slideIn.setToY(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(280), toast);
        fadeIn.setToValue(1);
        ParallelTransition in = new ParallelTransition(slideIn, fadeIn);

        PauseTransition stay = new PauseTransition(Duration.millis(2200));

        TranslateTransition slideOut = new TranslateTransition(Duration.millis(260), toast);
        slideOut.setToY(40);
        FadeTransition fadeOut = new FadeTransition(Duration.millis(260), toast);
        fadeOut.setToValue(0);
        ParallelTransition out = new ParallelTransition(slideOut, fadeOut);

        SequentialTransition seq = new SequentialTransition(in, stay, out);
        seq.setOnFinished(e -> host.getChildren().remove(toast));
        seq.play();
    }
}
