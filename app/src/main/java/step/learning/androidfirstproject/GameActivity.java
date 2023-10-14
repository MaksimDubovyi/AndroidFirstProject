package step.learning.androidfirstproject;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.media.MediaParser;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity {
    private static final int N = 4;
    private final int[][] cells = new int[N][N];
    private final TextView[][] tvCells = new TextView[N][N];
    private final Random random = new Random();

    private int scoreNow=0;
    private int scoreMax= random.nextInt(1000);;
    private TextView tvScoreMax;
    private TextView tvScoreNow;
    private Animation spawnCellAnimation;
    private Animation collapseCellsAnimation;
    private MediaPlayer spawnSound;
    private MediaPlayer collapseSound;
    private MediaPlayer SoundScore100;
    private MediaPlayer NoSwipeSound;
    private CheckBox checkBox;
    private  boolean collapse;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        NoSwipeSound = MediaPlayer.create( GameActivity.this, R.raw.skibidi_toilet3 ) ;
        spawnSound = MediaPlayer.create( GameActivity.this, R.raw.skibidi_toilet2 ) ;
        collapseSound = MediaPlayer.create( GameActivity.this, R.raw.skibidi_toilet1 ) ;
        SoundScore100 = MediaPlayer.create( GameActivity.this, R.raw.skibidi_toilet ) ;

        checkBox = findViewById(R.id.game_checkbox_sound);

            // завантажуємо анімацію
        spawnCellAnimation= AnimationUtils.loadAnimation(
                GameActivity.this, R.anim.game_spawn_cell
        );
            // ініціалізуємо анімацію
        spawnCellAnimation.reset();
        // завантажуємо анімацію
        collapseCellsAnimation= AnimationUtils.loadAnimation(
                GameActivity.this, R.anim.game_collapse_cells
        );
        // ініціалізуємо анімацію
        collapseCellsAnimation.reset();

        // Збираємо посилання на комірки ігрового поля
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                tvCells[i][j] = findViewById(getResources().getIdentifier(
                                "game_cell_" + i + j,
                                "id",
                                getPackageName()
                        )
                );
            }
        }

        tvScoreNow =  findViewById(getResources().getIdentifier("score_now", "id", getPackageName() ));
        tvScoreMax =  findViewById(getResources().getIdentifier("score_max", "id", getPackageName() ));

        TableLayout tableLayout = findViewById( R.id.game_table ) ;
        tableLayout.post( () -> {// відкладений запуск на кадр прорисовки
            int margin = 7 ;
            int w = this.getWindow().getDecorView().getWidth() - 2 * margin;
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams( w, w ) ;
            layoutParams.setMargins( 7, 50, 7, 50 );
            tableLayout.setLayoutParams( layoutParams ) ;
        } ) ;
        tableLayout.setOnTouchListener(new OnSwipeLisner(GameActivity.this) {
            @Override
            public void onSwipeBottom() {
                if(moveBottom())
                {
                    spawnCell();
                    showField();
                }
                else {
                    NoSwipeSound.start();
                    Toast.makeText(GameActivity.this, "Немає руху", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onSwipeLeft() {
                if(moveLeft())
                {
                    spawnCell();
                    showField();
                }
                else {
                    NoSwipeSound.start();
                    Toast.makeText(GameActivity.this, "Немає руху", Toast.LENGTH_SHORT).show();
                }

            }
            @Override
            public void onSwipeRight() {
                if(moveRight())
                {
                    spawnCell();
                    showField();
                }
                else {
                    NoSwipeSound.start();
                    Toast.makeText(GameActivity.this, "Немає руху", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onSwipeTop() {
                if(moveTop())
                {
                    spawnCell();
                    showField();
                }
                else {
                    NoSwipeSound.start();
                    Toast.makeText(GameActivity.this, "Немає руху", Toast.LENGTH_SHORT).show();
                }
            }
        });

        spawnCell();
        spawnCell();
        showField();
    }


    /**
     * Поява нового числа
     *
     * @return чи додалось число (є вільні комірки на полі)
     */
    private boolean spawnCell() {// Оскільки не відомо де і скільки порожніх комірок шукаємо їх всі
        List<Integer> freeCellIndexes = new ArrayList<>();

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (cells[i][j] == 0) {//Ознака порожньї комірки складаємо її координати в один індекс і зберігаємо
                    freeCellIndexes.add(10 * i + j);
                }

            }
        }
        // Перевіряємо чи є пусті комірки
        int cnt = freeCellIndexes.size();
        if (cnt == 0) {
            return false;
        }

        // Генеруємо випадковий індекс в межах довжини масиву
        int randIndex = random.nextInt(cnt);
        //Вилучаємо зібраний індекс комірки
        int randCellIndex = freeCellIndexes.get(randIndex);
        //розділяємо його на координати
        int x = randCellIndex / 10;
        int y = randCellIndex % 10;

        // генеруємо випадкове число : 2 (з імовірністю 0,9) чи 4 (0,1)
        cells[x][y] =
                random.nextInt(10) == 0 //щдин з 10
                        ? 4  // цей блок з імовірністю 1/10
                        : 2; // цей всі інші випадки
        //призначаємо анімацію появи для View комірки
        tvCells[x][y].startAnimation(spawnCellAnimation);
        //програємо звук
        if(checkBox.isChecked())
        {
            if( collapse==true)
            {
                collapseSound.start();
                if(scoreNow>100&&scoreNow<180 ||scoreNow>500&&scoreNow<600 ||scoreNow>1000&&scoreNow<1100
                        ||scoreNow>1500&&scoreNow<1600 ||scoreNow>2000&&scoreNow<2100 ||scoreNow>2500&&scoreNow<2600)
                {
                    SoundScore100.start();
                }
            }
            else {
                spawnSound.start();
            }
        }

        return true;
    }

    /**
     * Показ поля - відображення числових даних на View  та
     * підбір стилів у відповідності до значення числа
     */
    private void showField() {
        // Особливість - деякі параметри "на льоту" можна змінювати
        // через стилі, але не всі, для деяких доводиться подавати
        // окремі інструкції
        Resources resources = getResources() ;
        for( int i = 0; i < N; i++ ) {
            for( int j = 0; j < N; j++ ) {
                // встановлюємо текст - зображення числа у масиві
                tvCells[i][j].setText( String.valueOf( cells[i][j] ) );

                // змінюємо стиль у відповідності значення cells[i][j]
                tvCells[i][j].setTextAppearance(
                        resources.getIdentifier(
                                "game_cell_" + cells[i][j],
                                "style",
                                getPackageName()
                        )
                );

                // окремо змінюємо background, через стиль ігнорується
                tvCells[i][j].setBackgroundColor(
                        resources.getColor(
                                resources.getIdentifier(
                                        "game_bg_" + cells[i][j],
                                        "color",
                                        getPackageName()
                                ),
                                getTheme()
                        )
                );
            }
        }

        // Resource with placeholder (%d in resource
        tvScoreNow.setText( getString(R.string.game_tv_score,scoreNow) );
    }


    private boolean moveRight() {
        boolean result = false;
        collapse =false;
        // все переміщуємо ліворуч
        boolean needRepeat;
        for (int i = 0; i < N; i++) {
            do {
                needRepeat = false;
                for (int j = N - 1; j > 0; j--) {
                    if (cells[i][j] == 0 && cells[i][j - 1] != 0) {
                        cells[i][j] = cells[i][j - 1];
                        cells[i][j - 1] = 0;
                        needRepeat = true;
                        result = true;
                    }
                }
            } while (needRepeat);
            for (int j = N - 1; j > 0; j--) {
                if (cells[i][j] != 0 && cells[i][j] == cells[i][j - 1]) {
                    collapse=true;
                    cells[i][j] *= 2;
                    //Animation
                    tvCells[i][j].startAnimation(collapseCellsAnimation);
                    scoreNow += cells[i][j];
                    for (int k = j - 1; k > 0; k--) {
                        cells[i][k] = cells[i][k - 1];
                    }
                    cells[i][0] = 0;
                    result = true;
                }
            }
        }
        return result;
    }
    private boolean moveLeft() {
        collapse=false;
        boolean result = false;
        // все переміщуємо ліворуч
        boolean needRepeat;
        for (int i = 0; i < N; i++) {
            do {
                needRepeat = false;
                for (int j = 0; j < N - 1; j++) {
                    if (cells[i][j] == 0 && cells[i][j + 1] != 0) {
                        cells[i][j] = cells[i][j + 1];
                        cells[i][j + 1] = 0;
                        needRepeat = true;
                        result = true;
                    }
                }
            } while (needRepeat);
            for (int j = 0; j < N - 1; j++) {
                if (cells[i][j] != 0 && cells[i][j] == cells[i][j + 1]) {
                    collapse=true;
                    cells[i][j] *= 2;
                    //Animation
                    tvCells[i][j].startAnimation(collapseCellsAnimation);
                    scoreNow += cells[i][j];
                    for (int k = j + 1; k < N - 1; k++) {
                        cells[i][k] = cells[i][k + 1];
                    }
                    cells[i][N - 1] = 0;
                    result = true;
                }
            }
        }
        //перевіряемо
        //переміщуємо ліворуч
        return result;
    }
    private boolean moveTop() {
        collapse=false;
        boolean result = false;
        boolean needRepeat;
        for (int j = 0; j < N; j++) {
            do {
                needRepeat = false;
                for (int i = 0; i < N - 1; i++) {
                    if (cells[i][j] == 0 && cells[i + 1][j] != 0) {
                        cells[i][j] = cells[i + 1][j];
                        cells[i + 1][j] = 0;
                        needRepeat = true;
                        result = true;
                    }
                }
            } while (needRepeat);
            for (int i = 0; i < N - 1; i++) {
                if (cells[i][j] != 0 && cells[i][j] == cells[i + 1][j]) {
                    collapse=true;
                    cells[i][j] *= 2;
                    //Animation
                    tvCells[i][j].startAnimation(collapseCellsAnimation);
                    scoreNow += cells[i][j];
                    for (int k = j + 1; k < N - 1; k++) {
                        cells[k][j] = cells[k + 1][j];
                    }
                    cells[N - 1][j] = 0;
                    result = true;
                }
            }
        }
        return result;
    }
    private boolean moveBottom() {
        collapse=false;
        boolean result = false;
        boolean needRepeat;
        for (int j = 0; j < N; j++) {
            do {
                needRepeat = false;
                for (int i = N - 1; i > 0; i--) {
                    if (cells[i][j] == 0 && cells[i - 1][j] != 0) {
                        cells[i][j] = cells[i - 1][j];
                        cells[i - 1][j] = 0;
                        needRepeat = true;
                        result = true;
                    }
                }} while (needRepeat);
            for (int i = N - 1; i > 0; i--) {
                if (cells[i][j] != 0 && cells[i][j] == cells[i - 1][j]) {
                    collapse=true;
                    cells[i][j] *= 2;
                    tvCells[i][j].startAnimation(collapseCellsAnimation);
                    scoreNow += cells[i][j];
                    for (int k = i - 1; k > 0; k--) {
                        cells[k][j] = cells[k - 1][j];
                    }
                    cells[0][j] = 0;
                    result = true;
                }
            }
        }
        return result;
    }

}

/*
Анімації (double-anim) - плавні переходи числових параметрів
між початковим та кінцевим значеннями. Закладаються декларативно (у xml)
та проробляються ОС.
Створюємо ресурсну папку (anim, назва важлива)
у ній - game_spawn_cell.xml (див. коментарі у ньому)
Завантажуємо анімацію (onCreate)  та ініціалізуємо її
Призначаємо (викликаємо) анімацію при появі комірки (див. spawnCell)
 */