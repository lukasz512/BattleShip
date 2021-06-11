package pl.edu.pw.ii.battleship;

import java.io.Serializable;

public class Place implements Serializable {
    private int x = 0;
    private int y = 0;
    private boolean isHit = false;
    /**Place can have 1 ship, if it doesn't have a ship then it is null*/
    private Ship ship = null;

    private boolean isSunk;
    /**Initializes place with x and y coordinates
     * @param x is the x-coordinate of place, 0-based index
     * @param y is the y-coordinate of place, 0-based index*/
    public Place(int x, int y){
        this.x = x;
        this.y = y;
    }
    boolean isHit(){
        return isHit;
    }

    /**Marks the place as hit*/
    void hit() {
        isHit = true;
    }

    /**Gets X-coordinate, 0-based index*/
    public int getX(){
        return x;
    }

    /**Gets Y-coordinate, 0-based index*/
    public int getY(){
        return y;
    }

    /**Returns true if place has a ship*/
    boolean hasShip() {
        return ship != null;
    }

    /**Checks if place contains the ship
     * @param shipToCheck is the ship you want to find is in the place*/
    boolean hasShip(Ship shipToCheck) {
        return ship == shipToCheck;
    }

    /**Removes the ship from the place*/
    void removeShip(){
        ship = null;
    }

    /**Sets a ship on the place*/
    protected void setShip(Ship ship){
        this.ship = ship;
    }

    /**Returns the ship that is in the place, null if no ship is in the place*/
    public Ship getShip(){
        return ship;
    }

    public boolean isSunk() {
        return isSunk;
    }

    public void setSunk(boolean bool){
        this.isSunk = bool;
    }
}