package pl.edu.pw.ii.battleship;

import java.io.Serializable;

public class Place implements Serializable {
    private final int x;
    private final int y;
    private boolean isHit = false;
    private Ship ship = null;

    private boolean isSunk;

    public Place(int x, int y) {
        this.x = x;
        this.y = y;
    }

    boolean isHit() {
        return isHit;
    }

    void hit() {
        isHit = true;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    boolean hasShip() {
        return ship != null;
    }

    boolean hasShip(Ship shipToCheck) {
        return ship == shipToCheck;
    }

    void removeShip() {
        ship = null;
    }

    protected void setShip(Ship ship) {
        this.ship = ship;
    }

    public Ship getShip() {
        return ship;
    }

    public boolean isSunk() {
        return isSunk;
    }

    public void setSunk(boolean bool) {
        this.isSunk = bool;
    }
}