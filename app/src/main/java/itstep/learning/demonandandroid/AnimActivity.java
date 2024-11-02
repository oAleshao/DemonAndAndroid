package itstep.learning.demonandandroid;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AnimActivity extends AppCompatActivity {

    private Animation alpaDemo;
    private Animation scaleDemo;

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

        findViewById(R.id.anim_alpha).setOnClickListener(this::onAlfaClick);
        findViewById(R.id.anim_scale).setOnClickListener(this::onScaleClick);
    }

    private void onAlfaClick(View view){
        view.startAnimation(alpaDemo);
    }

    private void onScaleClick(View view){
        view.startAnimation(scaleDemo);
    }
}