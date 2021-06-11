package pl.edu.pw.ii.battleship;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Lukasz Ostrowski & Cezary Klos.
 */
public class MainActivity extends AppCompatActivity {
    /**
     * API address for comunication in BattleShipAplication
     */
    public static final String API_URL = "http://c63040ba1119.ngrok.io";
//    public static final String API_URL = "http://34.73.205.222:8080";

    /**
     * The view of the Board, main player's board view
     */
    private BoardView playerBoardView;

    /**
     * The view of the opponent's Board
     */
    private BoardView opponentBoardView;

    /**
     * TextView that says who's turn it is now
     */
    private TextView whosTurn;

    /**
     * TextView that shows number of shoots collected by Main Player
     */
    private TextView playerPointDisplay;

    /**
     * TextView that shows number of shoots collected by Opponent
     */
    private TextView opponentPointDisplay;

    /**
     * Contains the game data: UUID, and who's turn
     */
    Game game = new Game();

    /**
     * Creates main Player object
     */
    Player player = new Player();

    /**
     * Creates Opponent player object
     */
    Player opponent = new Player();

    /**
     * Contains Player Board sent from server
     */
    Board playerBoard;
    Board opponentBoard;

    /**
     * Empty Board for Opponent
     */
//    Board opponentBoard = new Board();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Activity entrance and exit animation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        Intent intent = getIntent();

        opponentBoardView = (BoardView) findViewById(R.id.opponentBoardView);
        playerBoardView = (BoardView) findViewById(R.id.playerBoardView);
        whosTurn = (TextView) findViewById(R.id.whosTurn);


        // TextView for displaying main Player Name
        TextView playerNameDisplay = (TextView) findViewById(R.id.player1Name);

        // TextView for displaying Opponent Name
        TextView opponentNameDisplay = (TextView) findViewById(R.id.player2Name);
        playerPointDisplay = (TextView) findViewById(R.id.player1Points);
        opponentPointDisplay = (TextView) findViewById(R.id.player2Points);


        game = (Game) getIntent().getSerializableExtra("game");
        player = (Player) getIntent().getSerializableExtra("player");
        opponent = (Player) getIntent().getSerializableExtra("opponent");
        playerNameDisplay.setText(player.getName());
        opponentNameDisplay.setText(opponent.getName());

        playerBoard = Board.decipherPlaceShips(player.getBoard());
//        opponentBoard = Board.decipherPlaceShips(opponent.getBoard());
        opponentBoard = new Board();

        //Gives board references to the BoardViews
        setNewBoards(playerBoardView, opponentBoardView, playerBoard, opponentBoard);

