package edu.stanford.cse193a.sqlitetest;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Scanner;

/**
 * This app was demo-ed in Stanford cs 193a. I recreated it...
 */
public class MainActivity extends AppCompatActivity {

    private static final String[] DB_NAMES = {"babynames", "imdb", "simpsons", "world"};
    private ProgressDialog dialog;
    private static final int MAX_ROWS_TO_SHOW = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Task(this).execute();
    }

    private void prepareQuery() {
        EditText queryBox = findViewById(R.id.user_query);
        String query = queryBox.getText().toString();

        if (((RadioButton) findViewById(R.id.baby_names)).isChecked()) {
            execQuery(query, "babynames");
        } else if (((RadioButton) findViewById(R.id.imdb)).isChecked()) {
            execQuery(query, "imdb");
        } else if (((RadioButton) findViewById(R.id.simpsons)).isChecked()) {
            execQuery(query, "simpsons");
        } else if (((RadioButton) findViewById(R.id.world)).isChecked()) {
            execQuery(query, "world");
        }
    }

    private void execQuery(String query, String dbName) {
        SQLiteDatabase db = openOrCreateDatabase(dbName, MODE_PRIVATE, null);
        TableLayout table = findViewById(R.id.table);
        table.removeAllViews();

        try {
            if (query.toLowerCase().startsWith("select")) {
                Cursor cursor = db.rawQuery(query, null);
                if (cursor.moveToFirst()) {
                    int colNumber = cursor.getColumnCount();

                    TableRow header = new TableRow(this);
                    TableRow.LayoutParams rowParams = new TableRow.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                    rowParams.weight = 1;
                    header.setLayoutParams(rowParams);

                    for (int i = 0; i < colNumber; i++) {
                        TextView view = makeTextView(cursor.getColumnName(i), header,
                                0xFF333333, 0xFFDDDDDD);
                        view.setTypeface(null, Typeface.BOLD);
                    }

                    table.addView(header);

                    int counter = 1;
                    do {
                        TableRow row = new TableRow(this);
                        row.setLayoutParams(rowParams);

                        for (int i = 0; i < colNumber; i++) {
                            makeTextView(getColumnValue(cursor, i), row,
                                    (counter % 2 == 0) ? 0xFFEEEEEE : 0xFFFFFFFF, 0xFF000000);
                        }

                        table.addView(row);
                        counter++;

                        if (counter > MAX_ROWS_TO_SHOW) {
                            break;
                        }
                    } while (cursor.moveToNext());

                    Toast.makeText(this, "Query complete: " + (counter - 1) + " rows.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "No results found!", Toast.LENGTH_SHORT).show();
                }
                cursor.close();
            } else {
                db.execSQL(query);
                Toast.makeText(this, "Ran successfully!", Toast.LENGTH_SHORT).show();
            }
        } catch (SQLiteException e) {
            String msg = e.getMessage();
            int index = msg.indexOf(", while compiling");
            if (index >= 0) {
                msg = msg.substring(0, index);
            }

            index = msg.indexOf(" (code");
            if (index >= 0) {
                msg = msg.substring(0, index);
            }

            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }

        table.postInvalidate();
    }

    private TextView makeTextView(String text, TableRow header, int bgColor, int txtColor) {
        TextView view = new TextView(this);
        TableRow.LayoutParams textParams = new TableRow.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        textParams.weight = 1;
        textParams.setMargins(2, 2, 2, 2);
        view.setLayoutParams(textParams);
        view.setBackgroundColor(bgColor);
        view.setTextColor(txtColor);
        view.setText(text);
        view.setPadding(2, 2, 2, 2);
        header.addView(view);
        return view;
    }

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

    public void onQueryClick(View view) {
        prepareQuery();
    }

    public void reset(View view) {
        Task task = new Task(this);
        task.reset = true;
        task.execute();
    }

    private class Task extends AsyncTask<String, Integer, Void> {
        private Context context;
        private boolean reset;

        Task (Context context) {
            this.context = context;
            reset = false;
        }

        @Override
        protected Void doInBackground(String... items) {
            for (String dbName: DB_NAMES) {
                if (reset || !checkDatabase(dbName)) {
                    deleteDatabase(dbName);
                    SQLiteDatabase db = context.openOrCreateDatabase(dbName, MODE_PRIVATE, null);
                    StringBuilder query = new StringBuilder();
                    int id = context.getResources().getIdentifier(dbName, "raw",
                            context.getPackageName());
                    Scanner scan = new Scanner(context.getResources().openRawResource(id));
                    while (scan.hasNextLine()) {
                        query.append(scan.nextLine()).append("\n");
                        if (query.toString().trim().endsWith(";")) {
                            db.execSQL(query.toString());
                            System.err.println(query.toString());
                            query = new StringBuilder();
                        }
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(context);
            dialog.setMessage("Updating database...");
            dialog.setMax(10000);
            dialog.show();
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            reset = false;
            dialog.hide();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            if (values.length > 0) dialog.setProgress(values[0]);
        }

        private boolean checkDatabase(String dbName) {
            SQLiteDatabase checkDB = null;
            try {
                checkDB = SQLiteDatabase.openDatabase(
                        context.getDatabasePath(dbName).getPath(),
                        null,
                        SQLiteDatabase.OPEN_READONLY);
                checkDB.close();
            } catch (SQLiteException e) {
                // database doesn't exist yet.
            }
            return checkDB != null;
        }
    }
}
