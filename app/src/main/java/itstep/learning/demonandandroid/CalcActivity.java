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
    private double lastNumber;
    private double currentNumber;
    private double result;
    private String historyText;
    private String lastOperation;
    private boolean userPressEqual = false;


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
        findViewById(R.id.calc_btn_equals).setOnClickListener(this::btnEquals);
        findViewById(R.id.calc_btn_negative).setOnClickListener(this::btnClickChangeSign);
    }

    // region OnChangeQualifier

    // When orientation changes
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence("tvResult", tvResult.getText());
        outState.putCharSequence("tvHistory", tvHistory.getText());
        outState.putString("lastOperation", lastOperation);
        outState.putString("historyText", historyText);
        outState.putDouble("lastNumber", lastNumber);
        outState.putDouble("currentNumber", currentNumber);
        outState.putDouble("result", result);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        tvResult.setText(savedInstanceState.getCharSequence("tvResult"));
        tvHistory.setText(savedInstanceState.getCharSequence("tvHistory"));
        lastOperation = savedInstanceState.getString("lastOperation");
        historyText = savedInstanceState.getString("historyText");
        lastNumber = savedInstanceState.getDouble("lastNumber");
        currentNumber = savedInstanceState.getDouble("currentNumber");
        result = savedInstanceState.getDouble("result");
    }

    // endregion



    private void makeOperation() {
        switch (lastOperation) {
            case "+": {
                result = lastNumber + currentNumber;
                break;
            }
            case "-": {
                result = lastNumber - currentNumber;
                break;
            }
            case "×": {
                result = lastNumber * currentNumber;
                break;
            }
            case "÷": {
                result = lastNumber / currentNumber;
                break;
            }
            case "x²": {
                result = Math.pow(lastNumber, 2);
                historyText = "sqr(" + historyText + ")";
                break;
            }
            case "²√ⅹ": {
                result = Math.sqrt(lastNumber);
                historyText = "√(" + historyText + ")";
                break;
            }
            case "¹∕ₓ": {
                result = 1. / lastNumber;
                historyText = "1/(" + historyText + ")";
                break;
            }
            default:
                result = 0;
                lastOperation = "";
        }
    }

    private void setHistoryText() {
        tvHistory.setText(historyText);
    }

    private String getNumberToText(double tmpNumber) {
        String resText = tmpNumber + "";
        if (tmpNumber == Math.rint(tmpNumber) && !Double.isInfinite(tmpNumber)) {
            resText = String.format("%.0f", tmpNumber);
        }
        return resText;
    }

    private double convertToNumber() {
        currentNumber = Double.parseDouble(tvResult.getText().toString());
        return currentNumber;
    }

    private void btnEquals(View view) {
        makeOperation();
        historyText = String.format("%s %s %s =", getNumberToText(lastNumber), lastOperation, getNumberToText(currentNumber));
        lastNumber = result;
        setHistoryText();
        tvResult.setText(getNumberToText(result));
        lastOperation = null;
        userPressEqual = true;
    }

    private void btnClickChangeSign(View view){
        convertToNumber();
        currentNumber = currentNumber == 0? currentNumber : currentNumber * -1;
        if(userPressEqual){
            lastNumber = currentNumber;
        }
        tvResult.setText(getNumberToText(currentNumber));
    }
    private void btnOperation(View view) {
        userPressEqual = false;
        String clickedOperation = ((Button) view).getText().toString();
        if (clickedOperation.equals("x\u00B2") || clickedOperation.equals("\u00B2\u221A\u2179") || clickedOperation.equals("\u00B9\u2215\u2093")) {
            lastOperation = clickedOperation;
            convertToNumber();
            lastNumber = currentNumber;
            if(historyText == null || historyText.isEmpty())
                historyText = getNumberToText(lastNumber);
            makeOperation();

            tvResult.setText(getNumberToText(result));
            setHistoryText();
            return;
        }


        if (lastOperation != null && !lastOperation.isEmpty()) {
            makeOperation();
            lastNumber = result;
            tvResult.setText(getNumberToText(lastNumber));
        } else {
            if(lastNumber == 0)
                lastNumber = currentNumber;

        }


        currentNumber = 0;
        lastOperation = clickedOperation;
        historyText = String.format("%s %s", getNumberToText(lastNumber), lastOperation);
        setHistoryText();
    }

    private void btnClickBackspace(View view) {
        if(userPressEqual){
            BtnClick_C(null);
            userPressEqual = false;
            return;
        }

        String resText = tvResult.getText().toString();
        resText = resText.substring(0, resText.length() - 1);
        if (resText.isEmpty()) {
            tvResult.setText(zeroSign);
        } else {
            tvResult.setText(resText);
            //  historyText = resText;
            convertToNumber();
        }
    }

    private void btnClickDigit(View view) {
        if(userPressEqual && lastOperation == null){
            BtnClick_C(null);
        }

        String resText = tvResult.getText().toString();
        if (resText.length() >= maxDigits) {
            return;
        }

        if (resText.equals(zeroSign)) {
            resText = "";
        }
        if (currentNumber == 0) {
            resText = ((Button) view).getText().toString();
        } else {
            resText += ((Button) view).getText();
        }
        tvResult.setText(resText);
        convertToNumber();
    }

    private void BtnClick_C(View view) {
        tvResult.setText(zeroSign);
        tvHistory.setText("");
        historyText = null;
        lastOperation = null;
        userPressEqual = false;
        lastNumber = currentNumber = result = 0;
    }
}

/*

 -------- 25
 25 -> last number
 +  -> last operation
 historyText -> 25 +
 4  -> current number


 if I click +
 +  ----> 25 + 4 = 29 -> historyText = 29 +
 else I click =
 25 + 4 = 29 -> historyText = 25 + 4 =

32 -> last number
65 -> current number
+  --> makeOperation();
result = 97;
lastNumber = 97;
'97 +' -> historyText

* */

