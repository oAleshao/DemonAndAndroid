package itstep.learning.demonandandroid;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.logging.Logger;

public class G2048Activity extends AppCompatActivity {

    private final String best_score_filename = "best_score.2048";
    private final int N = 4;
    private final int[][] tiles = new int[N][N];
    private int[][] undoTiles;
    private final TextView[][] tvTiles = new TextView[N][N];
    private final Random random = new Random();

    private Animation spawnAnimation, collapseAnimation;
    private TextView tvScore, tvBestScore;
    private int score, prevScore, bestScore, prevBestScore;
    private boolean userGotNewScore = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_g2048);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.game_2048), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        spawnAnimation = AnimationUtils.loadAnimation(this, R.anim.g2048_spawn);
        collapseAnimation = AnimationUtils.loadAnimation(this, R.anim.g2048_scale);

        LinearLayout gameField = findViewById(R.id.g2048_ll_field);
        tvScore = findViewById(R.id.g2048_tv_score);
        tvBestScore = findViewById(R.id.g2048_tv_best_score);
        gameField.post(() -> {
            int vw = this.getWindow().getDecorView().getWidth();
            int fieldMargin = 20;
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    vw - 2 * fieldMargin,
                    vw - 2 * fieldMargin
            );


            layoutParams.setMargins(fieldMargin, fieldMargin, fieldMargin, fieldMargin);
            layoutParams.gravity = Gravity.CENTER;
            gameField.setLayoutParams(layoutParams);

        });

        //NumberFormat.getInstance(Locale.ROOT).parse("2.48").doubleValue();
        gameField.setOnTouchListener(new OnSwipeListener(G2048Activity.this) {
            @Override
            public void onSwipeBottom() {
                if (canMoveBottom()) {
                    saveField();
                    moveBottom();
                    spawnTile();
                    showField();
                } else {
                    Toast.makeText(G2048Activity.this, "No Bottom Move", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onSwipeLeft() {
                if (canMoveLeft()) {
                    saveField();
                    moveLeft();
                    spawnTile();
                    showField();
                } else {
                    Toast.makeText(G2048Activity.this, "No Left Move", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onSwipeRight() {
                if (moveRight()) {
                    spawnTile();
                    showField();
                } else {
                    Toast.makeText(G2048Activity.this, "No Right Move", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onSwipeTop() {
                if (true) {
                    moveTop();
//                    spawnTile();
                    showField();
                } else {
                    Toast.makeText(G2048Activity.this, "No Top Move", Toast.LENGTH_SHORT).show();
                }
            }
        });
        findViewById(R.id.g2048_btn_undo).setOnClickListener(v -> undoMove());
        findViewById(R.id.g2048_btn_new).setOnClickListener(v -> newGame());
        newGame();
    }

    private void showUndoMessage() {

        new AlertDialog.Builder(this)
                .setTitle("limitation")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage("You cannot make a move")
                .setNeutralButton("Close", (dlg, btn) -> {
                })
                .setPositiveButton("Subscribe", (dlg, btn) -> {
                    Toast.makeText(this, "Soon", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Exit", (dlg, btn) -> {
                })
                .setCancelable(false)
                .show();
    }

    private void saveBestScore() {
        try (FileOutputStream fos = openFileOutput(best_score_filename, Context.MODE_PRIVATE);
             DataOutputStream writer = new DataOutputStream(fos)) {
            writer.writeInt(bestScore);
            writer.flush();
        } catch (Exception ex) {
            Log.e("G2048Activity::saveBestScore",
                    ex.getMessage() != null ? ex.getMessage() : "Error writing file");
        }
    }

    private void loadBestScore() {
        try (FileInputStream fis = openFileInput(best_score_filename);
             DataInputStream reader = new DataInputStream(fis)) {
            bestScore = prevBestScore = reader.readInt();
        } catch (Exception ex) {
            Log.e("G2048Activity::loadBestScore",
                    ex.getMessage() != null ? ex.getMessage() : "Error reading file");
        }
    }

    private void newGame() {
        initField();
        spawnTile();
        showField();
    }

    private void saveField() {
        undoTiles = new int[N][N];
        for (int i = 0; i < N; i++) {
            System.arraycopy(tiles[i], 0, undoTiles[i], 0, N);
        }
        prevScore = score;
    }

    private void undoMove() {
        if (undoTiles == null) {
            showUndoMessage();
            return;
        }
        for (int i = 0; i < N; i++) {
            System.arraycopy(undoTiles[i], 0, tiles[i], 0, N);
        }
        undoTiles = null;
        score = prevScore;
        showField();
    }

    private boolean canMoveLeft() {
        for (int i = 0; i < N; i++) {
            for (int j = 1; j < N; j++) {
                if (tiles[i][j] != 0 && (tiles[i][j - 1] == 0 || tiles[i][j] == tiles[i][j - 1])) {
                    return true;
                }
            }
        }
        return false;
    }

    private void moveLeft() {
        for (int i = 0; i < N; i++) {      // [4 2 2 4]
            int j0 = -1;
            for (int j = 0; j < N; j++) {
                if (tiles[i][j] != 0) {
                    if (j0 == -1) {
                        j0 = j;
                    } else {
                        if (tiles[i][j] == tiles[i][j0]) {  // collapse
                            tiles[i][j] *= 2;
                            score += tiles[i][j];
                            tvTiles[i][j].setTag(collapseAnimation);
                            tiles[i][j0] = 0;
                            j0 = -1;
                        } else {
                            j0 = j;
                        }
                    }
                }
            }
            j0 = -1;
            for (int j = 0; j < N; j++) {
                if (tiles[i][j] == 0) {   // [0 2 0 4] -> [2 4 0 0]
                    if (j0 == -1) {       // [0 0 0 2]     [0 0 2 2]
                        j0 = j;
                    }
                } else if (j0 != -1) {
                    tiles[i][j0] = tiles[i][j];
                    tiles[i][j] = 0;
                    j0 += 1;
                }
            }
        }
    }

    private boolean moveRight() {
        boolean result = false;
        for (int i = 0; i < N; i++) {
            boolean wasShift;
            do {
                wasShift = false;
                for (int j = N - 1; j > 0; j--) {
                    if (tiles[i][j - 1] != 0 && tiles[i][j] == 0) {
                        tiles[i][j] = tiles[i][j - 1];
                        tiles[i][j - 1] = 0;
                        wasShift = true;
                        result = true;
                    }

                }
            } while (wasShift);

            for (int j = N - 1; j > 0; j--) {
                if (tiles[i][j - 1] == tiles[i][j] && tiles[i][j] != 0) {
                    tiles[i][j] *= 2;
                    score += tiles[i][j];
                    tvTiles[i][j].setTag(collapseAnimation);
                    tiles[i][j - 1] = 0;
                    for (int k = j - 1; k > 0; k--) {
                        tiles[i][k] = tiles[i][k - 1];
                    }
                    tiles[i][0] = 0;
                    result = true;
                }
            }

        }

        return result;
    }

    /*

     bpl method -> for copying array

     * * * 4  03
     * 2 * 2  13
     * * * 2  23
     * * * 4  33

    * */

    private boolean canMoveBottom() {
        for (int j = 0; j < N; j++) {
            for (int i = 1; i < N; i++) {
                if (tiles[i - 1][j] != 0 && (tiles[i][j] == 0 || tiles[i - 1][j] == tiles[i][j])) {
                    return true;
                }
            }
        }
        return false;
    }

    private void moveBottom() {
        for (int j = 0; j < N; j++) {
            for (int i = 1; i < N; i++) {
                if (tiles[i][j] == 0 && tiles[i - 1][j] != 0) {
                    tiles[i][j] = tiles[i - 1][j];
                    tiles[i - 1][j] = 0;
                }
            }

            for (int i = N - 1; i > 0; i--) {
                if (tiles[i][j] == tiles[i - 1][j] && tiles[i][j] != 0) {
                    tiles[i][j] *= 2;
                    tiles[i - 1][j] = 0;
                    score += tiles[i][j];
                    tvTiles[i - 1][j].setTag(collapseAnimation);
                    for (int k = i; k > 0; k--) {
                        if (tiles[k][j] == 0 && tiles[k - 1][j] != 0) {
                            tiles[k][j] = tiles[k - 1][j];
                            tiles[k - 1][j] = 0;
                        }
                    }
                }
            }
        }

    }

    private void moveTop() {
        for (int j = 0; j < N; j++) {
            for (int i = N - 1; i > 0; i--) {
                if (tiles[i][j] != 0 && tiles[i - 1][j] == 0) {
                    tiles[i - 1][j] = tiles[i][j];
                    tiles[i][j] = 0;
                }
            }
            for (int i = 1; i < N; i++) {
                if (tiles[i][j] == tiles[i - 1][j] && tiles[i][j] != 0) {
                    tiles[i - 1][j] *= 2;
                    tiles[i][j] = 0;
                    score += tiles[i][j];
                    tvTiles[i][j].setTag(collapseAnimation);
                    for (int k = i; k < N - 1; k++) {
                        if (tiles[k][j] == 0 && tiles[k + 1][j] != 0) {
                            tiles[k][j] = tiles[k + 1][j];
                            tiles[k + 1][j] = 0;
                        }
                    }
                }
            }
        }
    }

    private boolean spawnTile() {

        List<Coordinates> freeTiles = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (tiles[i][j] == 0) {
                    freeTiles.add(new Coordinates(i, j));
                }

            }
        }

        if (freeTiles.isEmpty()) {
            return false;
        }

        Coordinates randomCoordinate = freeTiles.get(random.nextInt(freeTiles.size()));
        tiles[randomCoordinate.x][randomCoordinate.y] = random.nextInt(10) == 0 ? 4 : 2;
        tvTiles[randomCoordinate.x][randomCoordinate.y].setTag(spawnAnimation);
        return true;
    }

    private void initField() {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                tiles[i][j] = 0;
                tvTiles[i][j] = findViewById(getResources().getIdentifier(
                        "g2048_tile_" + i + j,
                        "id",
                        getPackageName()
                ));
            }
        }
        tiles[0][0] = 0;
        score = 0;
        loadBestScore();
    }

    private void showField() {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                tvTiles[i][j].setText(String.valueOf(tiles[i][j]));


                tvTiles[i][j].getBackground().setColorFilter(getResources().getColor(
                        getResources().getIdentifier(
                                tiles[i][j] <= 2048 ? "g2048_tile_" + tiles[i][j] : "g2048_tile_other",
                                "color",
                                getPackageName()
                        ),
                        getTheme()
                ), PorterDuff.Mode.SRC_ATOP);
                tvTiles[i][j].setTextColor(getResources().getColor(
                        getResources().getIdentifier(
                                tiles[i][j] <= 2048 ? "g2048_text_" + tiles[i][j] : "g2048_text_other",
                                "color",
                                getPackageName()
                        ),
                        getTheme()
                ));
                if (tvTiles[i][j].getTag() instanceof Animation) {
                    tvTiles[i][j].startAnimation((Animation) tvTiles[i][j].getTag());
                    tvTiles[i][j].setTag(null);
                }
            }
        }

        tvScore.setText(getString(R.string.g2048_tv_score, String.valueOf(score)));
        if (score > bestScore) {
            bestScore = score;
            if (bestScore > prevBestScore && userGotNewScore) {
                tvBestScore.startAnimation(collapseAnimation);
                userGotNewScore = false;
            }
            saveBestScore();
        }
        tvBestScore.setText(getString(R.string.g2048_tv_best, String.valueOf(bestScore)));

    }

    static class Coordinates {
        int x, y;

        public Coordinates(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}