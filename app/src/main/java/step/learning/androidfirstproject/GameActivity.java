package step.learning.androidfirstproject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import step.learning.androidfirstproject.OnSwipeLisner;
import step.learning.androidfirstproject.R;

public class GameActivity extends AppCompatActivity {

    private static final int N = 4;
    ArrayList<int[][]> cellsList = new ArrayList<int[][]>();
    private final int[][] cells = new int[N][N] ;
    private final int[][] prevCells = new int[N][N] ;  // for UNDO action
    private final int[][] tmpCells = new int[N][N] ;  // for UNDO action
    private final TextView[][] tvCells = new TextView[N][N] ;
    private final Random random = new Random() ;
    private int tmpScoreNow;
    private int prevScoreNow;
    private int countSteps=0;
    private int score ;
    private int bestScore ;
    private TextView tvScore ;
    private TextView tvBestScore ;
    private Animation spawnCellAnimation ;
    private Animation finishCellAnimation ;
    private Animation collapseCellsAnimation ;
    private MediaPlayer spawnSound ;
    private static final String bestScoreFilename = "best_score" ;
    private static final String saveCellsGameFilename = "save_game_cells" ;
    private static final String saveScoreGameFilename = "save_game_score" ;
    private boolean collapse;
    private MediaPlayer collapseSound;
    private MediaPlayer SoundScore100;
    private MediaPlayer NoSwipeSound;
    private CheckBox checkBox;
    private boolean finish=false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_game );

        NoSwipeSound = MediaPlayer.create( GameActivity.this, R.raw.skibidi_toilet3 ) ;
        spawnSound = MediaPlayer.create( GameActivity.this, R.raw.skibidi_toilet2 ) ;
        collapseSound = MediaPlayer.create( GameActivity.this, R.raw.skibidi_toilet1 ) ;
        SoundScore100 = MediaPlayer.create( GameActivity.this, R.raw.skibidi_toilet ) ;
        tvScore = findViewById( R.id.game_tv_score ) ;
        tvBestScore = findViewById( R.id.game_tv_best_score ) ;

        findViewById(R.id.game_btn_new_game).setOnClickListener(this::newGameClick);
        findViewById(R.id.game_btn_undo).setOnClickListener(this::undoCameClick);
        checkBox = findViewById(R.id.game_checkbox_sound);

        // завантажуємо анімацію
        spawnCellAnimation = AnimationUtils.loadAnimation(
                GameActivity.this,
                R.anim.game_spawn_cell
        ) ;
        // ініціалізуємо анімацію
        spawnCellAnimation.reset() ;

        collapseCellsAnimation = AnimationUtils.loadAnimation(
                GameActivity.this,
                R.anim.game_collapse_cells
        ) ;
        collapseCellsAnimation.reset();

        finishCellAnimation = AnimationUtils.loadAnimation(
                GameActivity.this,
                R.anim.game_collapse_finish
        ) ;
        collapseCellsAnimation.reset();

        // Збираємо посилання на комірки ігрового поля
        for( int i = 0; i < N; i++ ) {
            for( int j = 0; j < N; j++ ) {
                tvCells[i][j] = findViewById( getResources().getIdentifier( "game_cell_" + i + j,"id",getPackageName()
                        )
                ) ;
            }
        }

        TableLayout tableLayout = findViewById( R.id.game_table ) ;
        tableLayout.post( () -> {   // відкладений запуск (на кадр прорисовки)
            int margin = 7 ;
            int w = this.getWindow().getDecorView().getWidth() - 2 * margin;
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams( w, w ) ;
            layoutParams.setMargins( 7, 50, 7, 50 );
            layoutParams.gravity = Gravity.CENTER ;
            tableLayout.setLayoutParams( layoutParams ) ;
        } ) ;
        tableLayout.setOnTouchListener(
                new OnSwipeLisner( GameActivity.this ) {
                    @Override
                    public void onSwipeBottom() {
                        processMove( MoveDirection.BOTTOM );
                    }
                    @Override
                    public void onSwipeLeft() {
                        processMove( MoveDirection.LEFT );
                    }
                    @Override
                    public void onSwipeRight() {
                        processMove( MoveDirection.RIGHT );
                    }
                    @Override
                    public void onSwipeTop() {
                        processMove( MoveDirection.TOP );
                    }
                } );

        findViewById( R.id.game_btn_new_game ).setOnClickListener( this::newGameClick );
        findViewById( R.id.game_btn_undo ).setOnClickListener( this::undoCameClick );

        findViewById( R.id.save_game ).setOnClickListener( this::saveGame );
        findViewById( R.id.load_game ).setOnClickListener( this::loadGame );

        loadBestScore();
        startNewGame() ;
    }

    private void setCellsList(int [][] cells_ )
    {
        int [][] x = new int[N][N];
        for (int i = 0; i < N; i++) {
            System.arraycopy(cells_[i],0,x[i],0,N);
        }
        cellsList.add(x);
    }
    private void saveGame(View view)
    {
        saveScoreGame();
        saveCellsGame();
    }
    private void loadGame(View view)
    {
        loadScoreGame();
        loadCellsGame();
        showField() ;
    }
    private void newGameClick(View view) {
        showNewGameDialog();
    }

    private void  undoCameClick(View view)
    {
        undoCame_Click();
    }
    private void  undoCame_Click()
    {
        if(countSteps>0) {
            score = prevScoreNow;
            countSteps--;
            int [][] temp = cellsList.get(countSteps);
            for (int i = 0; i < N; i++) {
                System.arraycopy(temp[i], 0, cells[i], 0, N);
            }
            showField();
        }
    }
    private void saveScoreGame()
    {
        try( FileOutputStream outputStream =
                     openFileOutput( saveScoreGameFilename, Context.MODE_PRIVATE ) ;
             DataOutputStream writer = new DataOutputStream( outputStream )
        ) {
            writer.writeInt( score ) ;
            writer.flush() ;
            Log.d( "saveScore", "save OK" ) ;
        }
        catch( IOException ex ) {
            Log.e( "saveBestScore", Objects.requireNonNull( ex.getMessage() ) ) ;
        }
    }
    private void loadScoreGame() {
        try( FileInputStream inputStream = openFileInput( saveScoreGameFilename ) ;
             DataInputStream reader = new DataInputStream( inputStream )
        ) {
            score = reader.readInt() ;
            Log.d( "loadBestScore", "score read: " + bestScore ) ;
        }
        catch( IOException ex ) {
            bestScore = 0 ;
            Log.e( "loadBestScore", Objects.requireNonNull( ex.getMessage() ) ) ;
        }
    }
    private void saveCellsGame() {
                ObjectOutputStream oos = null;

        try( FileOutputStream outputStream =
                     openFileOutput( saveCellsGameFilename, Context.MODE_PRIVATE ) ;
             DataOutputStream writer = new DataOutputStream( outputStream )
        ){

            if (writer != null) {
                oos = new ObjectOutputStream(writer);
                oos.writeObject(cells);
            }
        } catch (FileNotFoundException e) {
            Log.d("saveGame", "FileNotFoundException");
            System.out.println(e.getMessage());
        } catch (IOException e) {
            Log.e("saveGame", "IOException: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    Log.e("saveGame", "Error closing stream: " + e.getMessage());
                }
            }
        }
    }
    private void loadCellsGame() {
                try( FileInputStream inputStream = openFileInput( saveCellsGameFilename ) ;
             ObjectInputStream reader = new ObjectInputStream( inputStream )
        ) {

                    int[][]s =  (int[][])reader.readObject();
            for( int i = 0; i < s.length; i++ ) {
                for( int j = 0; j < s.length; j++ ) {
                  cells[i][j]=s[i][j];
                }
            }

            Log.d( "loadBestScore", "Best score read: " + bestScore ) ;
        }
        catch (FileNotFoundException e)
        { Log.d( "loadGame", "FileNotFoundException "  ) ;
            System.out.println(e.getMessage());
        }
        catch( IOException ex ) {
            bestScore = 0 ;
            Log.e( "loadBestScore", Objects.requireNonNull( ex.getMessage() ) ) ;
        }
        catch (ClassNotFoundException e)
        {   Log.d( "loadGame", "ClassNotFoundException "  ) ;
            System.out.println(e.getMessage());}
    }

    private void saveBestScore() {
        /* Android має розподілену файлову систему. У застосунку є вільний
         * доступ до приватних файлів, які є частиною роботи та автоматично
         * видаляються разом з застосунком. Є спільні ресурси (картинки, завантаження
         * тощо) доступ до яких зазначається у маніфесті та має погоджуватись
         * дозволом користувача. Інші файли можуть виявитись недоступними. */
        try( FileOutputStream outputStream =
                     openFileOutput( bestScoreFilename, Context.MODE_PRIVATE ) ;
             DataOutputStream writer = new DataOutputStream( outputStream )
        ) {
            writer.writeInt( bestScore ) ;
            writer.flush() ;
            Log.d( "saveBestScore", "save OK" ) ;
        }
        catch( IOException ex ) {
            Log.e( "saveBestScore", Objects.requireNonNull( ex.getMessage() ) ) ;
        }
    }
    private void loadBestScore() {
        try( FileInputStream inputStream = openFileInput( bestScoreFilename ) ;
             DataInputStream reader = new DataInputStream( inputStream )
        ) {
            bestScore = reader.readInt() ;
            Log.d( "loadBestScore", "Best score read: " + bestScore ) ;
        }
        catch( IOException ex ) {
            bestScore = 0 ;
            Log.e( "loadBestScore", Objects.requireNonNull( ex.getMessage() ) ) ;
        }
    }
    private void startNewGame() {
        for( int i = 0; i < N; i++ ) {
            for( int j = 0; j < N; j++ ) {
                cells[i][j] = 0 ;
            }
        }
        score = 0 ;
        loadBestScore();
        tvBestScore.setText( getString( R.string.game_tv_best, bestScore ) );
        // cells[1][1] = 8;
        spawnCell() ;
        spawnCell() ;
        showField() ;
    }

    /**
     * Поява нового числа на полі
     * @return чи додалось число (є вільні комірки)
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
                if(score>100&&score<180 ||score>500&&score<600 ||score>1000&&score<1100
                        ||score>1500&&score<1600 ||score>2000&&score<2100 ||score>2500&&score<2600)
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
     * Показ поля - відображення числових даних на View та
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

        // Resource with placeholder (%d in resource for number)
        tvScore.setText( getString( R.string.game_tv_score, score ) );
    }

    private void processMove(MoveDirection direction)
    {   countSteps++;
        tmpScoreNow=score;

        for (int i = 0; i < N; i++) {
            System.arraycopy(cells[i],0,tmpCells[i],0,N);
        }
        if(move(direction))
        {
            setCellsList(tmpCells);

            prevScoreNow=tmpScoreNow;
            for (int i = 0; i < N; i++) {
                System.arraycopy(tmpCells[i],0,prevCells[i],0,N);
            }
            spawnCell();
            showField();
            if(isGameFail())// немає більше ходів
            {
                showFailDialog();
            }
            else {
                if(score>bestScore)
                {
                    bestScore=score;
                    saveBestScore();
                    tvBestScore.setText(getString(R.string.game_tv_best, bestScore));
                }
            }
            if(finish==false)
            {finishGame();}

        }
        else {
            NoSwipeSound.start();
            Toast.makeText(GameActivity.this, "Немає руху", Toast.LENGTH_SHORT).show();
        }

    }

    private void showFailDialog() {
        new AlertDialog.Builder( this, androidx.appcompat.R.style.Theme_AppCompat_Dialog_Alert )
                .setIcon( android.R.drawable.ic_dialog_alert )
                .setTitle( R.string.game_over )
                .setMessage( R.string.game_over_dialog )
                .setCancelable( false )
                .setPositiveButton( R.string.game_over_yes,
                        ( DialogInterface dialog, int whichButton ) -> startNewGame()
                )
                .setNegativeButton( R.string.game_over_no,
                        ( DialogInterface dialog, int whichButton ) -> finish()   // закрити активність
                )
                .setNeutralButton( R.string.game_over_undo,
                        ( DialogInterface dialog, int whichButton ) ->
                                dialog.dismiss()
                )
                .show();
    }
    private void showNewGameDialog() {
        new AlertDialog.Builder( this, androidx.appcompat.R.style.Base_Theme_AppCompat_Dialog_Alert)
                .setIcon(android.R.drawable.ic_menu_myplaces)
                .setTitle( R.string.game_start_new_game )
                .setCancelable(false)
                .setPositiveButton( R.string.game_over_yes,( DialogInterface dialog, int whichButton )
                        -> startNewGame()
                )
                .setNegativeButton( R.string.game_over_no,  ( DialogInterface dialog, int whichButton )
                        -> dialog.dismiss()  )// закривати активнысть)
                .setNeutralButton( R.string.game_over_undo,  ( DialogInterface dialog, int whichButton )
                        ->   undoCame_Click() )
                .show();
    }
    private boolean move( MoveDirection direction ) {
        switch( direction ) {
            case BOTTOM: return moveBottom() ;
            case LEFT: return moveLeft() ;
            case RIGHT: return moveRight() ;
            case TOP: return moveTop() ;
        }
        return false ;
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
                    score += cells[i][j];
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
                    score += cells[i][j];
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
                    score += cells[i][j];
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
                    score += cells[i][j];
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

    private boolean isGameFail() {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (cells[i][j] == 0) {
                    return false ;
                }
            }
        }
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N-1; j++) {
                if (cells[i][j] == cells[i][j + 1]) {
                    return false ;
                }
            }
        }
        for (int i = 0; i < N-1; i++) {
            for (int j = 0; j < N; j++)  {
                if (cells[i][j] == cells[i + 1][j]) {
                    return false ;
                }
            }
        }
        return true ;
    }

    private void finishGame()
    {
        for (int i = 0; i < N; i++) {

                for (int j = 0; j < N; j++) {
                    if (cells[i][j] ==2048) {
                        finish=true;
                        showFinishGame();
                        showFinishGameDialog();
                    }
                }
            }
    }
    private void showFinishGameDialog() {
        new AlertDialog.Builder( this, androidx.appcompat.R.style.Base_Theme_AppCompat_Dialog_Alert)
                .setIcon(android.R.drawable.ic_menu_myplaces)
                .setTitle( R.string.game_finish_title )
                .setMessage( R.string.game_finish_message )
                .setCancelable(false)
                .setPositiveButton( R.string.game_over_yes,( DialogInterface dialog, int whichButton )
                        -> dialog.dismiss()
                )
                .setNegativeButton( R.string.game_over_no,  ( DialogInterface dialog, int whichButton )
                        ->  startNewGame() )// закривати активнысть)
                .setNeutralButton( R.string.game_over_undo,  ( DialogInterface dialog, int whichButton )
                        ->   undoCame_Click() )
                .show();
    }
    private void showFinishGame()
    {
        for (int i = 0; i < N; i++) {

            for (int j = 0; j < N; j++) {
                tvCells[i][j].startAnimation(finishCellAnimation);
            }
        }
    }
    private enum MoveDirection {
        BOTTOM,
        LEFT,
        RIGHT,
        TOP
    }

}
/*
Д.З. Завершити роботу з проєктом "2048"
- Видавати повідомлення при натисканні кнопки "нова гра"
- Забезпечити при повернені руху (undo) також повернення рахунку та рекорду (якщо треба)
- Заблокувати роботу кнопки (undo) відразу після старту нової гри
- Реалізувати перевірку позитивного завершення гри (набір 2048 хоча у на одній комірці)
   передбачити можливість продовження гри (повторні повідомлення не видавати)
- Заблокувати повороти активності або реалізувати дизайн у ландшафтній орієнтації
** Повертання ходів далі ніж на один
 */
/*
Анімації (double-anim) - плавні переходи числових параметрів
між початковим та кінцевим значеннями. Закладаються декларативно (у xml)
та проробляються ОС.
Створюємо ресурсну папку (anim, назва важлива)
у ній - game_spawn_cell.xml (див. коментарі у ньому)
Завантажуємо анімацію (onCreate)  та ініціалізуємо її
Призначаємо (викликаємо) анімацію при появі комірки (див. spawnCell)
 */
/*
Car{
    @WHERE(" IS NOT NULL ")
    cond,
    @WHERE(" = 'robot' " )
    automat,
    ... }
Request{ cond=true, }
sql = SELECT * FROM cars WHERE is_sold = true

        for( Field field : Car.class.getDeclaredFields() ) {
            String par = field.getName() // cond, automat,
        for( par : {"cond", "automat"})
            if( req.getParameter(par) != null ) {
              sql += " AND " + par + field.getAnnotation( WHERE.class )
            }
        }
        if( req.getParameter("min-price") != null ) {
              sql += " AND price >= " + req.getParameter("min-price")
            }

        SELECT * FROM cars WHERE is_sold = true AND cond IS NOT NULL AND automat IS NOT NULL
 */