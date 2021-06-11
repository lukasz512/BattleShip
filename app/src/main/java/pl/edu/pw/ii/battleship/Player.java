package pl.edu.pw.ii.battleship;

import java.io.Serializable;

class Player implements Serializable {
    private String uuid;
    private String name;
    private String board;
    private int points = 0;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

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
