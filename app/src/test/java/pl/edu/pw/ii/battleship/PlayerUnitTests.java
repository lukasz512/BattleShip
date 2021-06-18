package pl.edu.pw.ii.battleship;


import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class PlayerUnitTests {

    String uuidValid = "662568f6-a6a2-4d6c-b4ae-3c0691ec0338";
    String nameValid = "Lukasz";
    String boardValid = "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
    int pointsFromServer = 15;

    String uuidInvalid = null;
    String nameEmpty = "";
    String nameToShort = "x";
    String boardInvalid = "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";



    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();


    @Test
    public void returnCorrectUuid() {
        Player player = new Player(uuidValid, nameValid, boardValid);
        String uuid = player.getUuid();
        assertEquals(uuid, uuidValid);
    }

    @Test
    public void returnCorrectName() {
        Player player = new Player(uuidValid, nameValid, boardValid);
        String name = player.getName();
        assertEquals(name, nameValid);
    }

    @Test
    public void returnCorrectUUID() {
        Player player = new Player(uuidValid, nameValid, boardValid);
        UUID uuid = player.getPrivateToken();
        assertEquals(uuid, uuid);
    }

    @Test
    public void createPlayerCorrect() {
        Player player = new Player(nameValid);
        String name = player.getName();
        assertEquals(name, nameValid);
    }

    @Test
    public void returnCorrectBoard() {
        Player player = new Player(uuidValid, nameValid, boardValid);
        String board = player.getBoard();
        assertEquals(board, boardValid);
    }

    @Test
    public void setPoints() {
        Player player = new Player(uuidValid, nameValid, boardValid);
        player.setPoints(pointsFromServer);
        int points = player.getPoints();
        assertEquals(points, 2);
    }

    @Test
    public void getPoints() {
        Player player = new Player(uuidValid, nameValid, boardValid);
        int points = player.getPoints();
        assertEquals(points, 0);
    }

    @Test
    public void throwsRuntimeExceptionWhenUUIDNotValid() {
        exceptionRule.expect(RuntimeException.class);
        exceptionRule.expectMessage("UUID is not valid");
        new Player(uuidInvalid, nameValid, boardValid);
    }

    @Test
    public void throwsRuntimeExceptionWhenNameEmpty() {
        exceptionRule.expect(RuntimeException.class);
        exceptionRule.expectMessage("Player cannot take in an empty String or null value for the \"name\" constructor");
        new Player(uuidValid, nameEmpty, boardValid);
    }

    @Test
    public void throwsRuntimeExceptionWhenNameTooShort() {
        exceptionRule.expect(RuntimeException.class);
        exceptionRule.expectMessage("Player has to contain at least 3 characters");
        new Player(uuidValid, nameToShort, boardValid);
    }

    @Test
    public void throwsRuntimeExceptionWhenBoardInvalid() {
        exceptionRule.expect(RuntimeException.class);
        exceptionRule.expectMessage("Player's board is invalid");
        new Player(uuidValid, nameValid, boardInvalid);
    }

}