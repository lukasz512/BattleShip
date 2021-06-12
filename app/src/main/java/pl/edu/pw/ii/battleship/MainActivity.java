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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Lukasz Ostrowski & Cezary Klos.
 */
public class MainActivity extends AppCompatActivity {
    /**
     * API address for communication in BattleShip Application
     */
    public static final String API_URL = "http://c561c377ba33.ngrok.io";
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
     * main Player object
     */
    Player player;

    /**
     * Opponent player object
     */
    Player opponent;

    /**
     * Contains Player Board sent from server
     */
    Board playerBoard;

    /**
     * Empty Board for Opponent
     */
    Board opponentBoard = new Board();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Activity entrance and exit animation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);


        // finds elements in Layout
        playerBoardView = (BoardView) findViewById(R.id.playerBoardView);
        playerPointDisplay = (TextView) findViewById(R.id.player1Points);
        opponentBoardView = (BoardView) findViewById(R.id.opponentBoardView);
        opponentPointDisplay = (TextView) findViewById(R.id.player2Points);
        whosTurn = (TextView) findViewById(R.id.whosTurn);

        // TextView for displaying Player & Opponent Name
        TextView playerNameDisplay = (TextView) findViewById(R.id.player1Name);
        TextView opponentNameDisplay = (TextView) findViewById(R.id.player2Name);

        // create Intent to get Game data
        game = (Game) getIntent().getSerializableExtra("game");
        player = (Player) getIntent().getSerializableExtra("player");
        opponent = (Player) getIntent().getSerializableExtra("opponent");

        // sets Player names on display
        playerNameDisplay.setText(player.getName());
        opponentNameDisplay.setText(opponent.getName());

        // gets Player board to redraw it on upper BoardView
        playerBoard = Board.decipherPlaceShips(player.getBoard());
        // can access Opponent BoardView from Server
