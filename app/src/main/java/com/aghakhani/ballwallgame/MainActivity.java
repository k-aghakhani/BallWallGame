package com.aghakhani.ballwallgame;

import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private View ball;
    private TextView scoreText;
    private RelativeLayout mainLayout;
    private int score = 0;
    private boolean gameOver = false;
    private Handler handler = new Handler();
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        // اتصال عناصر UI به کد جاوا
        ball = findViewById(R.id.ball);
        scoreText = findViewById(R.id.scoreText);
        mainLayout = findViewById(R.id.mainLayout);

        // تنظیم حرکت توپ با کشیدن انگشت
        ball.setOnTouchListener(new View.OnTouchListener() {
            float initialX, initialTouchX;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = ball.getX();
                        initialTouchX = event.getRawX();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float deltaX = event.getRawX() - initialTouchX;
                        float newX = initialX + deltaX;
                        // محدود کردن حرکت توپ به داخل صفحه
                        if (newX >= 0 && newX <= getScreenWidth() - ball.getWidth()) {
                            ball.setX(newX);
                        }
                        break;
                }
                return true;
            }
        });

        // شروع تولید موانع
        startObstacleSpawner();
    }

    // شروع تولید موانع
    private void startObstacleSpawner() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!gameOver) {
                    spawnObstacle();
                    handler.postDelayed(this, 1000); // ایجاد مانع هر ۱ ثانیه
                }
            }
        }, 1000);
    }

    // ایجاد یک مانع جدید
    private void spawnObstacle() {
        View obstacle = new View(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        params.leftMargin = random.nextInt(getScreenWidth() - 100); // موقعیت تصادفی افقی
        params.topMargin = 0;
        obstacle.setLayoutParams(params);
        obstacle.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));

        // اضافه کردن مانع به صفحه
        mainLayout.addView(obstacle);

        // حرکت مانع به پایین
        obstacle.animate()
                .translationY(getScreenHeight())
                .setDuration(2000)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        // حذف مانع از صفحه پس از پایان حرکت
                        mainLayout.removeView(obstacle);
                        if (!gameOver) {
                            score++;
                            scoreText.setText("Score: " + score);
                        }
                    }
                })
                .start();

        // بررسی برخورد توپ با مانع
        checkCollision(obstacle);
    }

    // بررسی برخورد توپ با مانع
    private void checkCollision(View obstacle) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!gameOver && isColliding(ball, obstacle)) {
                    gameOver = true;
                    scoreText.setText("Game Over! Score: " + score);
                } else if (!gameOver) {
                    handler.postDelayed(this, 10); // بررسی برخورد هر ۱۰ میلی‌ثانیه
                }
            }
        }, 10);
    }

    // بررسی آیا دو ویو با هم برخورد کرده‌اند
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

    // دریافت عرض صفحه
    private int getScreenWidth() {
        return getResources().getDisplayMetrics().widthPixels;
    }

    // دریافت ارتفاع صفحه
    private int getScreenHeight() {
        return getResources().getDisplayMetrics().heightPixels;
    }
}