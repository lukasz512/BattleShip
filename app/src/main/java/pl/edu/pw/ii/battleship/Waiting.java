package pl.edu.pw.ii.battleship;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import static pl.edu.pw.ii.battleship.MainActivity.API_URL;

public class Waiting extends AppCompatActivity implements Serializable {
    private Board playerBoard;
    private String uuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);

        //Activity entrance and exit animation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        // get user board
        Intent i = getIntent();
        uuid = i.getStringExtra("uuid");
        playerBoard = (Board) getIntent().getSerializableExtra("playerBoard");

        // send user ships positions
        sendUserShipsPositions();
    }

    public void sendUserShipsPositions() {
        JSONObject object = new JSONObject();
        try {
            object.put("board", playerBoard);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = API_URL + "/players/" + uuid + "/new_game";

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, object,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean isActive = response.getBoolean("active");
                            String gameUuid = response.getString("uuid");
                            if (isActive == true) {
                                // go to game
                                segueToMainActivity(response);
                            } else {
                                try {
                                    Thread.sleep(6000);
                                    retryEnterGame(gameUuid);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                System.out.println("Error while creating board" + error.getMessage());
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    public void retryEnterGame(String gameUuid) {
        String url = API_URL + "/matches/" + gameUuid;

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // get response data
                            boolean isActive = response.getBoolean("active");
                            String gameUuid = response.getString("uuid");

                            if ((Boolean) isActive == true) {
                                // start the game
                                segueToMainActivity(response);
                            } else {
                                Toast.makeText(getBaseContext(), "Waiting for opponent", Toast.LENGTH_LONG).show();
                                try {
                                    Thread.sleep(6000);
                                    retryEnterGame(gameUuid);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                    retryEnterGame(gameUuid);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            retryEnterGame(gameUuid);
                            Toast.makeText(getBaseContext(), "Waiting for opponent", Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    Thread.sleep(6000);
                    retryEnterGame(gameUuid);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        queue.add(stringRequest);
    }

    public String handleStringConcatenation(String arrayText) {
        return arrayText.replace("[", "").replace("]", "").replace("\"", "").replace(",", "");
    }

    public void segueToMainActivity(JSONObject response) {
        try {
            Intent intent = new Intent(Waiting.this, MainActivity.class);

            // get response data
            JSONObject playerOne = response.getJSONObject("playerOne");
            JSONObject playerTwo = response.getJSONObject("playerTwo");

            String uuidP1 = playerOne.getString("uuid");
            String nameP1 = playerOne.getString("name");
            String boardP1 = response.getString("playerOneShips");
            String b1 = handleStringConcatenation(boardP1);

            String uuidP2 = playerTwo.getString("uuid");
            String nameP2 = playerTwo.getString("name");
            String boardP2 = response.getString("playerTwoShips");
            String b2 = handleStringConcatenation(boardP2);

            // create user 1
            Player player1 = new Player();
            Player player2 = new Player();

            player1.setName(nameP1);
            player1.setUuid(uuidP1);
            player1.setBoard(b1);

            // create user 2
            player2.setName(nameP2);
            player2.setUuid(uuidP2);
            player2.setBoard(b2);

            // save players
            if (uuid.equals(uuidP1)) {
                intent.putExtra("player", player1);
                intent.putExtra("opponent", player2);
            } else {
                intent.putExtra("player", player2);
                intent.putExtra("opponent", player1);
            }

            // get game data
            String gameUuid = response.getString("uuid");
            JSONObject shootingPlayer = response.getJSONObject("shootingPlayer");
            String shootingPlayerUUID = shootingPlayer.getString("uuid");

            // create game
            Game game = new Game();
            game.setGameUUID(gameUuid);
            game.setShootingPlayer(shootingPlayerUUID);

            // save game
            intent.putExtra("game", game);

            // start activity
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}