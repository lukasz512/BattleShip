package pl.edu.pw.ii.battleship;

import java.io.Serializable;

class Player implements Serializable {
    private String uuid;
    private String name;
    private String board;
    private int points = 0;

    /**
     * Returns UUID of Player
     * used to distinguishing the players
     *
     * @return      String contains UUID of player - used to authenticate Player during the game
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Sets an UUID to the user, so server knows who is making the shoot
     *
     * @param  uuid UUID of Player - for the later recognition of the players
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * Returns name of Player
     * used to show who's turn during the game
     *
     * @return      String with player name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets an UUID to the user, so server knows who is making the shoot
     *
     * @param  name name of Player - Has to contain at least 3 characters
     */
    public void setName(String name) {
        if (name == null || name.trim().length() == 0) {
            throw new RuntimeException("Player cannot take in an empty String or null value for the \"name\" constructor");
        } else {
            this.name = name;
        }
    }

    public String getBoard() {
        return board;
    }

    public void setBoard(String board) {
        this.board = board;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        //
        this.points = -1 * points + 17;
    }
}
