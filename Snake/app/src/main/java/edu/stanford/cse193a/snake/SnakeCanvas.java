package edu.stanford.cse193a.snake;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import stanford.androidlib.graphics.GCanvas;
import stanford.androidlib.graphics.GLabel;
import stanford.androidlib.graphics.GLine;
import stanford.androidlib.graphics.GSprite;
import stanford.androidlib.util.RandomGenerator;

public class SnakeCanvas extends GCanvas {

    enum Direction { LEFT, RIGHT, UP, DOWN }

    private static final int FPS = 6;
    private static final int LABEL_SIZE = 35;

    private static final int SCALE_SNAKE = 6;
    private static final int SCALE_FOOD = 15;
    private static final int MARGIN = 6;

    private static RandomGenerator random;
    private static List<Bitmap> headImages;

    private GSprite snakeHead;
    private GSprite food;
    private GLabel score;
    private GSprite body;
    private GLine[] boundary;

    private int points;
    private ArrayList<GSprite> bodySprites;
    private int bodyAddCounter;
    private int snakeHeadIndex;
    private ArrayList<String> moves;

    private Context context;

    public SnakeCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        random = RandomGenerator.getInstance();
        bodySprites = new ArrayList<>();
        moves = new ArrayList<>();

        Bitmap originalHead = scaleBitmap(R.drawable.snakehead, SCALE_SNAKE);
        headImages = new ArrayList<>(Arrays.asList(
                flipBitmapHorizontal(originalHead),
                rotateBitmap(flipBitmapHorizontal(originalHead), 90),
                originalHead,
                rotateBitmap(flipBitmapVertical(originalHead), 90)
        ));
    }

    @Override
    public void init() {
        if (snakeHead == null) {
            snakeHead = new GSprite(headImages.get(snakeHeadIndex));
            snakeHead.setX(MARGIN);
            snakeHead.setY(MARGIN);
            snakeHead.setVelocityX(snakeHead.getWidth());
            snakeHead.setCollisionMargin(MARGIN, MARGIN, MARGIN, MARGIN * 2);
            add(snakeHead);
        }

        food = new GSprite(scaleBitmap(R.drawable.food, SCALE_FOOD));
        resetFoodLocation();
        food.setCollisionMargin(MARGIN / 2);
        add(food);

        if (score == null) {
            score = new GLabel(context.getString(R.string.score, points));
            score.setFontSize(LABEL_SIZE);
            score.setRightX(getWidth() - LABEL_SIZE * 2);
            score.setY(MARGIN);
            add(score);
        }

        boundary = new GLine[4];
        setBoundary(0, 0, 0, 0, getHeight());
        setBoundary(1, 0, getHeight(), getWidth(), getHeight());
        setBoundary(2, getWidth(), getHeight(), getWidth(), 0);
        setBoundary(3, getWidth(), 0, 0, 0);

        animate(FPS);
    }

    private Bitmap scaleBitmap(int id, int factor) {
        Bitmap image = BitmapFactory.decodeResource(getResources(), id);
        return Bitmap.createScaledBitmap(image,
                image.getWidth() / factor,
                image.getHeight() / factor,
                true);
    }

    private Bitmap flipBitmapHorizontal(Bitmap bitmap) {
        return flipBitmap(bitmap, -1, 1);
    }

    private Bitmap flipBitmapVertical(Bitmap bitmap) {
        return flipBitmap(bitmap, 1, -1);
    }

    private Bitmap flipBitmap(Bitmap bitmap, int sx, int sy) {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();

        Matrix matrix = new Matrix();
        matrix.postScale(sx, sy, width / 2f, width / 2f);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

    }

    private void setBoundary(int index, int initial_x, int initial_y, int final_x, int final_y) {
        boundary[index] = new GLine();
        boundary[index].setStartPoint(initial_x, initial_y);
        boundary[index].setEndPoint(final_x, final_y);
        add(boundary[index]);
    }

    @Override
    public void onAnimateTick() {
        if (bodyAddCounter != 0) {
            if (body != null) {
                bodySprites.add(body);
                add(body);
            }
            bodyAddCounter--;
            if (bodyAddCounter == 0) {
                body = null;
            } else {
                growSnake();
            }
        }

        if (!checkCollide()) {
            moveSnake();
            super.onAnimateTick();
        } else {
            animationStop();
            playSound(R.raw.game_over);
            changeLabel();
            storePoints();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void storePoints() {
        SharedPreferences preferences = context.getSharedPreferences("ScorePrefs",
                Context.MODE_PRIVATE);

        if (points > preferences.getInt("High Score", 0)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("High Score", points);
            editor.apply();
        }
    }

    private void changeLabel() {
        remove(score);

        GLabel gameOver = new GLabel(context.getString(R.string.game_over));
        gameOver.setFontSize(LABEL_SIZE);
        gameOver.setRightX(getWidth() - LABEL_SIZE * 2);
        gameOver.setY(MARGIN);
        add(gameOver);
    }

    private void moveSnake() {
        if (moves.size() != 0) {
            String move = moves.remove(0);
            if (move.equals("left")) {
                turnLeft();
            } else {
                turnRight();
            }
        }

        if (bodySprites.size() != 0) {
            for (int i = bodySprites.size() - 1; i > 0; i--) {
                GSprite body = bodySprites.get(i);
                GSprite lastBody = bodySprites.get(i - 1);
                setBodyLocation(body, lastBody);
            }
            GSprite firstBody = bodySprites.get(0);
            setBodyLocation(firstBody, snakeHead);
        }
    }

    private boolean checkCollide() {
        boolean crash = false;
        if (snakeHead.collidesWith(food)) {
            playSound(R.raw.food_eaten);
            resetFoodLocation();
            resetLabel();
            growSnake();
            bodyAddCounter += points;
        } else {
            for (GLine gLine : boundary) {
                if (snakeHead.intersects(gLine)) {
                    crash = true;
                }
            }
            for (GSprite bodySprite : bodySprites) {
                if (snakeHead.collidesWith(bodySprite)) {
                    crash = true;
                }
            }
        }
        return crash;
    }

    private void playSound(int id) {
        MediaPlayer player = MediaPlayer.create(context, id);
        if (id == R.raw.game_over) {
            player.setOnCompletionListener((mediaPlayer) -> {
                player.release();
                ((Activity) context).finish();
                Log.v("Sprites:", bodySprites.size() + "");
            });
        } else {
            player.setOnCompletionListener((mediaPlayer) -> player.release());
        }
        player.start();
    }

    private void growSnake() {
        body = new GSprite(scaleBitmap(R.drawable.snakebody, SCALE_SNAKE));
        GSprite lastBody = snakeHead;
        if (bodySprites.size() != 0) {
            lastBody = bodySprites.get(bodySprites.size() - 1);
        }
        setBodyLocation(body, lastBody);
        body.setCollisionMargin(MARGIN, MARGIN, MARGIN, MARGIN * 2);
    }

    private void resetLabel() {
        points++;
        score.setLabel(context.getString(R.string.score, points));
    }

    private void resetFoodLocation() {
        food.setRightX(random.nextFloat(food.getWidth(), getWidth()));
        food.setBottomY(random.nextFloat(food.getHeight(), getHeight()));
    }

    private void setBodyLocation(GSprite currentBody, GSprite prevBody) {
        currentBody.setX(prevBody.getX());
        currentBody.setY(prevBody.getY());
    }

    private Direction getDirection() {
        if (snakeHead.getVelocityX() > 0 && snakeHead.getVelocityY() == 0) {
            return Direction.RIGHT;
        } else if (snakeHead.getVelocityX() < 0 && snakeHead.getVelocityY() == 0) {
            return Direction.LEFT;
        } else if (snakeHead.getVelocityX() == 0 && snakeHead.getVelocityY() > 0) {
            return Direction.DOWN;
        } else {
            return Direction.UP;
        }
    }

    private void turn(List<Direction> directions) {
        Direction direction = getDirection();
        int sign = directions.contains(direction)? -1 : 1;

        if (direction == Direction.LEFT || direction == Direction.RIGHT) {
            snakeHead.setVelocityX(0);
            snakeHead.setVelocityY(sign * snakeHead.getHeight());
        } else {
            snakeHead.setVelocityY(0);
            snakeHead.setVelocityX(sign * snakeHead.getWidth());
        }
    }

    public void queueLeft() {
        moves.add("left");
    }

    public void queueRight() {
        moves.add("right");
    }

    private void turnLeft() {
        turn(new ArrayList<>(Arrays.asList(Direction.RIGHT, Direction.UP)));
        int size = headImages.size();
        snakeHeadIndex = ((snakeHeadIndex - 1) % size + size) % size;
        snakeHead.setBitmap(headImages.get(snakeHeadIndex));
    }

    private void turnRight() {
        turn(new ArrayList<>(Arrays.asList(Direction.LEFT, Direction.DOWN)));
        snakeHead.setBitmap(headImages.get((++snakeHeadIndex) % headImages.size()));
    }

    public void pauseGame() {
        animationPause();
    }

    public void resumeGame() {
        animationResume();
    }

    private Bundle makeBundle(GSprite sprite) {
        Bundle bundle = new Bundle();
        bundle.putFloat("X", sprite.getX());
        bundle.putFloat("Y", sprite.getY());
        bundle.putFloat("velocityX", sprite.getVelocityX());
        bundle.putFloat("velocityY", sprite.getVelocityY());
        return bundle;
    }

    /*@Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle values = new Bundle();
        values.putBundle("head", makeBundle(snakeHead));
        if (body != null) values.putBundle("body", makeBundle(body));
        for (int i = 0; i < bodySprites.size(); i++) {
            values.putBundle("bodySprites" + i, makeBundle(bodySprites.get(i)));
        }

        values.putSerializable("moves", moves);

        values.putInt("points", points);
        values.putInt("bodyAddCounter", bodyAddCounter);
        values.putInt("snakeHeadIndex", snakeHeadIndex);
        values.putInt("bodySpritesSize", bodySprites.size());

        values.putParcelable("super", super.onSaveInstanceState());

        return super.onSaveInstanceState();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle values = (Bundle) state;
            snakeHeadIndex = values.getInt("snakeHeadIndex");

            Bundle snakeHeadBundle = values.getBundle("head");
            assert snakeHeadBundle != null;
            snakeHead = new GSprite(headImages.get(snakeHeadIndex));
            snakeHead.setX(snakeHeadBundle.getFloat("X"));
            snakeHead.setY(snakeHeadBundle.getFloat("Y"));
            snakeHead.setVelocityX(snakeHeadBundle.getFloat("velocityX"));
            snakeHead.setVelocityY(snakeHeadBundle.getFloat("velocityY"));
            snakeHead.setCollisionMargin(MARGIN, MARGIN, MARGIN, MARGIN * 2);
            add(snakeHead);

            Bundle bodyBundle = values.getBundle("body");
            if (bodyBundle == null) {
                body = null;
            } else {
                body = new GSprite(scaleBitmap(R.drawable.snakebody, SCALE_SNAKE));
                body.setX(bodyBundle.getFloat("X"));
                body.setY(bodyBundle.getFloat("Y"));
            }

            points = values.getInt("points");

            score = new GLabel(context.getString(R.string.score, points));
            score.setFontSize(LABEL_SIZE);
            score.setRightX(getWidth() - LABEL_SIZE * 2);
            score.setY(MARGIN);
            add(score);

            bodyAddCounter = values.getInt("bodyAddCounter");

            int size = values.getInt("bodySpritesSize");
            for (int i = 0; i < size; i++) {
                Bundle spriteBundle = values.getBundle("bodySprites" + i);
                assert spriteBundle != null;
                GSprite sprite = new GSprite(scaleBitmap(R.drawable.snakebody, SCALE_SNAKE));
                sprite.setX(spriteBundle.getFloat("X"));
                sprite.setY(spriteBundle.getFloat("Y"));
                bodySprites.add(sprite);
            }

            moves = (ArrayList<String>) values.getSerializable("moves");
            if (moves == null) moves = new ArrayList<>();

            state = values.getParcelable("super");
        }
        super.onRestoreInstanceState(state);
    }*/
}
