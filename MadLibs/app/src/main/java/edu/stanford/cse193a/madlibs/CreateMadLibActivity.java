package edu.stanford.cse193a.madlibs;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.io.FileNotFoundException;
import java.io.PrintStream;

public class CreateMadLibActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_mad_lib);
    }

    public void addNewMadLib(View view) {
        EditText titleBox = findViewById(R.id.madlib_title);
        String title = titleBox.getText().toString();

        EditText madlibBox = findViewById(R.id.user_madlib);
        String madlib = madlibBox.getText().toString();

        if (title.equals("")) {
            createAlert("Title should not be empty");
        } else if (madlib.equals("")) {
            createAlert("MadLib should not be empty");
        } else {
            PrintStream output;
            try {
                output = new PrintStream(openFileOutput(title + ".txt", MODE_PRIVATE));
                output.print(madlib);
                output.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                Intent goBack = new Intent();
                goBack.putExtra("title", title);
                setResult(RESULT_OK, goBack);
                finish();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        EditText titleBox = findViewById(R.id.madlib_title);
        String title = titleBox.getText().toString();

        EditText madlibBox = findViewById(R.id.user_madlib);
        String madlib = madlibBox.getText().toString();

        outState.putString("title", title);
        outState.putString("madlib", madlib);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String title = savedInstanceState.getString("title");
        String madlib = savedInstanceState.getString("madlib");

        EditText titleBox = findViewById(R.id.madlib_title);
        titleBox.setText(title);

        EditText madlibBox = findViewById(R.id.user_madlib);
        madlibBox.setText(madlib);
    }

    private void createAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setPositiveButton("OK", (dialog, id) -> {});

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
