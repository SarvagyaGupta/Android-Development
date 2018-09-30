package edu.stanford.cse193a.snake;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import stanford.androidlib.SimpleActivity;

public class TitleScreenActivity extends SimpleActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title_screen);
    }

    @Override
    protected void onStart() {
        super.onStart();

        TextView highScoreBox = findViewById(R.id.high_score);
        SharedPreferences preferences = getSharedPreferences("ScorePrefs", MODE_PRIVATE);
        highScoreBox.setText(getString(R.string.high_score, preferences.getInt("High Score", 0)));
    }

    public void playGame(View view) {
        Intent intent = new Intent(this, SnakeGameActivity.class);
        startActivity(intent);
    }

    public void exitGame(View view) {
        finish();
    }
}
