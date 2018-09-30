package edu.stanford.cse193a.madlibs;

import android.content.Intent;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class FillInWordsActivity extends AppCompatActivity {

    private static final int REQ_CODE_STT = 145;
    private Story story;
    private int placeholdersLeft;
    private int totalPlaceholdersEntered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_in_words);

        String fileName = getIntent().getStringExtra("file");
        int fileId = getResources().getIdentifier(fileName, "raw", getPackageName());
        Scanner scan = null;
        try {
            scan = new Scanner(openFileInput(fileName + ".txt"));
        } catch (Exception e) {
            scan = new Scanner(getResources().openRawResource(fileId));
        }
        story = new Story(scan, Build.VERSION.SDK_INT > Build.VERSION_CODES.N);
        placeholdersLeft = story.getNumberOfPlaceHolders();
        totalPlaceholdersEntered = 0;

        if (story.getNumberOfPlaceHolders() == 0) {
            showStory();
        } else {
            setTextViews();
        }
    }

    public void acceptWord(View view) {
        EditText userWordBox = findViewById(R.id.user_word);
        String userWord = userWordBox.getText().toString();
        userWordBox.setText("");

        if (userWord.equals("")) {
            createAlert("The word should not be blank");
        } else if (userWord.contains("<") || userWord.contains(">")) {
            createAlert("The word should not contain < or >");
        } else {
            story.setPlaceholder(totalPlaceholdersEntered, userWord);

            placeholdersLeft--;
            totalPlaceholdersEntered++;

            if (placeholdersLeft == 0) {
                showStory();
            } else {
                setTextViews();
            }
        }
    }

    private void showStory() {
        Intent intent = new Intent(this, ShowStoryActivity.class);
        intent.putExtra("madlib", story.toString());
        startActivity(intent);
    }

    private void createAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setPositiveButton("OK", (dialog, id) -> {});

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setTextViews() {
        TextView wordsLeftBox = findViewById(R.id.words_left);
        wordsLeftBox.setText(placeholdersLeft + " word(s) left");

        TextView placeholderBox = findViewById(R.id.placeholder);
        placeholderBox.setText("Please type " + story.getPlaceholder(totalPlaceholdersEntered));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("story", story);
        outState.putInt("placeholdersLeft", placeholdersLeft);
        outState.putInt("totalPlaceholdersEntered", totalPlaceholdersEntered);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        story = (Story) savedInstanceState.getSerializable("story");
        totalPlaceholdersEntered = savedInstanceState.getInt("totalPlaceholdersEntered");
        placeholdersLeft = savedInstanceState.getInt("placeholdersLeft");

        setTextViews();
    }

    public void speakWord(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say the word!");

        startActivityForResult(intent, REQ_CODE_STT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE_STT && resultCode == RESULT_OK) {
            List<String> ret = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (ret != null && ret.size() != 0) {
                String text = ret.get(0);

                EditText userWordBox = findViewById(R.id.user_word);
                userWordBox.setText(text);
            }
        }
    }
}
