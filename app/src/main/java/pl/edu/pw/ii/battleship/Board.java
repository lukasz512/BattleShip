package pl.edu.pw.ii.battleship;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Board implements Serializable {
    private final int size = 10;
    private Place[][] board = null;

    public Board() {
        board = new Place[size()][size()];
        createBoard(board);
    }

    private void createBoard(Place[][] board) {

        for (int y = 0; y < board.length; y++) {
            for (int x = 0; x < board[0].length; x++) {
                board[y][x] = new Place(x, y);
            }
        }
    }

    boolean placeShip(Ship ship, int x, int y, boolean dir) {
        if (ship == null) {
            return false;
        }

        removeShip(ship);

        List<Place> shipPlaces = new ArrayList<Place>();
        Place place;

        //Goes through places where ship will be placed.*/
        for (int i = 0; i < ship.getSize(); i++) {
            //If dir is true, then ship will be placed horizontally, otherwise vertically
            if (dir) {
                place = placeAt(x + i, y);
            } else {
                place = placeAt(x, y + i);
            }

            //If place was invalid or already had a ship, returns false and doesn't place ship
            if (place == null || place.hasShip()) {
                return false;
            }

            //If was a valid place then adds to list of places, and looks through other places
            shipPlaces.add(place);
        }

        // Gives a reference to the ship to all the places that will have the ship
        for (Place placeWithShip : shipPlaces) {
            placeWithShip.setShip(ship);
        }

        ship.setDir(dir);
        ship.placeShip(shipPlaces);

        return true;
    }

    private void removeShip(Ship ship) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (board[i][j].hasShip(ship)) {
                    board[i][j].removeShip();
                }
            }
        }
        ship.removeShip();
    }

    /**
     * Returns the place in the board with coordinates (x, y)
     *
     * @param x x coordinate 0-based index
     * @param y y coordinate 0-based index
     * @return place on the board
     */
    Place placeAt(int x, int y) {
        if (board == null || isOutOfBounds(x, y) || board[y][x] == null) {
            return null;
        }

        return board[y][x];
    }

    /**
     * Hits given place, returns true if was able to successfully hit the
     *
     * @param placeToHit place you want to hit
     */
    boolean hit(Place placeToHit) {
        if (placeToHit == null) {
            return false;
        }
        //If place hasn't been hit before, then hits the place.
        if (!placeToHit.isHit()) {
            placeToHit.hit();
            return true;
        }
        return false;
    }

    boolean isOutOfBounds(int x, int y) {
        return x >= size() || y >= size() || x < 0 || y < 0;
    }

    int size() {
        return size;
    }


    List<Place> getShipHitPlaces() {

        List<Place> boardPlaces = getPlaces();
        List<Place> shipHitPlaces = new ArrayList<Place>();

        for (Place place : boardPlaces) {
            if (place.isHit() && place.hasShip()) {
                shipHitPlaces.add(place);
            }
        }
        return shipHitPlaces;
    }

    void putShipHitPlace(int x, int y){
        Place place = board[y][x];
        place.hit();
        place.setShip(new Ship("ship", 9));
        board[y][x] = place;
    }

    private List<Place> getPlaces() {
        List<Place> boardPlaces = new LinkedList<Place>();
        for (int i = 0; i < size(); i++) {
            for (int j = 0; j < size(); j++) {
                boardPlaces.add(board[i][j]);
            }
        }
        return boardPlaces;
    }

    @Override
    public String toString() {
        String boardString = "";
        if (board == null) {
            return "Board is null";
        }
        for (int i = 0; i < board[0].length; i++) {
            for (int j = 0; j < board.length; j++) {
                Place place = board[i][j];
                Ship ghost = place.getShip();

                if (ghost != null) {
                    String shipType = ghost.getName();
                    if (shipType.contains("aircraftcarrier"))
                        boardString += "5";
                    else if (shipType.contains("battleship"))
                        boardString += "4";
                    else if (shipType.contains("submarine"))
                        boardString += "3";
                    else if (shipType.contains("frigate"))
                        boardString += "2";
                    else //Sweeper
                        boardString += "1";
                }
                //empty place
                else {
                    boardString += "0";
                }
            }
        }

        return boardString;
    }

    static Board decipherPlaceShips(String opponentBoard) {
        Board b = new Board();
        int traverseString = 0;
        char[] tb = opponentBoard.toCharArray();
        for (int i = 0; i < b.size(); i++) {
            for (int j = 0; j < b.size(); j++) {
                int shipType = tb[traverseString];
                Place place = b.placeAt(j, i);

                if (shipType == '5')
                    place.setShip(new Ship("aircraftcarrier", 5));
                else if (shipType == '4')
                    place.setShip(new Ship("battleship", 4));
                else if (shipType == '3')
                    place.setShip(new Ship("submarine", 3));
                else if (shipType == '2')
                    place.setShip(new Ship("frigate", 2));
                else if (shipType == '1')
                    place.setShip(new Ship("minesweeper", 1));
                else {
                    //Don't set a ship
                }
                traverseString++;
            }
        }
        return b;
    }
}
