package uk.co.streefland.rhys.finalyearproject.gui.visualiser;

import java.util.List;
import java.util.Random;

public class RandomLayout extends Layout {

    Graph graph;

    Random rnd = new Random();

    public RandomLayout(Graph graph) {

        this.graph = graph;

    }

    public void execute() {

        List<Cell> cells = graph.getModel().getAllCells();

        for (Cell cell : cells) {

            double x = rnd.nextDouble() * 500;
            double y = rnd.nextDouble() * 500;

            cell.relocate(x, y);

            boolean collisionDetected;

            do {
                collisionDetected = detectCollision(cells, cell);
            } while (collisionDetected);
        }
    }

    private boolean detectCollision(List<Cell> cells, Cell cell) {
        for (Cell cell2 : cells) {
            if (!cell.equals(cell2)) {
                if (cell.getBoundsInParent().intersects(cell2.getBoundsInParent())) {

                    /* New random position for cell */
                    double x = rnd.nextDouble() * 500;
                    double y = rnd.nextDouble() * 500;
                    cell.relocate(x, y);

                    return true;
                }
            }
        }
        return false;
    }
}