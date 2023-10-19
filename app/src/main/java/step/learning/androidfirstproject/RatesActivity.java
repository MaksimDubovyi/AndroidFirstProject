package step.learning.androidfirstproject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

import step.learning.androidfirstproject.orm.NbuRate;
import step.learning.androidfirstproject.orm.NbuRateResponse;

public class RatesActivity extends AppCompatActivity {

    private final String nbuRatesUrl="https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json";
    private TextView tvJson;
    private NbuRateResponse nbuRateResponse;
    private Button btnRate;

    TextView arr_tv ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rates);

        tvJson=findViewById(R.id.rates_tv_json);

        new Thread(this::loadUtlData).start();

    }
    private void loadUtlData()
    {
        try(InputStream stream=new URL (nbuRatesUrl).openStream()) {

            ByteArrayOutputStream builder= new ByteArrayOutputStream();
            byte[]buffer= new byte[1024*16];
            int receivedLength;
            while ((receivedLength=stream.read(buffer))>0)
            {
                builder.write(buffer,0,receivedLength);
            }

            nbuRateResponse=new NbuRateResponse(new JSONArray(builder.toString()));
            runOnUiThread(this::showResponse);


        }  catch (IOException | JSONException e) {
            Log.e("loadUtlData",e.getMessage());
        }
        catch( NetworkOnMainThreadException ignored ) {
            Log.e("loadUtlData",getString( R.string.rates_ex_thread ));

        }
    }

    private void showResponse()
    {
        LinearLayout container =findViewById(R.id.rates_container);

        for(NbuRate nbuRate:nbuRateResponse.getRates())
        {

            // Створення нового контейнера для кожного Button
            LinearLayout btcCantainer = new LinearLayout(this);
            btcCantainer.setOrientation(LinearLayout.HORIZONTAL); // Встановлення орієнтації контейнера
            btcCantainer.setTag(nbuRate.getCc());
            btcCantainer.setBackgroundResource(R.drawable.calc_btn_choice);
//Кнопка
            Button rateButton = new Button(this);
            rateButton.setText(String.format(  Locale.UK,  "%s",nbuRate.getCc()));
            rateButton.setPadding(10,5,10,5);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    150, LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(10,5,10,5);

            rateButton.setLayoutParams(params);
            rateButton.setBackgroundResource(R.drawable.rate_btn_default);
            rateButton.setTextColor(ContextCompat.getColor(this,  R.color.white));
            rateButton.setTextSize(20);


            rateButton.setOnClickListener(this::rateClick);
            rateButton.setTag(nbuRate);
//---

            btcCantainer.addView(rateButton);
            container.addView(btcCantainer);


        }

    }
    private void  rateClick(View view)
    {

        Clear();
        if (btnRate != null) {
            btnRate.setBackgroundResource(R.drawable.rate_btn_default);
        }
        view.setBackgroundResource(R.drawable.rate_btn_choice);
        LinearLayout container =findViewById(R.id.rates_container);
        NbuRate nbuRate = (NbuRate) view.getTag();
        LinearLayout viewWithTag = container.findViewWithTag(nbuRate.getCc());

        TextView tv = new TextView(this);
        tv.setText(String.format(
        Locale.UK,"%s (%s) %f грн\n курс на  \" %s \" ",
        nbuRate.getCc(),  nbuRate.getTxt(), nbuRate.getRate(), nbuRate.getExchangedate()));

        tv.setPadding(10,5,10,5);
        tv.setTextColor(ContextCompat.getColor(this,  R.color.white));
        tv.setTextSize(14);

        tv.setTag(nbuRate.getCc());
        btnRate=(Button) view;;
        arr_tv=tv;
        viewWithTag.addView(tv);

    }

        private void Clear()
        {
           if(arr_tv!=null) {
               arr_tv.setText("");
           }
        }

//    private void showResponse()
//    {
//        LinearLayout container =findViewById(R.id.rates_container);
//        for(NbuRate nbuRate:nbuRateResponse.getRates())
//        {TextView tv = new TextView(this);
//        tv.setText(String.format(
//        Locale.UK,
//        "%s (%s) %f грн\n",
//        nbuRate.getCc(),
//        nbuRate.getTxt(),
//        nbuRate.getRate()));
//        tv.setPadding(10,5,10,5);
//        tv.setOnClickListener(this::rateClick);
//        tv.setTag(nbuRate);
//
//        container.addView(tv);
//        }
//
//    }
//    private void  rateClick(View view)
//    {
//        NbuRate nbuRate = (NbuRate) view.getTag();
//        showRateDialog(nbuRate);
//    }
    private void showRateDialog(NbuRate nbuRate) {

        new AlertDialog.Builder( this, androidx.appcompat.R.style.Base_Theme_AppCompat_Dialog_MinWidth)
                .setIcon(android.R.drawable.ic_popup_sync)
                .setTitle( nbuRate.getCc())
                .setMessage(nbuRate.getTxt()+"\n"+"1 "+nbuRate.getCc()+" = " +Double.toString( nbuRate.getRate())+" грн")
                .show();
    }
    private void showResponseTxt()
    {
        StringBuilder sb =new StringBuilder();
      for(NbuRate nbuRate:nbuRateResponse.getRates())
      {
          sb.append(String.format(
                  Locale.UK,
                  "%s (%s) %f грн\n",
                  nbuRate.getCc(),
                  nbuRate.getTxt(),
                  nbuRate.getRate()));
      }
      tvJson.setText(sb.toString());
    }

}

/*
Робота з Інтернет. АРІ.
На прикладі курсу валют.
1. Основу роботи з Інтернет становить об'єкт класу URL (схожий за
змістом з File). Створення об'єкту не спричиняє роботу з мережею,
лише з'являється сам об'єкт.

2. Мережна активність запускається спробою "відкриття" URL, зокрема,
openConnection() або openStream()
при цьому можна одержати NetworkOnMainThreadException, що свідчить
про спробу відкриття з'єднання з UI (основного) потоку. Це виключення
може не потрапити у catch, спостерігаємо у логері.

3. Підключення до інтернету вимагає включеного WIFI (на емуляторі), а
також дозволу, відсутність якого призводить до
SecurityException: Permission denied (missing INTERNET permission?)
Даний дозвіл має бути зазначений в маніфесті
   <uses-permission android:name="android.permission.INTERNET"/>
   Інколи зміна дозволів вимагає перевстановлення застосунку

4.  Винесення роботи з мережою до окремого потоку призводить до
 зворотної проблеми повязаною з виключенням
CalledFromWrongThreadException: Only the original thread that created a view hierarchy can touch its views.
яка забороняє змінювати представлення (View) з іншого потоку.
Звертатись слід через делегування
runOnUiThread(()->...);

-------------------------------------------------------
5. ORM. Після одержання сирих даних їх слід відобразити (map) на обєкти
та колукції платформи (мови програмування). Для цього створюють класи
що відповідають структурі даних та їх конструктори (фабрики),
які приймають JSON
 */