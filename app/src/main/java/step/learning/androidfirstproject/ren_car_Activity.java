package step.learning.androidfirstproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import com.squareup.picasso.Picasso;

import android.os.NetworkOnMainThreadException;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import step.learning.androidfirstproject.orm.NbuRate;
import step.learning.androidfirstproject.orm.RentCar;
import step.learning.androidfirstproject.orm.RentCarRateResponse;

public class ren_car_Activity extends AppCompatActivity {

    private RentCarRateResponse rentCarRateResponse;
    private final String showCarsUrl = "https://rentcarua.space/Car/";
    private final String CarControllerServ="https://serverccar.azurewebsites.net/api/Car/get-cars";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ren_car);
;

        new Thread(this::loadUtlData).start();
    }

    private void loadUtlData()
    {
        try(InputStream stream=new URL(CarControllerServ).openStream()) {

            ByteArrayOutputStream builder= new ByteArrayOutputStream();
            byte[]buffer= new byte[1024*16];
            int receivedLength;
            while ((receivedLength=stream.read(buffer))>0)
            {
                builder.write(buffer,0,receivedLength);
            }
            rentCarRateResponse =new RentCarRateResponse(new JSONArray(builder.toString()));

            runOnUiThread(this::showResponse);


        }  catch (IOException | JSONException e) {
            //Toast.makeText(this, "IOException "+e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("loadUtlData",e.getMessage());
        }
        catch( NetworkOnMainThreadException ignored ) {
            Log.e("loadUtlData",getString( R.string.rates_ex_thread ));

        }
    }

    private void showResponse() {
        LinearLayout container = findViewById(R.id.car_container);

        LinearLayout.LayoutParams lineParamsBlock = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        lineParamsBlock.setMargins(10, 10, 10, 10);


        LinearLayout.LayoutParams lineParamsContent = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        lineParamsContent.setMargins(10, 5, 10, 5);

        LinearLayout.LayoutParams lineParamsImg = new LinearLayout.LayoutParams(
                292, 212
        );




        for (RentCar rentCar : rentCarRateResponse.getRates()) {

            LinearLayout lineBlock = new LinearLayout(this);
            lineBlock.setOrientation(LinearLayout.HORIZONTAL);
            lineBlock.setBackgroundResource(R.drawable.rent_car_ing);
            lineBlock.setLayoutParams(lineParamsBlock);
            lineBlock.setPadding(30,10,10,10);

            LinearLayout lineContent = new LinearLayout(this);
            lineContent.setOrientation(LinearLayout.VERTICAL);
            lineContent.setBackgroundResource(R.drawable.rent_car_ing);
            lineContent.setLayoutParams(lineParamsContent);
            lineContent.setPadding(20,10,10,10);

            LinearLayout lineImg = new LinearLayout(this);
            lineImg.setOrientation(LinearLayout.VERTICAL);
            lineImg.setLayoutParams(lineParamsImg);
            lineImg.setBackgroundResource(R.drawable.rent_car_ing2);
            lineImg.setGravity(Gravity.CENTER);


            Typeface typeface = ResourcesCompat.getFont(this, R.font.nosifer);
            Typeface typeface2 = ResourcesCompat.getFont(this, R.font.metal);
            Typeface typeface3 = ResourcesCompat.getFont(this, R.font.ukr);

            TextView tvBrand = new TextView(this);
            tvBrand.setText(rentCar.getBrand());
            tvBrand.setTextColor(ContextCompat.getColor(this, R.color.black));
            tvBrand.setTypeface(typeface);
            tvBrand.setTextSize(10);

            TextView tvModel = new TextView(this);
            tvModel.setText(rentCar.getModel());
            tvModel.setTextColor(ContextCompat.getColor(this, R.color.black));
            tvModel.setTypeface(typeface);
            tvModel.setTextSize(10);

            TextView tvAgePrice = new TextView(this);
            tvAgePrice.setText(rentCar.getAge()+"р. "+rentCar.getPrice()+"грн");
            tvAgePrice.setTextColor(ContextCompat.getColor(this, R.color.black));
            tvAgePrice.setTypeface(typeface3);
            tvAgePrice.setTextSize(10);

            TextView tvEmail = new TextView(this);
            tvEmail.setText(rentCar.getUserEmail());
            tvEmail.setTextColor(ContextCompat.getColor(this, R.color.black));
            tvEmail.setTypeface(typeface2);
            tvEmail.setTextSize(10);

            TextView tvDate = new TextView(this);
            tvDate.setText(rentCar.getDateCreation());
            tvDate.setTextColor(ContextCompat.getColor(this, R.color.black));
            tvDate.setTypeface(typeface3);
            tvDate.setTextSize(10);

            TextView tvFuelEype = new TextView(this);
            tvFuelEype.setText("тип палива: "+rentCar.getFuelType());
            tvFuelEype.setTextColor(ContextCompat.getColor(this, R.color.black));
            tvFuelEype.setTypeface(typeface3);
            tvFuelEype.setTextSize(10);

            TextView tvTransmission = new TextView(this);
            tvTransmission.setText("тип трансмісії: "+rentCar.getTransmissionType());
            tvTransmission.setTextColor(ContextCompat.getColor(this, R.color.black));
            tvTransmission.setTypeface(typeface3);
            tvTransmission.setTextSize(10);

            ImageView img = new ImageView(this);
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);  // Встановити обрізання

            String photoUrl = showCarsUrl + rentCar.getFoto();
            Picasso.get().load(photoUrl).into(img);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(275, 195);
            img.setLayoutParams(layoutParams);



            lineContent.addView(tvBrand);
            lineContent.addView(tvModel);
            lineContent.addView(tvAgePrice);
            lineContent.addView(tvEmail);
            lineContent.addView(tvDate);
            lineContent.addView(tvFuelEype);
            lineContent.addView(tvTransmission);


            if(!rentCar.getBrand().equals("Mazda"))// фото не коректне тому цей елемент не виводжу
            {
                lineImg.addView(img);
                lineBlock.addView(lineImg);
                lineBlock.addView(lineContent);
                container.addView(lineBlock);
            }

        }
    }

}