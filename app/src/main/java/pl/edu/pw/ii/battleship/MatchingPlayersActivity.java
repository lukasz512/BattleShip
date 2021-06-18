package pl.edu.pw.ii.battleship;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import static pl.edu.pw.ii.battleship.MainActivity.API_URL;

public class MatchingPlayersActivity extends AppCompatActivity implements Serializable {
    private Player player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);

        //Activity entrance and exit animation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        player = (Player) getIntent().getSerializableExtra("player");
        // get user board

        // send user ships positions
        sendUserShipsPositions();
    }

    public void sendUserShipsPositions() {
        JSONObject object = new JSONObject();
        try {
            object.put("player-uuid", player.getUuid());
            object.put("privateToken", player.getPrivateToken());
            object.put("board", player.getBoard());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = API_URL + "/matches/new_game";

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, object,
                response -> {
                    try {
                        boolean isActive = response.getBoolean("active");
                        String gameUuid = response.getString("uuid");
                        if (isActive) {
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
                }, error -> {
            error.printStackTrace();
            System.out.println("Error while creating board" + error.getMessage());
        });
        requestQueue.add(jsonObjectRequest);
    }

    public void retryEnterGame(String gameUuid) {
        String url = API_URL + "/matches/" + gameUuid;

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // get response data
                        boolean isActive = response.getBoolean("active");
                        String gameUuid1 = response.getString("uuid");

                        if ((Boolean) isActive) {
                            // start the game
                            segueToMainActivity(response);
                        } else {
                            Toast.makeText(getBaseContext(), "Waiting for opponent", Toast.LENGTH_LONG).show();
                            try {
                                Thread.sleep(6000);
                                retryEnterGame(gameUuid1);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                retryEnterGame(gameUuid1);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        retryEnterGame(gameUuid);
                        Toast.makeText(getBaseContext(), "Waiting for opponent", Toast.LENGTH_LONG).show();
                    }
                }, error -> {
            try {
                Thread.sleep(6000);
                retryEnterGame(gameUuid);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        queue.add(stringRequest);
    }

    public void segueToMainActivity(JSONObject response) {
        try {
            Intent intent = new Intent(MatchingPlayersActivity.this, MainActivity.class);

            // get response data
            JSONObject playerOne = response.getJSONObject("playerOne");
            JSONObject playerTwo = response.getJSONObject("playerTwo");

            String uuidP1 = playerOne.getString("uuid");
            System.out.println(uuidP1);
            String uuidP2 = playerTwo.getString("uuid");
            System.out.println(uuidP2);


            // save players
            Player opponent;
            if (player.getUuid().equals(uuidP1)) {
                opponent = new Player(playerTwo.getString("name"));
                opponent.setUuid(playerTwo.getString("uuid"));

            } else {
                opponent = new Player(playerOne.getString("name"));
                opponent.setUuid(playerOne.getString("uuid"));
            }

            intent.putExtra("player", player);
            intent.putExtra("opponent", opponent);


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