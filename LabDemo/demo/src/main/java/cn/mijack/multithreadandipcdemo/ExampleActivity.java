package cn.mijack.multithreadandipcdemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Random;

public class ExampleActivity extends Activity {

    private static final int SHOW_RESULT = 1;
    private EditText editTextA;
    private EditText editTextB;
    private Button btnGetAnswer;
    private TextView result;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == SHOW_RESULT) {
                StringBuilder sb = new StringBuilder();
                sb.append("生成的方法为：").append(msg.obj).append("\n");
                double x1 = msg.arg1 - Math.sqrt(msg.arg2);
                double x2 = msg.arg1 + Math.sqrt(msg.arg2);
                sb.append("X1=").append(x1).append("\t\tX2=").append(x2);
                result.setText(sb.toString());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example);
        editTextA = (EditText) findViewById(R.id.editTextA);
        editTextB = (EditText) findViewById(R.id.editTextB);
        btnGetAnswer = (Button) findViewById(R.id.btnGetAnswer);
        result = (TextView) findViewById(R.id.result);
        btnGetAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        int a = getInteger(editTextA);
                        int b = getInteger(editTextB);
                        if (a * a < b) {
                            a = 2 * a;
                        }
                        Message message = Message.obtain();
                        message.what = SHOW_RESULT;
                        //x^2+2*a*x+b=0
                        message.obj = String.format("x^2+2*%s*x+%s=0", a, b);
                        message.arg1 = -a;
                        message.arg2 = a * a - b;
                        handler.sendMessage(message);

                    }
                }.start();
            }
        });
    }

    private Integer getInteger(EditText editText) {
        return Integer.valueOf(editText.getText().toString());
    }

    private int getDivisor(int a, int b, int c) {
        return getDivisor(a, getDivisor(b, c));
    }

    private int getDivisor(int a, int b) {
        if (a * b == 0) {
            return 1;
        }
        int m = Math.max(a, b);
        int n = Math.min(a, b);
        while (m != n) {
            int temp = m - n;
            n = Math.min(temp, m);
            m = Math.max(temp, m);
        }
        return m;
    }

    public static final int[] ARRAY = new int[]{-4, -1, 1, 0, 4, 9};

    public int getC() {
        Random random = new Random();
        //20%的概率崩溃
        int nextInt = random.nextInt(12);
        return ARRAY[random.nextInt() % ARRAY.length];
    }
}
