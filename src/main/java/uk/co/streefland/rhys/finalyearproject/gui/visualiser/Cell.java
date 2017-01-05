package uk.co.streefland.rhys.finalyearproject.gui.visualiser;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;

import java.util.ArrayList;
import java.util.List;

/** Represents a node on the graph */
class Cell extends Pane {

    private final KeyId cellId;

    private final List<Cell> children = new ArrayList<>();
    private final List<Cell> parents = new ArrayList<>();

    public Cell(KeyId cellId) {
        this.cellId = cellId;
    }

    public void addCellChild(Cell cell) {
        children.add(cell);
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

    void setView(Node view) {
        getChildren().add(view);
    }

    public KeyId getCellId() {
        return cellId;
    }
}