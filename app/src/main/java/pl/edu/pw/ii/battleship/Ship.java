package pl.edu.pw.ii.battleship;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

class Ship implements Serializable {
    private final String name;
    private final int size;
    private boolean dir = true;
    private List<Place> placed = new ArrayList<>();

    public Ship(String name, int size) {
        this.name = name;
        this.size = size;
    }

    int getSize() {
        return size;
    }

    public String getName() {
        return name;
    }

    void placeShip(List<Place> places) {
        placed = places;
    }

    void setDir(boolean newDir) {
        dir = newDir;
    }

    boolean getDir() {
        return dir;
    }

    boolean isPlaced() {
        return !placed.isEmpty();
    }


    List<Place> getPlacement() {
        return placed;
    }

    void removeShip() {
        placed.clear();
    }
}
