package edu.stanford.cse193a.animalgame;

import android.app.ProgressDialog;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class TestGameActivity extends AppCompatActivity {

    private static final String USERNAME = "sarvagya@uw.edu";
    private static final String PASSWORD = "GiveMeAccess";
    private static final String DB_NAME = "animalgame";
    private int currentNodeId;
    private DatabaseReference fb;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_game);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(USERNAME, PASSWORD);

        currentNodeId = 1;
        fb = FirebaseDatabase.getInstance().getReference();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Thinking...");
        setText();

        Button yesButton = findViewById(R.id.yes_button);
        yesButton.setOnClickListener((view) -> buttonPressed(true));

        Button noButton = findViewById(R.id.no_button);
        noButton.setOnClickListener((view) -> buttonPressed(false));
    }

    private void setText() {
        DatabaseReference firstNode = fb.child(DB_NAME + "/nodes/" + currentNodeId);
        firstNode.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot data) {
                Node node = data.getValue(Node.class);
                if (node != null) {
                    TextView displayView = findViewById(R.id.display);
                    String text = node.text;
                    if (node.isAnswer()) text = "Are you thinking of a(n) " + text;
                    text += text.endsWith("?") ? "" : "?";
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        text = Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString();
                    }
                    displayView.setText(text);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.v("Firebase error: ", error.getMessage());
            }
        });
    }

    private void buttonPressed(boolean input) {
        DatabaseReference nodes = fb.child(DB_NAME + "/nodes");
        Query query = nodes.orderByChild("id").equalTo(currentNodeId);
        dialog.show();
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot data) {
                if (data.hasChildren()) {
                    Node currentNode = data.getChildren().iterator().next().getValue(Node.class);
                    Log.v("Current Node: ", currentNode.toString());
                    if (currentNode.isQuestion()) {
                        if (input) {
                            currentNodeId = currentNode.yes;
                        } else {
                            currentNodeId = currentNode.no;
                        }
                        setText();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(TestGameActivity.this);
                        if (input) {
                            builder.setMessage("I win!! Try again next time... :)");
                        } else {
                            builder.setMessage("Damn... I'm not smart enough even " +
                                    "with all this knowledge :(");
                        }
                        builder.setPositiveButton("Okay", (dialog, i) -> finish());
                        builder.create().show();
                    }
                }
                dialog.hide();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.v("Firebase error:", error.getMessage());
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        dialog.dismiss();
    }
}
