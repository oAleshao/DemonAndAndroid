package itstep.learning.demonandandroid;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.DecimalFormat;

public class CalcActivity extends AppCompatActivity {

    private TextView tvResult;
    private TextView tvHistory;
    private static final int maxDigits = 10;
    private String zeroSign;
    private String lastOperation;
    private double prevNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calc);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvResult = findViewById(R.id.calc_tv_result);
        tvHistory = findViewById(R.id.calc_tv_history);
        zeroSign = getString(R.string.calc_btn_digit_0);
        BtnClick_C(null);

        findViewById(R.id.calc_btn_c).setOnClickListener(this::BtnClick_C);
        findViewById(R.id.calc_btn_backspace).setOnClickListener(this::btnClickBackspace);
        for (int i = 0; i < 10; i++) {
            String btnIdName = "calc_btn_digit_" + i;
            @SuppressLint("DiscouragedApi") int btnId = getResources().getIdentifier(btnIdName, "id", getPackageName());
            findViewById(btnId).setOnClickListener(this::btnClickDigit);
        }

        findViewById(R.id.calc_btn_add).setOnClickListener(this::btnOperation);
        findViewById(R.id.calc_btn_minus).setOnClickListener(this::btnOperation);
        findViewById(R.id.calc_btn_mult).setOnClickListener(this::btnOperation);
        findViewById(R.id.calc_btn_divide).setOnClickListener(this::btnOperation);
        findViewById(R.id.calc_btn_inverse).setOnClickListener(this::btnOperation);
        findViewById(R.id.calc_btn_square).setOnClickListener(this::btnOperation);
        findViewById(R.id.calc_btn_sqrt).setOnClickListener(this::btnOperation);

    }

    // region OnChangeQualifier

    // When orientation changes
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence("tvResult", tvResult.getText());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        tvResult.setText(savedInstanceState.getCharSequence("tvResult"));
    }

    // endregion

    private double makeOperation() {
        switch (lastOperation) {
            case "+": {
                return prevNumber + Double.parseDouble(tvResult.getText().toString());
            }
            case "-": {
                return prevNumber - Double.parseDouble(tvResult.getText().toString());
            }
            default:
                return -1;
        }

    }

    private void btnOperation(View view) {
        lastOperation = ((Button) view).getText().toString();
        double tmp = makeOperation();

        prevNumber = Double.parseDouble(tvResult.getText().toString());

    }

    private void btnClickBackspace(View view) {
        String resText = tvResult.getText().toString();
        resText = resText.substring(0, resText.length() - 1);
        if (resText.isEmpty()) {
            tvResult.setText(zeroSign);
        } else {
            tvResult.setText(resText);
        }
    }

    private void btnClickDigit(View view) {
        String resText = tvResult.getText().toString();
        if (resText.length() >= maxDigits) {
            Toast.makeText(this, R.string.calc_msg_too_long, Toast.LENGTH_SHORT).show();
            return;
        }

        if (resText.equals(zeroSign)) {
            resText = "";
        }
        resText += ((Button) view).getText();
        tvResult.setText(resText);
    }

    private void BtnClick_C(View view) {
        tvResult.setText(zeroSign);
        tvHistory.setText("");
    }
}