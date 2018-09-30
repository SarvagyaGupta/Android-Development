package edu.stanford.cse193a.animalgame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AnimalGameMainActivity extends AppCompatActivity {

    private boolean isClicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animal_game_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        isClicked = preferences.getBoolean("isClicked", false);

        Button resume = findViewById(R.id.resume_game);
        resume.setEnabled(isClicked);
    }

    public void playGame(View view) {
        if (view.getId() == R.id.new_game) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure? This will reset any previous knowledge I have gained.");
            builder.setPositiveButton("Yes", (dialog, i) -> {
                SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
                editor.putBoolean("isClicked", true);
                editor.apply();

                makeIntent(true);
            });
            builder.setNegativeButton("No", (dialog, i) -> {});

            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            makeIntent(false);
        }
    }

    private void makeIntent(boolean reset) {
        Intent intent = new Intent(this, PlayGameActivity.class);
        intent.putExtra("reset", reset);
        startActivity(intent);
    }

    public void testGame(View view) {
        Intent intent = new Intent(this, TestGameActivity.class);
        startActivity(intent);
    }
}
