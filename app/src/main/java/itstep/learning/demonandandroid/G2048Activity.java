package itstep.learning.demonandandroid;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class G2048Activity extends AppCompatActivity {

    private final int N = 4;
    private final int[][] tiles = new int[N][N];
    private final TextView[][] tvTiles = new TextView[N][N];
    private final Random random = new Random();

    private Animation spawnAnimation, collapseAnimation;
    private TextView tvScore, tvBestScore;
    private int score, bestScore, prevBestScore;
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
                if (moveBottom()) {
                    spawnTile();
                    showField();
                } else {
                    Toast.makeText(G2048Activity.this, "No Bottom Move", Toast.LENGTH_SHORT).show();
                } }

            @Override
            public void onSwipeLeft() {
                if (moveLeft()) {
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
                Toast.makeText(G2048Activity.this, "Top", Toast.LENGTH_SHORT).show();
            }
        });
        initField();
        spawnTile();
        showField();
    }

    private boolean moveLeft() {
        boolean result = false;
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
                            result = true;
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
                    result = true;
                }
            }
        }
        return result;
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

    private boolean moveBottom() {
        boolean result = false;

        for (int j = 0; j < N; j++) {
            for (int i = 0; i < N; i++) {
                if(tiles[i][j] == tiles[i][j]){

                }
                tiles[i][j] = 0;
            }
        }


        return result;
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
        bestScore = prevBestScore = 20;
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
        if(score > bestScore){
            bestScore = score;
            if(bestScore > prevBestScore && userGotNewScore){
                tvBestScore.startAnimation(collapseAnimation);
                userGotNewScore = false;
            }
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