        // check who's turn
        updateTurnDisplay();
    }


    /**
     * Gives a Board references to the BoardViews
     */
    private void setNewBoards(BoardView playerBoardView, BoardView opponentBoardView, Board playerBoard, Board opponentBoard) {

        playerBoardView.setBoard(playerBoard);
        opponentBoardView.setBoard(opponentBoard);

        playerBoardView.displayBoardsShips(true);
//      shows opponent board ships if given from server
//      opponentBoardView.displayBoardsShips(true);

        opponentBoardView.addBoardTouchListener((x, y) -> placeShoot(opponentBoard, x, y));
    }

    public void updateTurnDisplay() {
        runOnUiThread(() -> {
            if (playerTurn()) {
                whosTurn.setText(player.getName());
            } else {
                whosTurn.setText(opponent.getName());
                reciveShoot();
            }
        });
    }

    public boolean playerTurn() {
        return game.getShootingPlayer().equals(player.getUuid());
    }

    public void placeShoot(Board board, int x, int y) {
        if (playerTurn()) {
            sendShoot(x, y);
        } else {
            Toast.makeText(getBaseContext(), "Wait for your turn!", Toast.LENGTH_SHORT).show();
        }
        updateBoards();
    }


    /**
     * Updates the board's displays
     *
     */
    public void updateBoards() {
        runOnUiThread(() -> {
            opponentBoardView.invalidate();
            playerBoardView.invalidate();
        });
    }

    /**
     * Updates Players Points value
     *
     * @param  response vertical value of a Place, where Shoot was made
     */
    public void updatePoints(JSONObject response) {
        System.out.println("points"+ response);
        try {
            JSONObject player1 = response.getJSONObject("playerOne");
            int player1Points = response.getInt("playerOneFieldsRemainingCount");
            int player2Points = response.getInt("playerTwoFieldsRemainingCount");

            String p1UUID = player1.getString("uuid");

            if (p1UUID.equals(player.getUuid())) {
                player.setPoints(player2Points);
                opponent.setPoints(player1Points);
            } else {
                player.setPoints(player1Points);
                opponent.setPoints(player2Points);
            }
            playerPointDisplay.setText(String.valueOf(player.getPoints()));
            opponentPointDisplay.setText(String.valueOf(opponent.getPoints()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends coordinates of shoot to the server
     *
     * @param  x vertical value of a Place, where Shoot was made
     * @param  y horizontal value of a Place, where Shoot was made
     */
    public void sendShoot(int x, int y) {
        // Create RequestQueue object
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        // creates empty object for
        JSONObject object = new JSONObject();
        try {
            object.put("player-uuid", player.getUuid());
            object.put("x", x);
            object.put("y", y);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String url = API_URL + "/matches/" + game.getGameUUID() + "/shoot";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, object,
                response -> {
                    // check if game over
                    if (response.has("winnerPlayer") && !response.isNull("winnerPlayer")) {
                        try {
                            JSONObject winnerPlayer = response.getJSONObject("winnerPlayer");
                            gameOver(winnerPlayer);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            // continue the game
                        }
                    } else {
                        try {
                            // get winner user
                            JSONObject shootingPlayer = response.getJSONObject("shootingPlayer");
                            String shootingUUID = shootingPlayer.getString("uuid");
                            game.setShootingPlayer(shootingUUID);

                            // get shooting from opponent
                            JSONObject lastShot = response.getJSONObject("lastShot");
                            int x1 = lastShot.getInt("x");
                            int y1 = lastShot.getInt("y");
                            int shotWasHit = response.getInt("lastShotHit");
                            boolean shotWasSunk = response.getBoolean("lastShotSunk");

                            // place my shoot result
                            if (shotWasHit != 0) {  //server sends 0 when miss
                                Ship ship = Board.decodeShipType(shotWasHit);
                                opponentBoard.putShipHitPlace(x1, y1, ship);
                                if(shotWasSunk) opponentBoard.setShipAsSunk(opponentBoard, ship);
                            } else {
                                opponentBoard.hit(opponentBoard.placeAt(x1, y1));
                            }

                            // update which Player is shooting
                            updateTurnDisplay();
                            updatePoints(response);
                            // update Players' Boards
                            updateBoards();
                            // start listening for shoot from Opponent
                            reciveShoot();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, error -> {
                    NetworkResponse response = error.networkResponse;
                    if (response != null && response.data != null) {
                        // place was shoot already
                        if (response.statusCode == 400) {
                            Toast.makeText(getBaseContext(), "Choose another place to shoot!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        // starts request
        requestQueue.add(jsonObjectRequest);
    }

    public void reciveShoot() {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        String url = API_URL + "/matches/" + game.getGameUUID();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    // check if game over
                    if (response.has("winnerPlayer") && !response.isNull("winnerPlayer")) {
                        try {
                            JSONObject winnerPlayer = response.getJSONObject("winnerPlayer");
                            gameOver(winnerPlayer);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            // continue the game
                        }
                    } else {
                        // check if players turn
                        try {
                            JSONObject shootingPlayer = response.getJSONObject("shootingPlayer");
                            String shootingUUID = shootingPlayer.getString("uuid");

                            // get shooting from opponent
                            JSONObject lastShot = response.getJSONObject("lastShot");
                            int x = lastShot.getInt("x");
                            int y = lastShot.getInt("y");
                            int shotWasHit = response.getInt("lastShotHit");
                            boolean shotWasSunk = response.getBoolean("lastShotSunk");

                            // x = -1 => game just started
                            if (shootingUUID.equals(player.getUuid()) && x != -1 && y != -1) {
                                // set my turn
                                game.setShootingPlayer(shootingUUID);
                                // place shoot from opponent
                                if (shotWasHit != 0) {
                                    Ship ship = Board.decodeShipType(shotWasHit);
                                    playerBoard.putShipHitPlace(x, y, ship);
                                    if (shotWasSunk) {
                                        playerBoard.setShipAsSunk(playerBoard, ship);
                                    }
                                } else {
                                    playerBoard.hit(playerBoard.placeAt(x, y));
                                }

                                updateTurnDisplay();
                                updatePoints(response);
                                updateBoards();
                            } else {
                                // opponent turn - wait for shoot
                                // loop for checking who's turn
                                try {
                                    Thread.sleep(1000);
                                    reciveShoot();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, Throwable::printStackTrace);
        requestQueue.add(jsonObjectRequest);
    }

    public void gameOver(JSONObject winner) {
        System.out.println("winner" + winner);
        try {
            String winnerUUID = winner.getString("uuid");
            // player wins
            Intent intentGO;
            if (winnerUUID.equals(player.getUuid())) {
                intentGO = new Intent(MainActivity.this, GameWonActivity.class);
            } else {
                intentGO = new Intent(MainActivity.this, GameLostActivity.class);
            }
            startActivity(intentGO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void surrender(View view) {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JSONObject object = new JSONObject();
        try {
            object.put("player-uuid", player.getUuid());
        } catch (Exception e) {
            e.printStackTrace();
        }

        String url = API_URL + "/matches/" + game.getGameUUID() + "/surrender";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, object,
                response -> {
                    Intent intLost;
                    intLost = new Intent(MainActivity.this, GameLostActivity.class);
                    startActivity(intLost);
                }, error -> {
                    Intent intLostErr;
                    intLostErr = new Intent(MainActivity.this, GameLostActivity.class);
                    startActivity(intLostErr);
                    error.printStackTrace();
                });
        requestQueue.add(jsonObjectRequest);
    }
}
