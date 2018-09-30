package edu.stanford.cse193a.madlibs;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_CODE_CREATE_MADLIB = 145;
    private ArrayList<String> files;
    private ArrayAdapter<String> storiesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        files = new ArrayList<>(getPreferences(MODE_PRIVATE).getStringSet("files",
                            new HashSet<>(Arrays.asList(getResources().getStringArray(R.array.stories)))));
        storiesAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                files
        );

        Spinner stories = findViewById(R.id.user_file);
        stories.setAdapter(storiesAdapter);
    }

    public void startMadLibs(View view) {
        Spinner fileSpinner = findViewById(R.id.user_file);
        String fileName = fileSpinner.getSelectedItem().toString();

        Intent intent = new Intent(this, FillInWordsActivity.class);
        intent.putExtra("file", fileName);
        startActivity(intent);
    }

    public void createMadLibs(View view) {
        Intent intent = new Intent(this, CreateMadLibActivity.class);
        startActivityForResult(intent, REQ_CODE_CREATE_MADLIB);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE_CREATE_MADLIB && resultCode == RESULT_OK) {
            String title = data.getStringExtra("title");
            files.add(title);
            System.err.println(files);
            storiesAdapter.notifyDataSetChanged();

            Toast.makeText(this, "Successfully added " + title + " madlib",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("files", files);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        files = savedInstanceState.getStringArrayList("files");

        storiesAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = preferences.edit();
        prefsEditor.putStringSet("files", new HashSet<>(files));
        prefsEditor.apply();
    }
}
