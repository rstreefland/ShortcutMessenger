package uk.co.streefland.rhys.finalyearproject.gui.visualiser;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;

import java.util.ArrayList;
import java.util.List;

public class Cell extends Pane {

    KeyId cellId;

    List<Cell> children = new ArrayList<>();
    List<Cell> parents = new ArrayList<>();

    Node view;

    public Cell(KeyId cellId) {
        this.cellId = cellId;
    }

    public void addCellChild(Cell cell) {
        children.add(cell);
    }

    public List<Cell> getCellChildren() {
        return children;
    }

    public void addCellParent(Cell cell) {
        parents.add(cell);
    }

    public List<Cell> getCellParents() {
        return parents;
    }

    public void removeCellChild(Cell cell) {
        children.remove(cell);
    }

    public void setView(Node view) {

        this.view = view;
        getChildren().add(view);

    }

    public Node getView() {
        return this.view;
    }

    public KeyId getCellId() {
        return cellId;
    }
}