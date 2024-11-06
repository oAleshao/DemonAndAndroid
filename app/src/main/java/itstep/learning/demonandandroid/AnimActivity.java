package itstep.learning.demonandandroid;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AnimActivity extends AppCompatActivity {

    private Animation alpaDemo;
    private Animation scaleDemo;
    private Animation rotateDemo;
    private Animation translateDemo;
    private Animation bellDemo;
    private AnimationSet comboSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_anim);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        alpaDemo = AnimationUtils.loadAnimation(this, R.anim.alpa_demo);
        scaleDemo = AnimationUtils.loadAnimation(this, R.anim.scale_demo);
        rotateDemo = AnimationUtils.loadAnimation(this, R.anim.rotate_demo);
        translateDemo = AnimationUtils.loadAnimation(this, R.anim.translate_demo);
        bellDemo = AnimationUtils.loadAnimation(this, R.anim.bell_demo);

        comboSet = new AnimationSet(true);
        comboSet.addAnimation(alpaDemo);
        comboSet.addAnimation(scaleDemo);
        comboSet.addAnimation(rotateDemo);
        comboSet.addAnimation(translateDemo);

        findViewById(R.id.anim_alpha).setOnClickListener(this::onAlfaClick);
        findViewById(R.id.anim_scale).setOnClickListener(this::onScaleClick);
        findViewById(R.id.anim_rotate).setOnClickListener(this::onRotateClick);
        findViewById(R.id.anim_translate).setOnClickListener(this::onTranslateClick);
        findViewById(R.id.anim_combo).setOnClickListener(this::onComboClick);
        findViewById(R.id.anim_bell).setOnClickListener(this::onBellClick);

    }

    private void onAlfaClick(View view) {
        view.startAnimation(alpaDemo);
    }

    private void onScaleClick(View view) {
        view.startAnimation(scaleDemo);
    }

    private void onRotateClick(View view) {
        view.startAnimation(rotateDemo);
    }

    private void onTranslateClick(View view) {
        view.startAnimation(translateDemo);
    }

    private void onComboClick(View view) {
        view.startAnimation(comboSet);
    }

    private void onBellClick(View view) {
        view.startAnimation(bellDemo);
    }

}