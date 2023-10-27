package step.learning.androidfirstproject.orm;

import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class RentCar {

    public RentCar(JSONObject jsonObject) throws JSONException {
        double price = jsonObject.getDouble("price");
        double engineCapacity = jsonObject.getDouble("engineCapacity");

        String idString = jsonObject.getString("id");
        UUID id = UUID.fromString(idString);

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", new Locale("uk", "UA"));
            Date dateCreation = dateFormat.parse(jsonObject.getString("dateСreation"));

            SimpleDateFormat outputDateFormat = new SimpleDateFormat("HH:mm:ss EEEE dd MM yyyy", new Locale("uk", "UA"));
            String formattedDate = outputDateFormat.format(dateCreation);


            setDateCreation(formattedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

          setId(id);
          setUserEmail( jsonObject.getString("userEmail"));
          setFoto( jsonObject.getString("foto"));
          setPrice((float) price);
          setBrand( jsonObject.getString("brand"));
          setModel( jsonObject.getString("model"));
          setDoor( jsonObject.getInt("door"));
           setAge( jsonObject.getInt("age"));
        setClimate( jsonObject.getBoolean("climate"));
          setColor( jsonObject.getString("color"));
          setNumberOfSeats( jsonObject.getInt("numberOfSeats"));
          setEngineCapacity( (float) engineCapacity);
          setTransmissionType( jsonObject.getString("transmissionType"));
          setFuelType( jsonObject.getString("fuelEype"));
          setStatus(StatusCar.values()[jsonObject.getInt("status")]);

    }
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Boolean getClimate() {
        return climate;
    }

    public void setClimate(Boolean climate) {
        this.climate = climate;
    }

    public Integer getDoor() {
        return door;
    }

    public void setDoor(Integer door) {
        this.door = door;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getNumberOfSeats() {
        return numberOfSeats;
    }

    public void setNumberOfSeats(int numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
    }

    public float getEngineCapacity() {
        return engineCapacity;
    }

    public void setEngineCapacity(float engineCapacity) {
        this.engineCapacity = engineCapacity;
    }

    public String getTransmissionType() {
        return transmissionType;
    }

    public void setTransmissionType(String transmissionType) {
        this.transmissionType = transmissionType;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public StatusCar getStatus() {
        return status;
    }

    public void setStatus(StatusCar status) {
        this.status = status;
    }

    public String getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(String dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Date getDeleteDt() {
        return deleteDt;
    }

    public void setDeleteDt(Date deleteDt) {
        this.deleteDt = deleteDt;
    }
    private UUID id;
    private String userEmail;
    private String foto;
    private Float price;
    private String brand;

    private String model;
    private Integer age;
    private Boolean climate;
    private Integer door;
    private String color;
    private int numberOfSeats;
    private float engineCapacity;
    private String transmissionType;
    private String fuelType;
    private StatusCar status;
    private String dateCreation;
    private Date deleteDt;

    public enum StatusCar {
        FREE,                     // Вільний
        BUSY,                     // Зданий
        AWAITING_CONFIRMATION,    // Очікує підтвердження знаходиться на карті (готовий до здачі в оренду)
        ORDER,                    // Замовник очікує підтвердження від Арендодавця
    }
}
