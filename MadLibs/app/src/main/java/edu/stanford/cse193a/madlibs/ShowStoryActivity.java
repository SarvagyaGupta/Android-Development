package edu.stanford.cse193a.madlibs;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import stanford.androidlib.SimpleActivity;

public class ShowStoryActivity extends AppCompatActivity {

    private TextToSpeech textToSpeech;
    private boolean isReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_story);

        Intent intent = getIntent();
        String madlib = intent.getStringExtra("madlib");

        TextView madlibBox = findViewById(R.id.story);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            madlibBox.setText(Html.fromHtml(madlib, Html.FROM_HTML_MODE_LEGACY));
        } else {
            madlibBox.setText(madlib);
        }

        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS)
                isReady = true;
        });

        Handler handler = new Handler();
        handler.postDelayed(() -> textToSpeech.speak(madlibBox.getText().toString(),
                TextToSpeech.QUEUE_FLUSH,
                null), 1000);
    }

    public void mainScreen(View view) {
        finish();
    }
}
