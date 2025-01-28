package com.aghakhani.ballwallgame;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private View ball; // The player's ball
    private TextView scoreText; // The score display
    private RelativeLayout mainLayout; // The main game layout
    private int score = 0; // Player's current score
    private boolean gameOver = false; // Game over flag
    private Handler handler = new Handler(); // Handler for timed events
    private Random random = new Random(); // Random generator for obstacle positions

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        ball = findViewById(R.id.ball);
        scoreText = findViewById(R.id.scoreText);
        mainLayout = findViewById(R.id.mainLayout);

        // Set ball movement with touch events
        ball.setOnTouchListener(new View.OnTouchListener() {
            float initialX, initialTouchX;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Save initial touch and ball positions
                        initialX = ball.getX();
                        initialTouchX = event.getRawX();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        // Calculate the new position and update ball position
                        float deltaX = event.getRawX() - initialTouchX;
                        float newX = initialX + deltaX;
                        // Keep the ball within screen boundaries
                        if (newX >= 0 && newX <= getScreenWidth() - ball.getWidth()) {
                            ball.setX(newX);
                        }
                        break;
                }
                return true;
            }
        });

        // Start spawning obstacles
        startObstacleSpawner();
    }

    // Start spawning obstacles at regular intervals
    private void startObstacleSpawner() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!gameOver) {
                    spawnObstacle();
                    handler.postDelayed(this, 1000); // Spawn an obstacle every 1 second
                }
            }
        }, 1000);
    }

    // Spawn a new obstacle
    private void spawnObstacle() {
        View obstacle = new View(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        // Set a random horizontal position for the obstacle
        params.leftMargin = random.nextInt(getScreenWidth() - 100);
        params.topMargin = 0;
        obstacle.setLayoutParams(params);
        obstacle.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));

        // Add the obstacle to the game layout
        mainLayout.addView(obstacle);

        // Animate the obstacle moving downward
        obstacle.animate()
                .translationY(getScreenHeight())
                .setDuration(2000)
                .withEndAction(() -> {
                    // Remove the obstacle when it leaves the screen
                    mainLayout.removeView(obstacle);
                    if (!gameOver) {
                        score++; // Increment the score
                        updateScoreText(); // Update the score display
                        playSound(R.raw.score_sound); // Play score sound
                    }
                })
                .start();

        // Check for collisions between the ball and the obstacle
        checkCollision(obstacle);
    }

    // Check if the ball collides with an obstacle
    private void checkCollision(View obstacle) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!gameOver && isColliding(ball, obstacle)) {
                    gameOver = true; // Mark game as over
                    showGameOverDialog(); // Show game over dialog

                    // Change score box color to red
                    scoreText.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                    scoreText.setTextColor(getResources().getColor(android.R.color.white)); // Ensure text is visible

                    vibrateEffect(ball); // Apply a vibration effect to the ball
                    playSound(R.raw.game_over_sound); // Play game-over sound
                } else if (!gameOver) {
                    handler.postDelayed(this, 10); // Recheck collision every 10ms
                }
            }
        }, 10);
    }

    // Check if two views are colliding
    private boolean isColliding(View v1, View v2) {
        int[] loc1 = new int[2];
        int[] loc2 = new int[2];
        v1.getLocationOnScreen(loc1);
        v2.getLocationOnScreen(loc2);

        return loc1[0] < loc2[0] + v2.getWidth() &&
                loc1[0] + v1.getWidth() > loc2[0] &&
                loc1[1] < loc2[1] + v2.getHeight() &&
                loc1[1] + v1.getHeight() > loc2[1];
    }

    // Get the screen width
    private int getScreenWidth() {
        return getResources().getDisplayMetrics().widthPixels;
    }

    // Get the screen height
    private int getScreenHeight() {
        return getResources().getDisplayMetrics().heightPixels;
    }

    // Update the score display
    private void updateScoreText() {
        scoreText.setText("Score: " + score);
        vibrateEffect(scoreText); // Apply a vibration effect to the score text
    }

    // Apply a vibration effect to a view
    private void vibrateEffect(View view) {
        view.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(100)
                .withEndAction(() -> view.animate().scaleX(1f).scaleY(1f).setDuration(100))
                .start();
    }

    // Play a sound effect
    private void playSound(int soundResId) {
        MediaPlayer mediaPlayer = MediaPlayer.create(this, soundResId);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(MediaPlayer::release);
    }

    // Show a game over dialog
    private void showGameOverDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme);
        builder.setTitle("🎮 Game Over 🎮")
                .setMessage("Oh no! You lost. \nYour score: " + score + "\n\nDo you want to try again or exit the game?")
                .setCancelable(false)
                .setPositiveButton("🔄 Restart", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Restart the game
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                })
                .setNegativeButton("❌ Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Exit the game
                        finish();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();

        // تغییر رنگ متن عنوان و پیام
        TextView titleView = dialog.findViewById(android.R.id.title);
        if (titleView != null) {
            titleView.setTextColor(getResources().getColor(android.R.color.white));
            titleView.setTextSize(24);
        }

        TextView messageView = dialog.findViewById(android.R.id.message);
        if (messageView != null) {
            messageView.setTextColor(getResources().getColor(android.R.color.white));
            messageView.setTextSize(18);
        }
    }
}
