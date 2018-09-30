package edu.stanford.cse193a.todolist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class ToDoListActivity extends AppCompatActivity {

    private List<Item> items;
    private ArrayAdapter<Item> listAdapter;
    private static final int REQ_CODE_ADD_NEW = 145;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_list);
        initialize();

        IntentFilter filter = new IntentFilter();
        filter.addAction(NotificationService.ACTION_DONE);
        NotificationReceiver receiver = new NotificationReceiver();
        registerReceiver(receiver, filter);
    }

    private void initialize() {
        items = new ArrayList<>();

        try {
            Scanner scan = new Scanner(openFileInput("todo.txt"));
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                String[] parts = line.split("\t");
                items.add(new Item(parts[0], parts[1], parts[2]));
            }
            scan.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Collections.sort(items);

        listAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                items
        );

        ListView list = findViewById(R.id.todo_list);
        list.setAdapter(listAdapter);
        list.setOnItemLongClickListener((adapterView, view, position, l) -> {
                items.remove((Item) adapterView.getItemAtPosition(position));
                writeFile();
                listAdapter.notifyDataSetChanged();
                return true;
        });
    }

    public void listItemAdd(View view) {
        Intent intent = new Intent(this, AddNewTaskActivity.class);
        startActivityForResult(intent, REQ_CODE_ADD_NEW);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE_ADD_NEW && resultCode == RESULT_OK) {
            String title = data.getStringExtra("title");
            String priority = data.getStringExtra("priority");
            String dueDate = data.getStringExtra("date");

            Toast.makeText(this, "Added " + title + " task successfully!",
                    Toast.LENGTH_SHORT).show();

            Item newItem = new Item(title, priority, dueDate);
            items.add(newItem);
            Log.v("Date: ", newItem.getDueDate().toString());
            Collections.sort(items);
            writeFile();

            listAdapter.notifyDataSetChanged();

            Intent intent = new Intent(this, NotificationService.class);
            intent.setAction(NotificationService.ACTION_NOTIFY_USER);
            intent.putExtra("title", title);
            intent.putExtra("date", newItem.getDueDate());
            startService(intent);
        }
    }

    private void writeFile() {
        try {
            PrintStream output = new PrintStream(openFileOutput("todo.txt", MODE_PRIVATE));
            for (Item item: items) {
                output.println(item.getDescription() + "\t" + item.getPriority()
                        + "\t" + item.getDueDate());
            }
            output.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    class NotificationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }
}
