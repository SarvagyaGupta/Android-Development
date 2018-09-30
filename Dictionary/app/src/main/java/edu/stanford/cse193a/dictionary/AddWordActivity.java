package edu.stanford.cse193a.dictionary;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.io.FileNotFoundException;
import java.io.PrintStream;

public class AddWordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_word);

        Intent intent = getIntent();
        String initialWord = intent.getStringExtra("initial_text");

        EditText newWordBox = findViewById(R.id.new_word);
        newWordBox.setText(initialWord);
    }

    public void addNewWord(View view) {
        EditText wordBox = findViewById(R.id.new_word);
        EditText defnBox = findViewById(R.id.new_defn);

        String word = wordBox.getText().toString();
        String defn = defnBox.getText().toString();

        try {
            PrintStream output = new PrintStream(openFileOutput("added_words.txt", MODE_APPEND));
            output.println(word + " - " + defn);
            output.close();

            Intent goBack = new Intent();
            goBack.putExtra("new_word", word);
            goBack.putExtra("new_defn", defn);
            setResult(RESULT_OK, goBack);
            finish();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
