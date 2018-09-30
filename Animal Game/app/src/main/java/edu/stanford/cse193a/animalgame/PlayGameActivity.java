package edu.stanford.cse193a.animalgame;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.Scanner;

public class PlayGameActivity extends AppCompatActivity {

    private SQLiteDatabase db;
    private static final String DB_NAME = "animalgame";
    private int currentNodeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_game);

        currentNodeId = 1;

        Intent intent = getIntent();
        boolean reset = intent.getBooleanExtra("reset", false);
        initializeDB(reset);
        setText();

        logTable("nodes");
        logTable("graph");

        Button yesButton = findViewById(R.id.yes_button);
        yesButton.setOnClickListener((view) -> buttonPressed("yes"));

        Button noButton = findViewById(R.id.no_button);
        noButton.setOnClickListener((view) -> buttonPressed("no"));
    }

    private void initializeDB(boolean reset) {
        db = openOrCreateDatabase(DB_NAME, MODE_ENABLE_WRITE_AHEAD_LOGGING, null);
        if (reset || !checkDatabase(DB_NAME)) {
            deleteDatabase(DB_NAME);
            db = openOrCreateDatabase(DB_NAME, MODE_ENABLE_WRITE_AHEAD_LOGGING, null);
            StringBuilder query = new StringBuilder();
            int id = getResources().getIdentifier(DB_NAME, "raw", getPackageName());
            Scanner scan = new Scanner(getResources().openRawResource(id));
            while (scan.hasNextLine()) {
                query.append(scan.nextLine()).append("\n");
                if (query.toString().trim().endsWith(";")) {
                    db.execSQL(query.toString());
                    Log.v("Queries: ", query.toString());
                    query = new StringBuilder();
                }
            }
        }
    }

    private boolean checkDatabase(String dbName) {
        SQLiteDatabase checkDB = null;
        try {
            checkDB = SQLiteDatabase.openDatabase(
                    getDatabasePath(dbName).getPath(),
                    null,
                    SQLiteDatabase.OPEN_READONLY);
            checkDB.close();
        } catch (SQLiteException e) {
            // database doesn't exist yet.
        }
        return checkDB != null;
    }

    private void buttonPressed(String input) {
        String query = "SELECT childid FROM graph WHERE parentid = " + currentNodeId
                + " AND type = '" + input + "';";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            currentNodeId = cursor.getInt(0);
            setText();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            if (input.equals("yes")) {
                builder.setMessage("Yes!!! I guessed right!");
                builder.setPositiveButton("Okay >:-(", (dialog, which) -> finish());
            } else {
                LayoutInflater inflater = getLayoutInflater();
                View view = inflater.inflate(R.layout.layout_new_word, null);
                builder.setView(view);
                builder.setPositiveButton("Done", (dialog, which) -> {
                    addNewNode(view);
                    finish();
                });
            }
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        cursor.close();
    }

    private void addNewNode(View view) {
        int nodeId = getMaxId("nodeid", "nodes");
        int graphId = getMaxId("graphid", "graph");

        updateNodesTable(view, nodeId);
        updateGraphTable(view, nodeId, graphId);
    }

    private void updateGraphTable(View view, int nodeId, int graphId) {
        RadioButton yesButton = view.findViewById(R.id.yes_button);
        if (yesButton.isChecked()) {
            insertGraph((nodeId + 1), nodeId, graphId);
        } else {
            insertGraph(nodeId, (nodeId + 1), graphId);
        }
        logTable("graph");
    }

    private void updateNodesTable(View view, int nodeId) {
        EditText wordBox = view.findViewById(R.id.user_word);
        String word = wordBox.getText().toString();

        EditText questionBox = view.findViewById(R.id.user_question);
        String question = questionBox.getText().toString();

        String updateNodeQuery = "UPDATE nodes SET nodeid = " + nodeId + " WHERE nodeid = "
                + currentNodeId;
        String newQuestionNodeQuery = "INSERT INTO nodes VALUES(" + currentNodeId +
                ", 'question', '" + question + "', 0, '0000-00-00 00:00:00', 0);";
        String newAnswerNodeQuery = "INSERT INTO nodes VALUES(" + (nodeId + 1) + ", 'answer', '"
                + word + "', 0, '0000-00-00 00:00:00', 0);";

        logTable("nodes");
        db.execSQL(updateNodeQuery);
        db.execSQL(newQuestionNodeQuery);
        db.execSQL(newAnswerNodeQuery);

        logTable("nodes");
    }

    private void insertGraph(int yesNode, int noNode, int graphId) {
        String yesGraph = "INSERT INTO graph VALUES(" + graphId + ", " + currentNodeId + ", " +
                yesNode + ", 'yes');";
        String noGraph = "INSERT INTO graph VALUES(" + (graphId + 1) + ", " + currentNodeId
                + ", " + noNode + ", 'no');";
        db.execSQL(yesGraph);
        db.execSQL(noGraph);
    }

    private int getMaxId(String column, String table) {
        Cursor cursor = db.rawQuery("SELECT MAX(" + column + ") FROM " + table, null);
        int id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getInt(0) + 1;
        }
        cursor.close();
        return id;
    }

    private void setText() {
        Cursor cursor = db.rawQuery("SELECT text, type FROM nodes WHERE nodeid = " + currentNodeId + ";",
                null);
        if (cursor.moveToFirst()) {
            TextView displayView = findViewById(R.id.display);
            String text = cursor.getString(0);
            if (cursor.getString(1).equals("answer"))
                text = "Are you thinking of a(n) " + text;
            text += text.endsWith("?") ? "" : "?";
            displayView.setText(text);
        }
        cursor.close();
    }

    @Override
    protected void onStop() {
        super.onStop();
        db.close();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentNodeId", currentNodeId);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentNodeId = savedInstanceState.getInt("currentNodeId");
        setText();
    }

    /* For debugging purpose */
    private String getColumnValue(Cursor cursor, int index) {
        String colValue = "";
        int type = cursor.getType(index);
        if (type == Cursor.FIELD_TYPE_FLOAT) {
            colValue = String.valueOf(cursor.getFloat(index));
        } else if (type == Cursor.FIELD_TYPE_INTEGER) {
            colValue = String.valueOf(cursor.getInt(index));
        } else if (type == Cursor.FIELD_TYPE_NULL) {
            colValue = "NULL";
        } else if (type == Cursor.FIELD_TYPE_STRING) {
            colValue = String.valueOf(cursor.getString(index));
        }
        return colValue;
    }

    /* For debugging purpose */
    private void logTable(String table) {
        String printAllQuery = "SELECT * FROM " + table;
        Cursor cursor = db.rawQuery(printAllQuery, null);
        if (cursor.moveToFirst()) {
            do {
                StringBuilder node = new StringBuilder("{");
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    node.append(getColumnValue(cursor, i)).append(", ");
                }
                Log.v(table + ":", node.substring(0, node.length() - 2) + "}");
            } while (cursor.moveToNext());
        }
        cursor.close();
    }
}
