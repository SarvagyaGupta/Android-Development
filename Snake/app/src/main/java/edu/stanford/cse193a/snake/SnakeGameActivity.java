package edu.stanford.cse193a.snake;

import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class SnakeGameActivity extends AppCompatActivity {

    private static MediaPlayer song;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snake_game);

        song = MediaPlayer.create(this, R.raw.snake_movement);
        song.setLooping(true);
    }

    public void turnLeftClick(View view) {
        SnakeCanvas canvas = findViewById(R.id.snake_canvas);
        canvas.queueLeft();
    }

    public void turnRightClick(View view) {
        SnakeCanvas canvas = findViewById(R.id.snake_canvas);
        canvas.queueRight();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SnakeCanvas canvas = findViewById(R.id.snake_canvas);
        canvas.pauseGame();
        song.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SnakeCanvas canvas = findViewById(R.id.snake_canvas);
        canvas.resumeGame();
        song.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        song.release();
    }
}
