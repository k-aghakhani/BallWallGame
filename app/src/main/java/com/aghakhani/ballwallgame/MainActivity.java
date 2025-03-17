package com.aghakhani.ballwallgame;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private View ball; // The player's ball
    private TextView scoreText; // The score display
    private TextView levelText; // The level display
    private RelativeLayout mainLayout; // The main game layout
    private int score = 0; // Player's current score
    private int level = 1; // Player's current level
    private boolean gameOver = false; // Flag to indicate if the game is over
    private Handler handler = new Handler(); // Handler for scheduling timed events
    private Random random = new Random(); // Random generator for obstacle positions and properties

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        ball = findViewById(R.id.ball);
        scoreText = findViewById(R.id.scoreText);
        levelText = findViewById(R.id.levelText);
        mainLayout = findViewById(R.id.mainLayout);

        // Set initial level display
        updateLevelText();

        // Set up ball movement with touch events
        ball.setOnTouchListener(new View.OnTouchListener() {
            float initialX, initialTouchX;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Store initial positions when the ball is touched
                        initialX = ball.getX();
                        initialTouchX = event.getRawX();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        // Calculate new position and move the ball
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

    // Spawn a new obstacle with variety in size, color, and speed
    private void spawnObstacle() {
        View obstacle = new View(this);

        // Random size for the obstacle (between 50dp and 150dp)
        int obstacleSize = random.nextInt(100) + 50; // Random between 50 and 150
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(obstacleSize, obstacleSize);
        params.leftMargin = random.nextInt(getScreenWidth() - obstacleSize); // Random horizontal position
        params.topMargin = 0;
        obstacle.setLayoutParams(params);

        // Array of colors for variety
        int[] obstacleColors = {
                Color.RED,          // Red
                Color.BLUE,         // Blue
                Color.GREEN,        // Green
                Color.YELLOW        // Yellow
        };
        obstacle.setBackgroundColor(obstacleColors[random.nextInt(obstacleColors.length)]);

        // Add the obstacle to the layout
        mainLayout.addView(obstacle);

        // Calculate speed based on level (faster as level increases)
        int baseDuration = 2000; // Base duration for Level 1
        int levelFactor = Math.max(0, level - 1); // Increase difficulty from Level 2 onwards
        int duration = Math.max(500, baseDuration - (levelFactor * 300)); // Decrease duration for higher levels

        obstacle.animate()
                .translationY(getScreenHeight())
                .setDuration(duration)
                .withEndAction(() -> {
                    // Remove obstacle when it leaves the screen
                    mainLayout.removeView(obstacle);
                    if (!gameOver) {
                        score++; // Increment score
                        updateScoreText(); // Update score display
                        checkLevelUp(); // Check if player levels up
                        playSound(R.raw.score_sound); // Play score sound
                    }
                })
                .start();

        // Check for collision with the ball
        checkCollision(obstacle);
    }

    // Check if the ball collides with an obstacle
    private void checkCollision(View obstacle) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!gameOver && isColliding(ball, obstacle)) {
                    gameOver = true; // End the game
                    showGameOverDialog(); // Show game over dialog

                    // Change score background to red
                    scoreText.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                    scoreText.setTextColor(getResources().getColor(android.R.color.white));
                    vibrateEffect(ball); // Apply vibration effect to the ball
                    playSound(R.raw.game_over_sound); // Play game over sound
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

    // Get the screen width in pixels
    private int getScreenWidth() {
        return getResources().getDisplayMetrics().widthPixels;
    }

    // Get the screen height in pixels
    private int getScreenHeight() {
        return getResources().getDisplayMetrics().heightPixels;
    }

    // Update the score display text
    private void updateScoreText() {
        scoreText.setText("Score: " + score);
        vibrateEffect(scoreText); // Apply vibration effect to the score text
    }

    // Update the level display text
    private void updateLevelText() {
        levelText.setText("Level: " + level);
        vibrateEffect(levelText); // Apply vibration effect to the level text
    }

    // Check if the player has reached a new level (every 30 points)
    private void checkLevelUp() {
        int newLevel = (score / 30) + 1; // Calculate the level based on score (every 30 points)
        if (newLevel > level) { // If a new level is reached
            level = newLevel; // Update the player's level
            updateLevelText(); // Update the level display
            showLevelUpDialog(); // Show level-up dialog
        }
    }

    // Show a dialog when the player levels up
    private void showLevelUpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme);
        builder.setTitle("🎉 Level Up! 🎉")
                .setMessage("Congratulations!\nYou passed Level " + (level - 1) + "!\nNow you are on Level " + level + ".")
                .setCancelable(false)
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // Close the dialog and continue the game
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();

        // Customize button styles manually
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (positiveButton != null) {
            positiveButton.setBackgroundResource(R.drawable.button_background);
            positiveButton.setTextColor(Color.WHITE);
            positiveButton.setPadding(24, 12, 24, 12);
        }

        // Center the message text and ensure readability
        TextView messageText = (TextView) dialog.findViewById(android.R.id.message);
        if (messageText != null) {
            messageText.setTextSize(16);
            messageText.setTextColor(Color.BLACK);
            messageText.setGravity(android.view.Gravity.CENTER);
            messageText.setPadding(0, 0, 0, 32);
        }

        // Ensure the title is readable and add padding
        TextView titleText = (TextView) dialog.findViewById(getResources().getIdentifier("alertTitle", "id", "android"));
        if (titleText != null) {
            titleText.setTextColor(Color.BLACK);
            titleText.setPadding(0, 16, 0, 16);
        }

        // Add padding to the dialog content to create more space
        View dialogView = dialog.findViewById(android.R.id.content);
        if (dialogView != null) {
            dialogView.setPadding(24, 24, 24, 24);
        }
    }

    // Apply a vibration animation to a view
    private void vibrateEffect(View view) {
        view.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(100)
                .withEndAction(() -> view.animate().scaleX(1f).scaleY(1f).setDuration(100))
                .start();
    }

    // Play a sound effect from resources
    private void playSound(int soundResId) {
        MediaPlayer mediaPlayer = MediaPlayer.create(this, soundResId);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(MediaPlayer::release);
    }

    // Show a game over dialog with restart and exit options
    private void showGameOverDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme);
        builder.setTitle("🎮 Game Over 🎮")
                .setMessage("Oh no! You lost.\nYour score: " + score + "\nDo you want to try again or exit the game?")
                .setCancelable(false)
                .setPositiveButton("🔄 Restart", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Restart the game by recreating the activity
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

        // Customize button styles manually
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        if (positiveButton != null && negativeButton != null) {
            positiveButton.setBackgroundResource(R.drawable.button_background);
            negativeButton.setBackgroundResource(R.drawable.button_background);
            positiveButton.setTextColor(Color.WHITE);
            negativeButton.setTextColor(Color.WHITE);
            positiveButton.setPadding(24, 12, 24, 12);
            negativeButton.setPadding(24, 12, 24, 12);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(24, 0, 24, 0);
            positiveButton.setLayoutParams(params);
            negativeButton.setLayoutParams(params);
        }

        TextView messageText = (TextView) dialog.findViewById(android.R.id.message);
        if (messageText != null) {
            messageText.setTextSize(16);
            messageText.setTextColor(Color.BLACK);
            messageText.setGravity(android.view.Gravity.CENTER);
            messageText.setPadding(0, 0, 0, 32);
        }

        TextView titleText = (TextView) dialog.findViewById(getResources().getIdentifier("alertTitle", "id", "android"));
        if (titleText != null) {
            titleText.setTextColor(Color.BLACK);
            titleText.setPadding(0, 16, 0, 16);
        }

        View dialogView = dialog.findViewById(android.R.id.content);
        if (dialogView != null) {
            dialogView.setPadding(24, 24, 24, 24);
        }
    }

    // Clean up Handler when the activity is destroyed
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null); // Prevent memory leaks
    }
}