package pl.edu.pw.ii.battleship;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import static pl.edu.pw.ii.battleship.MainActivity.API_URL;

public class CreateUserActivity extends AppCompatActivity {
    private EditText userName;
    private Player player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);

        //Activity entrance and exit animation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        Button createUser = findViewById(R.id.createUser);
        userName = findViewById(R.id.userName);

        createUser.setOnClickListener(v -> {
            String name = userName.getText().toString().trim();

            // check if user name is empty
            if (name.matches("")) {
                Toast.makeText(getBaseContext(), "You didn't enter the Name", Toast.LENGTH_LONG).show();
            } else if (name.length() < 3) {
                Toast.makeText(getBaseContext(), "Name has to contain at least 3 characters", Toast.LENGTH_LONG).show();
            } else {
                // create user
                String url = API_URL + "/players/";
                CreateUser(name, url);
            }
        });
    }

    public void CreateUser(String name, String url) {

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

        player = new Player(name);
        JSONObject object = new JSONObject();
        try {
            object.put("name", player.getName());
            object.put("privateToken", player.getPrivateToken());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, object,
                response -> {
                    try {
                        // get user uuid
                        String uuid = response.getString("uuid");
                        player.setUuid(uuid);
                        // go to another activity
                        Intent intent1 = new Intent(CreateUserActivity.this, PlaceShipsActivity.class);
                        // transfer user data to another activity
                        intent1.putExtra("player", player);
                        startActivity(intent1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getBaseContext(), "Creating user Error", Toast.LENGTH_LONG).show();
                    }
                }, error -> {
            System.out.println("Creating user Error: " + error.getMessage());
            error.printStackTrace();
            Toast.makeText(getBaseContext(), "Creating user Error.\nTry again.\n", Toast.LENGTH_LONG).show();
        });
        requestQueue.add(jsonObjectRequest);
    }
}