package step.learning.androidfirstproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import com.squareup.picasso.Picasso;
import android.widget.ImageView;
public class ren_car_Activity extends AppCompatActivity {

    private final String getCarsUrl = "https://chat.momentfor.fun/";
    // export const CarController="https://serverccar.azurewebsites.net/api/Car/get-cars";
    //export const CarController="https://localhost:7777/api/Car/get-cars";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ren_car);

        ImageView ivCarPhoto = findViewById(R.id.myImageView);
        String photoUrl = "https://rentcarua.space/Car/653560ad22831.jpg";
        Picasso.get().load(photoUrl).into(ivCarPhoto);
    }
}