//      opponentBoard = Board.decipherPlaceShips(opponent.getBoard());

        //Gives board references to the BoardViews
        setNewBoards(playerBoardView, opponentBoardView, playerBoard, opponentBoard);

        // check who's turn
        updateTurnDisplay();
    }


    /**
     * Gives a Board references to the BoardViews
     *
     * @param playerBoardView   container for displaying upper BoardView
     * @param opponentBoardView container for displaying lower BoardView
     * @param playerBoard       contains places where Player's ships are located
     * @param opponentBoard     empty Board ready to make shoots
     */
    private void setNewBoards(BoardView playerBoardView, BoardView opponentBoardView, Board playerBoard, Board opponentBoard) {
        // set right Board
        playerBoardView.setBoard(playerBoard);
        opponentBoardView.setBoard(opponentBoard);

        // show ships on Player Board
        playerBoardView.displayBoardsShips(true);

//      shows opponent board ships if given from server
//      opponentBoardView.displayBoardsShips(true);

        // activate touchListener on Opponent Board
        opponentBoardView.addBoardTouchListener(this::placeShoot);
    }

    /**
     * Updates name of active player on display
     */
    public void updateTurnDisplay() {
        runOnUiThread(() -> {
            if (playerTurn()) {
                whosTurn.setText(player.getName());
            } else {
                whosTurn.setText(opponent.getName());
                receiveShoots();
            }
        });
    }

    /**
     * Checks which Player is active
     *
     * @return true if main Player is Active / false if Opponent is Active
     */
    public boolean playerTurn() {
        return game.getShootingPlayer().equals(player.getUuid());
    }

    /**
     * Sends shoot coordinates to the server if it's Player Turn
     * otherwise shows Toast with information :"Wait for your turn"
     *
     * @param x vertical coordinate of a shoot
     * @param y horizontal coordinate of a shoot
     */
    public void placeShoot(int x, int y) {
        if (playerTurn()) {
            sendShoot(x, y);
        } else {
            Toast.makeText(getBaseContext(), "Wait for your turn!", Toast.LENGTH_SHORT).show();
        }
        updateBoards();
    }


    /**
     * Redraw boards on displays
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
     * @param response response from server with information about players and points
     */
    public void updatePoints(JSONObject response) {
        try {
            // get points from server
            JSONObject player1 = response.getJSONObject("playerOne");
            int player1Points = response.getInt("playerOneFieldsRemainingCount");
            int player2Points = response.getInt("playerTwoFieldsRemainingCount");

            // checks if player1 on server is equal to Main Player
            String p1UUID = player1.getString("uuid");
            if (p1UUID.equals(player.getUuid())) {
                player.setPoints(player2Points);
                opponent.setPoints(player1Points);
            } else {
                player.setPoints(player1Points);
                opponent.setPoints(player2Points);
            }
            // show Players' points on display
            playerPointDisplay.setText(String.valueOf(player.getPoints()));
            opponentPointDisplay.setText(String.valueOf(opponent.getPoints()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends coordinates of shoot to the server
     *
     * @param x vertical value of a Place, where Shoot was made
     * @param y horizontal value of a Place, where Shoot was made
     */
    public void sendShoot(int x, int y) {
        // Create RequestQueue object
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        // creates empty object for
        JSONObject object = new JSONObject();
        try {
            // put data to payload
            object.put("player-uuid", player.getUuid());
            object.put("x", x);
            object.put("y", y);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // creates url to place shoot
        String url = API_URL + "/matches/" + game.getGameUUID() + "/shoot";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, object,
                response -> {
                    // check if game over
                    if (response.has("winnerPlayer") && !response.isNull("winnerPlayer")) {
                        try {
                            // if winner object exists - game is over
                            JSONObject winnerPlayer = response.getJSONObject("winnerPlayer");
                            gameOver(winnerPlayer);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            // continue the game
                        }
                    } else {
                        try {
                            // get active user
                            JSONObject shootingPlayer = response.getJSONObject("shootingPlayer");
                            String shootingUUID = shootingPlayer.getString("uuid");
                            game.setShootingPlayer(shootingUUID);

                            // get shoot coordinates, and info if was hit from opponent
                            JSONObject lastShot = response.getJSONObject("lastShot");
                            int x1 = lastShot.getInt("x");
                            int y1 = lastShot.getInt("y");
                            int shotWasHit = response.getInt("lastShotHit");
                            boolean shotWasSunk = response.getBoolean("lastShotSunk");

                            // place shoot on main Player's Board
                            if (shotWasHit != 0) {  //server sends 0 when miss
                                Ship ship = Board.decodeShipType(shotWasHit);
                                opponentBoard.putShipHitPlace(x1, y1, ship);
                                if (shotWasSunk) opponentBoard.setShipAsSunk(opponentBoard, ship);
                            } else {
                                opponentBoard.hit(opponentBoard.placeAt(x1, y1));
                            }

                            // update which Player is shooting
                            updateTurnDisplay();
                            // update points on display
                            updatePoints(response);
                            // update Players' Boards
                            updateBoards();
                            // start listening for shoot from Opponent
                            receiveShoots();
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

    /**
     * Looped listener
     * Runs each second to check if Opponent placed the shoot
     * if not - it runs again and waits for shoot
     */
    public void receiveShoots() {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        String url = API_URL + "/matches/" + game.getGameUUID();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    // check if game over
                    if (response.has("winnerPlayer") && !response.isNull("winnerPlayer")) {
                        try {
                            // if winner object exists - game is over
                            JSONObject winnerPlayer = response.getJSONObject("winnerPlayer");
                            gameOver(winnerPlayer);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            // continue the game
                        }
                    } else {
                        // check if Opponent placed the shoot
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
                                // set main Player turn
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

                                // update which Player is shooting
                                updateTurnDisplay();
                                // update points on display
                                updatePoints(response);
                                // update Players' Boards
                                updateBoards();
                                // start listening for shoot from Opponent
                            } else {
                                // opponent turn - wait for shoot
                                // loop for checking who's turn
                                try {
                                    Thread.sleep(1000);
                                    receiveShoots();
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

    /**
     * Transfer layers to correct Activities after the game is over
     * or one of Players surrendered
     *
     * @param winner object of winner: contains name and uuid
     */
    public void gameOver(JSONObject winner) {
        try {
            String winnerUUID = winner.getString("uuid");
            // player wins
            Intent intentGO;
            // if main Player wins - go to Game Won Activity
            if (winnerUUID.equals(player.getUuid())) {
                intentGO = new Intent(MainActivity.this, GameWonActivity.class);
            } else {
                // if main Player looses - go to Lost Game activity
                intentGO = new Intent(MainActivity.this, GameLostActivity.class);
            }
            startActivity(intentGO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Transfer layers to correct Activities after the game is over
     * or one of Players surrendered
     *
     * @param view object View - used in layout to attach button
     */
    public void surrender(View view) {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JSONObject object = new JSONObject();
        try {
            object.put("player-uuid", player.getUuid());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // url to send surrender request to the server
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
