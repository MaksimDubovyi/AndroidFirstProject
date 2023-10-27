package step.learning.androidfirstproject.orm;

import android.view.View;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatMessage {
    public String id;
    public String author;
    public String text;
    public String moment;

    transient   private View view; //посилання на представлення даного повідомлення (transient - це для того щоб яко потрібно сеарелізувати то помітили це поле що його не потрібно )
    private final static SimpleDateFormat sqlDateFormat =
            new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.UK);
    public Date getDate()
    {
        try
        {
            return sqlDateFormat.parse(getMoment());
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    public static ChatMessage fromJson(JSONObject jsonObject)
    {
        ChatMessage chatMessage =new ChatMessage();
        try {
            for (Field field : ChatMessage.class.getDeclaredFields()) {
                if (jsonObject.has(field.getName())) {
                    field.set(
                            chatMessage,
                            jsonObject.getString(field.getName())
                    );

                }
            }
        }
        catch (Exception ignored){}

       return chatMessage;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getMoment() {
        return moment;
    }

    public void setMoment(String moment) {
        this.moment = moment;
    }



}
