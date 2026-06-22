package com.cours.app.util;

import java.util.function.BooleanSupplier;

import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

/**
 * Redonne le redimensionnement à une fenêtre sans décoration.
 *
 * <p>On surveille la position de la souris près des bords du nœud : quand elle
 * s'en approche, le curseur change et un glisser redimensionne la fenêtre dans
 * la bonne direction (8 directions, coins compris).</p>
 */
public final class ResizeHelper {

    private static final int MARGIN = 7;

    private ResizeHelper() {
    }

    public static void addResizeListener(Stage stage, Region node, double minW, double minH, BooleanSupplier enabled) {
        Listener listener = new Listener(stage, node, minW, minH, enabled);
        node.setOnMouseMoved(listener::moved);
        node.setOnMousePressed(listener::pressed);
        node.setOnMouseDragged(listener::dragged);
    }

    private static final class Listener {
        private final Stage stage;
        private final Region node;
        private final double minW;
        private final double minH;
        private final BooleanSupplier enabled;

        private Cursor direction = Cursor.DEFAULT;
        private double startScreenX, startScreenY;
        private double startX, startY, startW, startH;

        Listener(Stage stage, Region node, double minW, double minH, BooleanSupplier enabled) {
            this.stage = stage;
            this.node = node;
            this.minW = minW;
            this.minH = minH;
            this.enabled = enabled;
        }

        void moved(MouseEvent e) {
            if (!enabled.getAsBoolean()) {
                direction = Cursor.DEFAULT;
                node.setCursor(Cursor.DEFAULT);
                return;
            }
            double x = e.getX();
            double y = e.getY();
            double w = node.getWidth();
            double h = node.getHeight();

            boolean left = x < MARGIN;
            boolean right = x > w - MARGIN;
            boolean top = y < MARGIN;
            boolean bottom = y > h - MARGIN;

            if (top && left) {
                direction = Cursor.NW_RESIZE;
            } else if (top && right) {
                direction = Cursor.NE_RESIZE;
            } else if (bottom && left) {
                direction = Cursor.SW_RESIZE;
            } else if (bottom && right) {
                direction = Cursor.SE_RESIZE;
            } else if (left) {
                direction = Cursor.W_RESIZE;
            } else if (right) {
                direction = Cursor.E_RESIZE;
            } else if (top) {
                direction = Cursor.N_RESIZE;
            } else if (bottom) {
                direction = Cursor.S_RESIZE;
            } else {
                direction = Cursor.DEFAULT;
            }
            node.setCursor(direction);
        }

        void pressed(MouseEvent e) {
            startScreenX = e.getScreenX();
            startScreenY = e.getScreenY();
            startX = stage.getX();
            startY = stage.getY();
            startW = stage.getWidth();
            startH = stage.getHeight();
        }

        void dragged(MouseEvent e) {
            if (direction == Cursor.DEFAULT || !enabled.getAsBoolean()) {
                return;
            }
            double dx = e.getScreenX() - startScreenX;
            double dy = e.getScreenY() - startScreenY;
            double nx = startX, ny = startY, nw = startW, nh = startH;

            boolean east = direction == Cursor.E_RESIZE || direction == Cursor.NE_RESIZE || direction == Cursor.SE_RESIZE;
            boolean west = direction == Cursor.W_RESIZE || direction == Cursor.NW_RESIZE || direction == Cursor.SW_RESIZE;
            boolean south = direction == Cursor.S_RESIZE || direction == Cursor.SE_RESIZE || direction == Cursor.SW_RESIZE;
            boolean north = direction == Cursor.N_RESIZE || direction == Cursor.NE_RESIZE || direction == Cursor.NW_RESIZE;

            if (east) {
                nw = Math.max(minW, startW + dx);
            }
            if (south) {
                nh = Math.max(minH, startH + dy);
            }
            if (west) {
                double w = Math.max(minW, startW - dx);
                nx = startX + (startW - w);
                nw = w;
            }
            if (north) {
                double h = Math.max(minH, startH - dy);
                ny = startY + (startH - h);
                nh = h;
            }

            stage.setX(nx);
            stage.setY(ny);
            stage.setWidth(nw);
            stage.setHeight(nh);
        }
    }
}
