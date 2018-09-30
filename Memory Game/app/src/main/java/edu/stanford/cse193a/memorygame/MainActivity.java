package edu.stanford.cse193a.memorygame;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int SIZE = 8;
    private Map<Integer, Drawable> hiddenImages;
    private ImageButton prevButton;
    private boolean toCompare;
    private List<ImageButton> correctButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();
    }

    public void revealPicture(View view) {
        final ImageButton button = (ImageButton) view;

        if ((button != prevButton || !toCompare) && !correctButtons.contains(button)) {
            final Drawable buttonImage = hiddenImages.get(button.getId());
            button.setImageDrawable(buttonImage);

            if (toCompare) {
                Drawable prevButtonImage = hiddenImages.get(prevButton.getId());
                if (prevButtonImage != buttonImage) {
                    prevButton.setImageResource(R.drawable.question_mark);
                    new Handler().postDelayed(() -> {
                            button.setImageResource(R.drawable.question_mark);
                    }, 200);
                } else {
                    correctButtons.add(prevButton);
                    correctButtons.add(button);
                }
            }

            toCompare = !toCompare;
            prevButton = button;
        }
    }

    private void initialize() {
        hiddenImages = new HashMap<>();
        prevButton = null;
        toCompare = false;
        correctButtons = new ArrayList<>();

        TypedArray images = getResources().obtainTypedArray(R.array.images);
        TypedArray buttons = getResources().obtainTypedArray(R.array.buttons);
        Random randy = new Random();
        boolean[] usedImages = new boolean[SIZE];
        boolean[] usedButtons = new boolean[SIZE * 2];

        for (int i = 0; i < SIZE; i++) {
            int randomImage = getValue(usedImages, randy);
            int button1 = getValue(usedButtons, randy);
            int button2 = getValue(usedButtons, randy);

            Drawable image = images.getDrawable(randomImage);
            hiddenImages.put(buttons.getResourceId(button1, 0), image);
            hiddenImages.put(buttons.getResourceId(button2, 0), image);
        }

        images.recycle();
        buttons.recycle();
    }

    private int getValue(boolean[] check, Random randy) {
        int res;
        do {
            res = randy.nextInt(check.length);
        } while (check[res]);
        check[res] = true;
        return res;
    }

    public void restart(View view) {
        TypedArray buttons = getResources().obtainTypedArray(R.array.buttons);

        for (int i = 0; i < buttons.length(); i++) {
            ImageButton button = findViewById(buttons.getResourceId(i, 0));
            button.setImageResource(R.drawable.question_mark);
        }

        buttons.recycle();
        initialize();
    }
}
