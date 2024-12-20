package itstep.learning.demonandandroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        findViewById(R.id.main_btn_calc).setOnClickListener(this::onCalcButtonClick);
        findViewById(R.id.main_btn_game2048).setOnClickListener(this::onG2048ButtonClick);
        findViewById(R.id.main_btn_anim).setOnClickListener(this::onAnimButtonClick);
        findViewById(R.id.main_btn_chat).setOnClickListener(this::onChatButtonClick);
    }

    private void onCalcButtonClick(View view){
        Intent intent = new Intent(MainActivity.this, CalcActivity.class);
        startActivity(intent);
    }

    private void onAnimButtonClick(View view){
        Intent intent = new Intent(MainActivity.this, AnimActivity.class);
        startActivity(intent);
    }

    private void onG2048ButtonClick(View view){
        Intent intent = new Intent(MainActivity.this, G2048Activity.class);
        startActivity(intent);
    }
    private void onChatButtonClick(View view){
        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
        startActivity(intent);
    }



}