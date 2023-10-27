package step.learning.androidfirstproject.orm;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class RentCarRateResponse {

    private List<RentCar> rates;

    public RentCarRateResponse(JSONArray jsonArray) throws JSONException {
        rates=new ArrayList<>();
        int length= jsonArray.length();
        for (int i=0;i<length;i++)
        {
            rates.add(new RentCar(jsonArray.getJSONObject(i)));
        }
    }

    public List<RentCar> getRates() {
        return rates;
    }
}
