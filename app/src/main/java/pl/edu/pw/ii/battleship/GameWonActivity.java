package pl.edu.pw.ii.battleship;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class GameWonActivity extends AppCompatActivity {
    Button mainMenuBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_won);

        //Activity entrance and exit animation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        mainMenuBtn = findViewById(R.id.mainMenu);

        mainMenuBtn.setOnClickListener(v -> startActivity(new Intent(GameWonActivity.this, MainMenuActivity.class)));
    }
}