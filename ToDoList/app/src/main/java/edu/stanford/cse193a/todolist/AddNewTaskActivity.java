package edu.stanford.cse193a.todolist;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class AddNewTaskActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_task);
    }

    public void addNewTask(View view) {
        EditText titleBox = findViewById(R.id.user_item);
        String title = titleBox.getText().toString();

        if (title.length() == 0) {
            Toast.makeText(this, "Title should not be empty", Toast.LENGTH_LONG).show();
        } else {
            Spinner prioritySpinner = findViewById(R.id.priority);
            String priority = prioritySpinner.getSelectedItem().toString();

            DatePicker dueDatePicker = findViewById(R.id.due_date);
            int day = dueDatePicker.getDayOfMonth();
            int month = dueDatePicker.getMonth();
            int year = dueDatePicker.getYear();

            TimePicker timePicker = findViewById(R.id.due_time);
            int hour = 0;
            int min = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                hour = timePicker.getHour();
                min = timePicker.getMinute();
            } else {
                hour = timePicker.getCurrentHour();
                min = timePicker.getCurrentMinute();
            }

            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day, hour, min);
            String dueDate = calendar.getTime().toString();

            Intent goBack = new Intent();
            goBack.putExtra("title", title);
            goBack.putExtra("priority", priority);
            goBack.putExtra("date", dueDate);
            setResult(RESULT_OK, goBack);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
    }
}
