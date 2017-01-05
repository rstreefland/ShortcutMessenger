package uk.co.streefland.rhys.finalyearproject.gui.visualiser;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

/**
 * Enables the dragging of nodes usign the mouse
 */
class MouseGestures {

    private final DragContext dragContext = new DragContext();
    private final Graph graph;

    public MouseGestures( Graph graph) {
        this.graph = graph;
    }

    public void makeDraggable( final Node node) {
        node.setOnMousePressed(onMousePressedEventHandler);
        node.setOnMouseDragged(onMouseDraggedEventHandler);
    }

    private final EventHandler<MouseEvent> onMousePressedEventHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {

            Node node = (Node) event.getSource();

            double scale = graph.getScale();

            dragContext.x = node.getBoundsInParent().getMinX() * scale - event.getScreenX();
            dragContext.y = node.getBoundsInParent().getMinY()  * scale - event.getScreenY();
        }
    };

    private final EventHandler<MouseEvent> onMouseDraggedEventHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {

            Node node = (Node) event.getSource();

            double offsetX = event.getScreenX() + dragContext.x;
            double offsetY = event.getScreenY() + dragContext.y;

            // adjust the offset in case we are zoomed
            double scale = graph.getScale();

            offsetX /= scale;
            offsetY /= scale;

            node.relocate(offsetX, offsetY);
        }
    };

    class DragContext {
        double x;
        double y;
    }
}