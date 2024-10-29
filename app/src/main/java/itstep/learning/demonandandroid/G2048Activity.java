package itstep.learning.demonandandroid;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class G2048Activity extends AppCompatActivity {

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
        findViewById(R.id.g2048_ll_field).setOnTouchListener(new OnSwipeListener(G2048Activity.this) {
            @Override
            public void onSwipeBottom() {
                Toast.makeText(G2048Activity.this, "Bottom", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSwipeLeft() {
                Toast.makeText(G2048Activity.this, "Left", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSwipeRight() {
                Toast.makeText(G2048Activity.this, "Right", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSwipeTop() {
                Toast.makeText(G2048Activity.this, "Top", Toast.LENGTH_SHORT).show();
            }
        });
    }
}