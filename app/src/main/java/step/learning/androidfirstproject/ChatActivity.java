package step.learning.androidfirstproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import step.learning.androidfirstproject.orm.ChatMessage;
import step.learning.androidfirstproject.orm.ChatResponse;

public class ChatActivity extends AppCompatActivity {

    private final String chatUrl = "https://chat.momentfor.fun/";
    private Animation newMessageAnimation ;
    private MediaPlayer newMessageSound ;
    private EditText etNik;
    private EditText etMessage;
    private TextView newMessage;
    private LinearLayout chatContainer;
    private final List<ChatMessage> chatMessages= new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatContainer=findViewById(R.id.chat_layout_container);
        etMessage=findViewById(R.id.chat_edit_text);
        etNik=findViewById(R.id.chat_et_nik);

        newMessage=findViewById(R.id.chat_new_message);
        findViewById(R.id.chat_tv_title).setOnClickListener(this::clickTitle);


        // завантажуємо анімацію
        newMessageAnimation = AnimationUtils.loadAnimation(
                ChatActivity.this,
                R.anim.new_message
        ) ;
        // ініціалізуємо анімацію
        newMessageAnimation.reset() ;

        newMessageSound = MediaPlayer.create( ChatActivity.this, R.raw.newmesage ) ;

        findViewById(R.id.btn_temp).setOnClickListener(this::sendButtonClick);

        new  Thread(this:: loadChatMessages).start();
    }
