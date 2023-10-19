package step.learning.androidfirstproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class CalkActivity extends AppCompatActivity {

    private final int MAX_DIGITS=10;
    private TextView tvResult;
    private  TextView tvExpression;
    private  String operator1;
    private  String operator2;
    private String operand1;
    private String operand2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calk);

        tvResult =findViewById(R.id.calc_tv_result);
        tvResult.setText(R.string.calc_btn_0);

        tvExpression =findViewById(R.id.calc_tv_expression);
        tvExpression.setText("");


        findViewById(R.id.calc_btn_inverse).setOnClickListener(this::inverseClick);

        for(int i=0;i<MAX_DIGITS;i++)
        {
            findViewById( getResources().getIdentifier("calc_btn_"+i,"id",getPackageName()
            )).setOnClickListener(this::digitClick);
        }

        findViewById(R.id.calc_btn_c).setOnClickListener(this::clearClick);
        findViewById(R.id.calc_btn_ce).setOnClickListener(this::clearClickAll);
        findViewById(R.id.calc_btn_plus_minus).setOnClickListener(this::plusMinusClick);
        findViewById(R.id.calc_btn_square).setOnClickListener(this::Sqr);
        findViewById(R.id.calc_btn_equal).setOnClickListener(this::equalClick);
        findViewById(R.id.calc_btn_backspace).setOnClickListener(this::backspaceClick);
        findViewById(R.id.calc_btn_comma).setOnClickListener(this::DecimalPointClick);



        findViewById(R.id.calc_btn_minus).setOnClickListener(this::choiceOperator);
        findViewById(R.id.calc_btn_plus).setOnClickListener(this::choiceOperator);
        findViewById(R.id.calc_btn_divide).setOnClickListener(this::choiceOperator);
        findViewById(R.id.calc_btn_multiply).setOnClickListener(this::choiceOperator);
    }



    /*
          При зміні конфігурації відбувається перестворення активності, через що
          втрачаються дані, введені на UI. З метою збереження цих даних задаються
          обробники відповідних подій onRestoreInstanceState та onSaveInstanceState
           */
    @Override
    public void onRestoreInstanceState(@Nullable Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        tvResult.setText(savedInstanceState.getCharSequence("result"));
        tvExpression.setText(savedInstanceState.getCharSequence("expression"));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence("result",tvResult.getText());
        outState.putCharSequence("expression",tvExpression.getText());
    }

    private void choiceOperator(View view)
    {
        equal();
        String str1 = tvResult.getText().toString();
        double arg =getResult(str1);
        if(arg!=0)
        {
           colorDefault();
           choiceOper(view);
        }
    }
    private void choiceOper(View view)
    {
        String str =((Button)view).getText().toString();
        if(str.equals(getString(R.string.calc_btn_minus)))
        {
            operator1="-";
            view.setBackgroundResource(R.drawable.calc_btn_choice);
        }
        else if(str.equals(getString(R.string.calc_btn_plus)))
        {
            operator1="+";
            view.setBackgroundResource(R.drawable.calc_btn_choice);
        }
        else if(str.equals(getString(R.string.calc_btn_divide)))
        {
            operator1="/";
            view.setBackgroundResource(R.drawable.calc_btn_choice);
        } else if(str.equals(getString(R.string.calc_btn_multiply)))
        {
            operator1="*";
            view.setBackgroundResource(R.drawable.calc_btn_choice);
        }
    }
    private void equal()
    {
        if(operand2!=null)
        {
            double numb =getResult(operand1);
            double numb2 =getResult(operand2);
            if(operator2.equals("-"))
            {
                double result= numb-numb2;
                tvExpression.setText(tvResult.getText());
                intOrDouble(result);
            }
            else if(operator2.equals("+"))
            {
                double result= numb+numb2;
                tvExpression.setText(tvResult.getText());
                intOrDouble(result);
            }
            else if(operator2.equals("/"))
            {
                double result= numb/numb2;
                tvExpression.setText(tvResult.getText());
                intOrDouble(result);
            }
            else if(operator2.equals("*"))
            {
                double result= numb*numb2;
                tvExpression.setText(tvResult.getText());
                intOrDouble(result);
            }

        }
    }
    private void equalClick(View view)
    {
        equal();
        operatorNull();
        colorDefault();
        operand2=null;
    }
    private void  inverseClick(View view)
    {
        String str = tvResult.getText().toString();
        double arg =getResult(str);
        if(arg==0)
        {
        tvResult.setText(R.string.calc_div_zero_message);
        }
        else {
            str="1/"+str+" =";
            tvExpression.setText(str);
            showResult(1/arg);
        }
    }

    private void Sqr(View view)
    {
        String str =tvResult.getText().toString();
        Double res =  Double.parseDouble(str);
        tvExpression.setText("sqr( "+ res +" )");
        res*=res;
        intOrDouble( res);

    }

    private  void DecimalPointClick(View view )
    {

        String str =tvResult.getText().toString();
        if(str.equals(getString(R.string.calc_btn_0)))
        {
            str="0";
        }
        if(operator2==null)
        {
            boolean containsDecimalPoint = str.contains(".");
        if (containsDecimalPoint==false)
            str += ".";
        }
        else
        {
            if(operand2!=null)
            {
                boolean containsDecimalPoint = operand2.contains(".");
                if (containsDecimalPoint==false) {
                    operand2 += ".";
                    str+=".";

                }
            }

        }
        tvResult.setText(str);

    }
    private void  backspaceClick(View view)
    {
        String str = tvResult.getText().toString();
        if (str.length() > 0) {
            str = str.substring(0, str.length() - 1);
            tvResult.setText(str);
        }

        if (operand2!=null)
            operand2 = operand2.substring(0, operand2.length() - 1);

    }
    private void plusMinusClick(View view)
    {
        String str =tvResult.getText().toString();
        Double res =  Double.parseDouble(str);
        res-=res*2;
        intOrDouble( res);
        tvExpression.setText("negate( "+ tvResult.getText().toString() +" )");

    }
    private void  digitClick(View view)
    {

        String str =tvResult.getText().toString();
        if(str.equals(getString(R.string.calc_btn_0)))
        {
            str="";
        }
        if(operator1==null) {
            str += ((Button) view).getText();

            if(operand2!=null) {
                operand2 += ((Button) view).getText();;
            }
        }
        else
        {
            operator2=operator1;
            String x = ((Button) view).getText().toString();
            if(operand2==null)
            operand2 =x;

            operand1=str;
            str+=operator1+operand2;
            operator1=null;
        }
        tvResult.setText(str);
    }

    private void  clearClick(View view)
    {

        tvResult.setText(R.string.calc_btn_0);
        operatorNull();
        operand1=null;
        operand2=null;
        colorDefault();
    }

    private void  clearClickAll(View view)
    {
        tvExpression.setText("");
        tvResult.setText(R.string.calc_btn_0);
        operatorNull();
        operand1=null;
        operand2=null;
        colorDefault();
    }
    private  double getResult(String str)
    {
        str= str.replaceAll(getString(R.string.calc_btn_0),"0");
        str= str.replaceAll(getString(R.string.calc_btn_comma),".");
        return  Double.parseDouble(str);
    }

    private  void showResult(double res)
    {
        String str = String.valueOf(res);
        if(str.length()>MAX_DIGITS)
        {
            str=str.substring(0,MAX_DIGITS);
        }
        str= str.replaceAll("0",getString(R.string.calc_btn_0));
        tvResult.setText(str);
    }

 private  void intOrDouble( double  d)
 {
     int i=(int)d;
     if(i==d)
         tvResult.setText(String.valueOf(i));
     else
         tvResult.setText(String.valueOf(d));
 }
    private void operatorNull()
    {
        operator1=null;
        operator2=null;
    }

    private void colorDefault()
    {
        findViewById(R.id.calc_btn_minus).setBackgroundResource(R.drawable.calc_btn_func);
        findViewById(R.id.calc_btn_plus).setBackgroundResource(R.drawable.calc_btn_func);
        findViewById(R.id.calc_btn_divide).setBackgroundResource(R.drawable.calc_btn_func);
        findViewById(R.id.calc_btn_multiply).setBackgroundResource(R.drawable.calc_btn_func);
    }
}