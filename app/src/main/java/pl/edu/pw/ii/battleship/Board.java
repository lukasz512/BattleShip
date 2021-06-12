package pl.edu.pw.ii.battleship;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Board implements Serializable {
    private final Place[][] board;

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

        List<Place> shipPlaces = new ArrayList<>();
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
        for (Place[] places : board) {
            for (int j = 0; j < board[0].length; j++) {
                if (places[j].hasShip(ship)) {
                    places[j].removeShip();
                }
            }
        }
        ship.removeShip();
    }

    Place placeAt(int x, int y) {
        if (board == null || isOutOfBounds(x, y) || board[y][x] == null) {
            return null;
        }

        return board[y][x];
    }

    void hit(Place placeToHit) {
        if (placeToHit == null) {
            return;
        }
        //If place hasn't been hit before, then hits the place.
        if (!placeToHit.isHit()) {
            placeToHit.hit();
        }
    }

    boolean isOutOfBounds(int x, int y) {
        return x >= size() || y >= size() || x < 0 || y < 0;
    }

    int size() {
        return 10;
    }


    List<Place> getShipHitPlaces() {

        List<Place> boardPlaces = getPlaces();
        List<Place> shipHitPlaces = new ArrayList<>();

        for (Place place : boardPlaces) {
            if (place.isHit() && place.hasShip()) {
                shipHitPlaces.add(place);
            }
        }
        return shipHitPlaces;
    }

    void putShipHitPlace(int x, int y, Ship ship){
        Place place = board[y][x];
        place.hit();
        place.setShip(ship);
        board[y][x] = place;
    }

    List<Place> getShipSunkPlaces() {

        List<Place> boardPlaces = getPlaces();
        List<Place> shipSunkPlaces = new ArrayList<>();

        for (Place place : boardPlaces) {
            if (place.isHit() && place.hasShip() && place.isSunk()) {
                shipSunkPlaces.add(place);
            }
        }
        return shipSunkPlaces;
    }

    void setShipAsSunk(Board b, Ship ship){
        for (int i = 0; i < b.size(); i++) {
            for (int j = 0; j < b.size(); j++) {
                Place place = board[i][j];
                if (place.getShip() != null && !place.isSunk()) place.setSunk(place.getShip().getName().equals(ship.getName()));
            }
        }
    }

    private List<Place> getPlaces() {
        List<Place> boardPlaces = new LinkedList<>();
        for (int i = 0; i < size(); i++) {
            boardPlaces.addAll(Arrays.asList(board[i]).subList(0, size()));
        }
        return boardPlaces;
    }

    @Override
    public String toString() {
        StringBuilder boardString = new StringBuilder();
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
                        boardString.append("5");
                    else if (shipType.contains("battleship"))
                        boardString.append("4");
                    else if (shipType.contains("submarine"))
                        boardString.append("3");
                    else if (shipType.contains("frigate"))
                        boardString.append("2");
                    else //Sweeper
                        boardString.append("1");
                }
                //empty place
                else {
                    boardString.append("0");
                }
            }
        }

        return boardString.toString();
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
                traverseString++;
            }
        }
        return b;
    }

    static Ship decodeShipType(int shipType){
        if (shipType == 5)
            return new Ship("aircraftcarrier", 5);
        else if (shipType == 4)
            return new Ship("battleship", 4);
        else if (shipType == 3)
            return new Ship("submarine", 3);
        else if (shipType == 2)
            return new Ship("frigate", 2);
        else if (shipType == 1)
            return new Ship("minesweeper", 1);
        else {
           return null;
        }
    }
}