private void clickTitle(View view)
{
    newMessage.startAnimation(newMessageAnimation);
    newMessageSound.start();
}
    private  void  sendButtonClick(View view)
    {
        String nik = etNik.getText().toString();
        String message = etMessage.getText().toString();
        if(nik.isEmpty())
        {
            Toast.makeText(this, "Введіть нік у чаті", Toast.LENGTH_SHORT).show();
            etNik.requestFocus();
            return;
        }
        if(message.isEmpty())
        {
            Toast.makeText(this, "Введіть повідомлення", Toast.LENGTH_SHORT).show();
            etMessage.requestFocus();
            return;
        }
        new Thread(()->sendMessage(nik,message)).start();
    }

    private void sendMessage(String nik,String message)
    {
        try {
            // Надсилаємо POST - запит, це відбувається у декілька кроків
            // 1. Налаштовуємо зєднання
            URL url=new URL(chatUrl);
            HttpURLConnection connection=(HttpURLConnection) url.openConnection();
            connection.setDoOutput(true); //Це означає що запит буде мати тіло (Output)
            connection.setDoInput(true); //Це означає що від запиту очікується відповідь  (Input)
            connection.setRequestMethod("POST");
            // заголовки встановлюються як RequestProperty
            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded"); //ми надсилаємо тип html FORM (форма)
            connection.setRequestProperty("Accept","*/*"); // а отримати можемо що завгодно
            connection.setChunkedStreamingMode(0); // Не розділяти на блоки (один потік)

            //2. Формуємо запит - пишемо до Output
            OutputStream connectionOutput = connection.getOutputStream();
            String body = String.format( "author=%s&msg=%s", nik, message ) ;
            connectionOutput.write(body.getBytes(StandardCharsets.UTF_8));
            connectionOutput.flush(); // надсилаємо запит
            connectionOutput.close(); // закриваємо

            //3. Одержуємо відповідь - перевіряємо статус та читаємо Input
            int statusCode = connection.getResponseCode();
            if(statusCode==201)
            {  // відповідь - успіх, тут тіла не буде

                Log.d("sendChatMessage","Request Ok");
            }
            else {// відповідь помилка, повідомлення про неї у тілі
                InputStream connectionInput = connection.getInputStream();
                String errorMessage=streamToString(connectionInput);
                Log.d("sendChatMessage","Request failed with code "+ statusCode+" "+ errorMessage);
            }
              connection.disconnect();
            // у разі успішного надсилання завантажуємо повідомлення
            if(statusCode==201)
            {  // відповідь - успіх, тут тіла не буде

                loadChatMessages();
            }
        }
        catch (Exception ex)
        {
            Log.d("sendMessage",ex.getMessage());
        }

    }

    private View chatMessageView(ChatMessage chatMessage,int x)
    {
        LinearLayout messageContainer=new LinearLayout(ChatActivity.this);
        messageContainer.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT

        );

        // Margin -  параметри контейнера (Layout), вони задають правила взаємного
        // розміщення елементів в одному контейнері
        containerParams.setMargins(10,5,10,5);
        messageContainer.setPadding(10,5,10,5);

        Drawable messageCardRight = AppCompatResources.getDrawable(
                getApplicationContext(),
               R.drawable.message_card_right
        );
        Drawable messageCardLeft = AppCompatResources.getDrawable(
                getApplicationContext(),
                R.drawable.message_card_left
        );
        if(x % 2 != 0) {
            containerParams.gravity = Gravity.RIGHT;
            messageContainer.setBackground(messageCardRight);
        }
        else
        {
            messageContainer.setBackground(messageCardLeft);
        }

        messageContainer.setLayoutParams(containerParams);



        Typeface typeface = ResourcesCompat.getFont(this, R.font.italic);
        TextView tv= new TextView(ChatActivity.this);
        tv.setText(chatMessage.getAuthor());
        tv.setTypeface(typeface);
        tv.setPadding(20,5,190,5);
        int colorName = ContextCompat.getColor(ChatActivity.this, R.color.chat_card_message_name);
        tv.setTextColor(colorName);
        messageContainer.addView(tv);

        tv= new TextView(ChatActivity.this);
        tv.setText(chatMessage.getText());
        tv.setPadding(10,5,10,5);
        tv.setTypeface(null, Typeface.ITALIC);
        messageContainer.addView(tv);

        tv= new TextView(ChatActivity.this);
        tv.setText(chatMessage.getMoment());
        tv.setGravity(Gravity.RIGHT);
        tv.setTextSize(10);
        int colorDate = ContextCompat.getColor(ChatActivity.this, R.color.chat_card_message_date);
        tv.setTextColor(colorDate);
        Typeface typeDate = ResourcesCompat.getFont(this, R.font.bold);
        tv.setTypeface(typeDate);
        messageContainer.addView(tv);

        return messageContainer;
    }

    private void showChatMessages()
    {
     int x=0;
        for(ChatMessage chatMessage:chatMessages)
        {

            chatContainer.addView(chatMessageView(chatMessage,x) );
            x++;
        }
    }
    private void loadChatMessages() {

        try( InputStream inputStream = new URL( chatUrl ).openStream() )
        {

            ChatResponse  chatResponse=ChatResponse.fromJsonString(
                    streamToString(inputStream));
            // Перевіряємо на нові повідомлення оновлюємо за потребою
            boolean wasNewMessage=false;
            chatResponse.getData().sort(Comparator.comparing(ChatMessage::getDate));
            for(ChatMessage message: chatResponse.getData())
            {
                if(chatMessages.stream().noneMatch(m->m.getId().equals(message.getId())))
                {//це нове повідомлення (немає у колекції)
                    chatMessages.add(message);
                    wasNewMessage=true;
                }
            }
            if(wasNewMessage)
            {
                newMessage.startAnimation(newMessageAnimation);
                newMessageSound.start();
                runOnUiThread(this::showChatMessages);
            }
        }
        catch( NetworkOnMainThreadException ignored ) {
            Log.e( "loadChatMessages", "NetworkOnMainThreadException" ) ;
        }
        catch( MalformedURLException ex ) {
            Log.e( "loadChatMessages", "URL parse error: " + ex.getMessage() ) ;
        }
        catch( IOException ex ) {
            Log.e( "loadChatMessages", "IO error: " + ex.getMessage() ) ;
        }


    }

    private String streamToString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream builder = new ByteArrayOutputStream();
        byte[] buffer= new byte[4096];
        int bytesReceived;
        while ((bytesReceived=inputStream.read(buffer))>0)
        {
            builder.write(buffer,0,bytesReceived);

        }
        return  builder.toString();
    }
}