package step.learning.androidfirstproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class GameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        findViewById(R.id.game_layout).setOnTouchListener(new OnSwipeLisner(GameActivity.this)
        {
            @Override
            public void onSwipeBottom() {
                Toast.makeText(  /// Повідомлення що зявляється та зникає  з часом
                        GameActivity.this,
                        "onSwipeBottom",
                        Toast.LENGTH_SHORT // час відображення повідомлення
                ).show();
            }

            @Override
            public void onSwipeLeft() {
                Toast.makeText(GameActivity.this, "onSwipeLeft", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSwipeRight() {
                Toast.makeText(GameActivity.this, "onSwipeRight", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSwipeTop() {
                Toast.makeText(GameActivity.this, "onSwipeTop", Toast.LENGTH_SHORT).show();
            }
        });

    }


}