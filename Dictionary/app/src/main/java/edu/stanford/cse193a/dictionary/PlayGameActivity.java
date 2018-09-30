package edu.stanford.cse193a.dictionary;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;

public class PlayGameActivity extends AppCompatActivity {

    private Map<String, String> dictionary;
    private MediaPlayer mp;
    private int points;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dictionary = new TreeMap<>();
        readFile();
        chooseWords();
        points = 0;

        ListView list = findViewById(R.id.word_list);
        list.setOnItemClickListener((adapterView, view, i, l) -> {
            String pickedWord = adapterView.getItemAtPosition(i).toString();
            TextView textView = findViewById(R.id.the_word);
            String word = textView.getText().toString();
            String correctDefinition = dictionary.get(word);

            if (pickedWord.equals(correctDefinition)) {
                points++;
                Toast.makeText(PlayGameActivity.this, "Yes :) Score = " + points,
                        Toast.LENGTH_SHORT).show();
            } else {
                points--;
                Toast.makeText(PlayGameActivity.this, "No :( Score = " + points,
                        Toast.LENGTH_SHORT).show();
            }
            chooseWords();
        });

        mp = MediaPlayer.create(this, R.raw.jeopardy);
        mp.start();
    }

    private void readFile() {
        readFileHelper(new Scanner(getResources().openRawResource(R.raw.grewords)));
        try {
            readFileHelper(new Scanner(openFileInput("added_words.txt")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void readFileHelper(Scanner scan) {
        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            String[] parts = line.split(" - ");
            dictionary.put(parts[0], parts[1]);
        }
        scan.close();
    }

    private void chooseWords() {
        Random randy = new Random();
        List<String> words = new ArrayList<>(dictionary.keySet());
        List<String> definition = new ArrayList<>(dictionary.values());

        String word = words.get(randy.nextInt(words.size()));

        String defn = dictionary.get(word);
        definition.remove(defn);
        Collections.shuffle(definition);
        definition = definition.subList(0, 4);
        definition.add(defn);
        Collections.shuffle(definition);

        TextView textView = findViewById(R.id.the_word);
        textView.setText(word);

        ListView list = findViewById(R.id.word_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                definition
        );
        list.setAdapter(adapter);
    }

    public void addNewWord(View view) {
        Intent intent = new Intent(this, AddWordActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mp.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mp.start();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("points", points);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        points = savedInstanceState.getInt("points", 0);
    }
}
