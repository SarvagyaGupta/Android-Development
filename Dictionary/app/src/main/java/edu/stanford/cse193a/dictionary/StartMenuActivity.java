package edu.stanford.cse193a.dictionary;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class StartMenuActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADD_WORD = 1234;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_menu);
    }

    public void playTheGame(View view) {
        // go to the main activity
        Intent intent = new Intent(this, PlayGameActivity.class);
        startActivity(intent);
    }

    public void addNewWord(View view) {
        Intent intent = new Intent(this, AddWordActivity.class);
        intent.putExtra("initial_text", "FooBar");
        startActivityForResult(intent, REQUEST_CODE_ADD_WORD);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ADD_WORD) {
            String newWord = data.getStringExtra("new_word");
            Toast.makeText(this, "Successfully added the word" + newWord,
                    Toast.LENGTH_SHORT).show();
        }
    }
}
