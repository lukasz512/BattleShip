package pl.edu.pw.ii.battleship;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by Lukasz Ostrowski
 */
class Player implements Serializable {
    /** UUID unique for Player */
    private String uuid;
    /** name of Player */
    private String name;

    /** name of Player */
    private UUID privateToken;

    /** String representation of ships placed on a Player's board */
    private String board;

    /** amount of points collected through the game: max 17 */
    private int points = 0;

    /**
     * Initializes Player object
     *
     * @param uuid  unique identifier to indicate Players
     * @param name  displayed in MainActivity to show the number of points
     * @param board String representation of Player's board with places where ships are located
     *              <p>
     *              points = 0 - initial value of points collected through the game
     */
    public Player(String uuid, String name, String board) {
        setUuid(uuid);
        setName(name);
        setBoard(board);
        this.privateToken = UUID.randomUUID();
    }

    public Player(String name) {
        setName(name);
        this.privateToken = UUID.randomUUID();
    }

    /**
     * Set the Player UUID
     *
     * @param uuid unique identifier to point the right Player
     */
    public void setUuid(String uuid) {
        if (uuid == null) {
            throw new RuntimeException("UUID is not valid");
        } else {
            this.uuid = uuid;
        }
    }

    /**
     * Set the Player name
     * check if string is not empty and if the length of name is at least 3
     *
     * @param name Player's name displayed in MainActivity to show the number of points
     */
    public void setName(String name) {
        if (name == null || name.trim().length() == 0) {
            throw new RuntimeException("Player cannot take in an empty String or null value for the \"name\" constructor");
        } else if (name.trim().length() < 3) {
            throw new RuntimeException("Player has to contain at least 3 characters");
        } else {
            this.name = name;
        }
    }

    /**
     * Set the Player Board
     * check if board contains 100 charakters
     *
     * @param board String representation of Player's board with places where ships are located
     */
    public void setBoard(String board) {
        if (board == null || board.trim().length() != 100) {
            throw new RuntimeException("Player's board is invalid");
        } else {
            this.board = board;
        }
    }

    /**
     * Set the Player points
     *
     * @param points from API "playerOneFieldsRemainingCount" or "playerTwoFieldsRemainingCount"
     */
    public void setPoints(int points) {
        //
        this.points = -1 * points + 17;
    }

    /**
     * Returns the player's UUID
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Returns the player's name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the player's board
     */
    public String getBoard() {
        return board;
    }

    /**
     * Returns the player's points
     */
    public int getPoints() {
        return points;
    }

    public UUID getPrivateToken() {
        return privateToken;
    }

}
