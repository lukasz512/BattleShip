package pl.edu.pw.ii.battleship;

import java.io.Serializable;

public class Game implements Serializable {
    private String gameUUID;
    private String shootingPlayerUUID;

    public String getGameUUID() {
        return gameUUID;
    }

    public void setGameUUID(String gameUUID) {
        this.gameUUID = gameUUID;
    }

    public String getShootingPlayer() {
        return shootingPlayerUUID;
    }

    public void setShootingPlayer(String shootingPlayer) {
        this.shootingPlayerUUID = shootingPlayer;
    }
}
