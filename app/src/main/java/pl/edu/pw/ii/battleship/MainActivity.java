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

public class MainActivity extends AppCompatActivity {
    public static final String API_URL = "http://d9d0a25a9926.ngrok.io";
//    public static final String API_URL = "http://34.73.205.222:8080";

    private BoardView playerBoardView;
    private BoardView opponentBoardView;
    private TextView whosTurn;

    Game game = new Game();
    Player player = new Player();
    Player opponent = new Player();

    Board playerBoard;
    Board opponentBoard;


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

        game = (Game) getIntent().getSerializableExtra("game");
        player = (Player) getIntent().getSerializableExtra("player");
        opponent = (Player) getIntent().getSerializableExtra("opponent");

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
        opponentBoardView.displayBoardsShips(true); //TODO REMOVE TO PREVENT CHEATING

        opponentBoardView.addBoardTouchListener(new BoardView.BoardTouchListener() {
            @Override
            public void onTouch(int x, int y) {
                placeShoot(opponentBoard, x, y);
            }
        });
    }

    public void updateTurnDisplay() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (playerTurn()) {
                    whosTurn.setText(player.getName());
                } else {
                    whosTurn.setText(opponent.getName());
                    reciveShoot();
                }
            }
        });
    }

    public boolean playerTurn() {
        return game.getShootingPlayer().equals(player.getUuid());
    }

    public void placeShoot(Board board, int x, int y) {
        if (playerTurn()) {
//            board.hit(board.placeAt(x, y)); //rysuje przed informacja zwrotna
            sendShoot(x, y);
        } else {
            Toast.makeText(getBaseContext(), "Wait for your turn!", Toast.LENGTH_SHORT).show();
        }
        updateBoards();
    }


    /**
     * Updates the board's displays
     */
    public void updateBoards() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                updateTurnDisplay();
                opponentBoardView.invalidate();
                playerBoardView.invalidate();
            }
        });
    }

    public void sendShoot(int x, int y) {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
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
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // check who's turn

                        System.out.println("re" + response.toString());

                        // check if game over
                        if (response.has("winnerPlayer") && !response.isNull("winnerPlayer")) {
                            try {
                                JSONObject winnerPlayer = response.getJSONObject("winnerPlayer");
                                if (winnerPlayer != null) {
                                    gameOver(winnerPlayer);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                // continue the game
                            }
                        } else {
                            try {
                                JSONObject shootingPlayer = response.getJSONObject("shootingPlayer");
                                String shootingUUID = shootingPlayer.getString("uuid");
                                game.setShootingPlayer(shootingUUID);

                                // get shooting from opponent
                                JSONObject lastShot = response.getJSONObject("lastShot");
                                int x = lastShot.getInt("x");
                                int y = lastShot.getInt("y");
                                int shotWasHit = response.getInt("lastShotHit");

                                // place my shoot result
                                if (shotWasHit != 0) {  //server sends 0 when miss
                                    Ship ship = Board.decodeShipType(shotWasHit);
                                    opponentBoard.putShipHitPlace(x, y, ship);

                                } else {
                                    opponentBoard.hit(opponentBoard.placeAt(x, y));
                                }

                                // update display
                                updateTurnDisplay();
                                updateBoards();

                                reciveShoot();
                                System.out.println("strzelilem");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse response = error.networkResponse;
                if (response != null && response.data != null) {
                    switch (response.statusCode) {
                        // place was shoot already
                        case 400:
                            Toast.makeText(getBaseContext(), "Choose another place to shoot!", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    public void reciveShoot() {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        String url = API_URL + "/matches/" + game.getGameUUID();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        // check if game over
                        if (response.has("winnerPlayer") && !response.isNull("winnerPlayer")) {
                            try {
                                JSONObject winnerPlayer = response.getJSONObject("winnerPlayer");
                                if (winnerPlayer != null) {
                                    gameOver(winnerPlayer);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                // continue the game
                            }
                        } else {

                            System.out.println("resp strzal" + response.toString());

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
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
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
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        System.out.println("rspo surrender " + response.toString());
                        Intent intLost;
                        intLost = new Intent(MainActivity.this, GameLostActivity.class);
                        startActivity(intLost);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Intent intLostErr;
                intLostErr = new Intent(MainActivity.this, GameLostActivity.class);
                startActivity(intLostErr);
                error.printStackTrace();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }
}
