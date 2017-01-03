package uk.co.streefland.rhys.finalyearproject.gui.visualiser;

import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ZoomEvent;
import javafx.scene.transform.Scale;

/** Enables zooming of the graph */
public class ZoomableScrollPane extends ScrollPane {

    private Group zoomGroup;
    private Scale scaleTransform;
    private double scaleValue = 1.0;

    public ZoomableScrollPane(Node content) {
        Group contentGroup = new Group();
        zoomGroup = new Group();
        contentGroup.getChildren().add(zoomGroup);
        zoomGroup.getChildren().add(content);
        setContent(contentGroup);
        scaleTransform = new Scale(scaleValue, scaleValue, 0, 0);
        zoomGroup.getTransforms().add(scaleTransform);

        zoomGroup.setOnZoom(new ZoomHandler());
    }

    public double getScaleValue() {
        return scaleValue;
    }

    public void zoomTo(double scaleValue) {
        this.scaleValue = scaleValue;

        scaleTransform.setX(scaleValue);
        scaleTransform.setY(scaleValue);
    }

    private class ZoomHandler implements EventHandler<ZoomEvent> {
        @Override
        public void handle(ZoomEvent zoomEvent) {
            scaleValue = scaleValue * zoomEvent.getZoomFactor();
            zoomTo(scaleValue);
            zoomEvent.consume();
        }
    }
}