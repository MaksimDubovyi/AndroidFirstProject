package step.learning.androidfirstproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.NetworkOnMainThreadException;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    private ScrollView svContainer;
    private String nikFilename="nik.saved";
    private Map<String,String> emoji = new HashMap<String,String>(){{
        put(":)", new String(Character.toChars(0x1f600)));
        put(":(", new String(Character.toChars(0x1f612)));
    }};
    private final List<ChatMessage> chatMessages= new ArrayList<>();
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatContainer=findViewById(R.id.chat_layout_container);
        etMessage=findViewById(R.id.chat_edit_text);
        etNik=findViewById(R.id.chat_et_nik);

        newMessage=findViewById(R.id.chat_new_message);
        findViewById(R.id.chat_tv_title).setOnClickListener(this::clickTitle);
        findViewById(R.id.chat_btn_save_nik).setOnClickListener(this::saveNikClick);

        svContainer=findViewById(R.id.chat_sv_container);

        // завантажуємо анімацію
        newMessageAnimation = AnimationUtils.loadAnimation(
                ChatActivity.this,
                R.anim.new_message
        ) ;
        // ініціалізуємо анімацію
        newMessageAnimation.reset() ;

        newMessageSound = MediaPlayer.create( ChatActivity.this, R.raw.newmesage ) ;

        findViewById(R.id.btn_temp).setOnClickListener(this::sendButtonClick);


        handler=new Handler();
        handler.post(this::updateChate);
        tryLoadNik();
    }

    private void saveNikClick(View view)
    {
        try(FileOutputStream outputStream =
                     openFileOutput( nikFilename, Context.MODE_PRIVATE );
            DataOutputStream writer = new DataOutputStream( outputStream )
        ) {
            User user =new User();
            user.setNik(etNik.getText().toString());

           // writer.writeUTF(user.toJson() );
            writer.writeUTF(new Gson().toJson(user));
          writer.flush() ;
            Log.d( "saveBestScore", "save OK" ) ;
        }
        catch( IOException ex ) {
            Log.e( "saveBestScore", Objects.requireNonNull( ex.getMessage() ) ) ;
        }
    }

    private void tryLoadNik() {
        try(FileInputStream inputStream = openFileInput( nikFilename );
            DataInputStream reader = new DataInputStream( inputStream )
        ) {
            String json = reader.readUTF() ;
            Log.d( "tryLoadNik", "read: " + json ) ;
//            User user = new User( json ) ;
            User user = new Gson().fromJson(json,User.class);
            etNik.setText( user.getNik() ) ;
        }
        catch( Exception ex ) {
            Log.e( "tryLoadNik", Objects.requireNonNull( ex.getMessage() ) ) ;
        }
    }


    private void updateChate()
    {
        new  Thread(this:: loadChatMessages).start();
        handler.postDelayed(this::updateChate,3000);
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
            String body = String.format( "author=%s&msg=%s", URLEncoder.encode(nik,StandardCharsets.UTF_8.name()),
                                                             URLEncoder.encode(message,StandardCharsets.UTF_8.name()));

            connectionOutput.write(body.getBytes(StandardCharsets.UTF_8));
            connectionOutput.flush(); // надсилаємо запит
            connectionOutput.close(); // закриваємо

            //3. Одержуємо відповідь - перевіряємо статус та читаємо Input
            int statusCode = connection.getResponseCode();
            if(statusCode==201)
            {  // відповідь - успіх, тут тіла не буде

                Log.d("sendChatMessage","Request Ok");
                // Тут ми певні що повідомлення надіслене - стираємо його текст у полі введення
              runOnUiThread(()->  etMessage.setText("")); // Так як в другому потоці використовуємо runOnUiThread
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

    private View chatMessageView(ChatMessage chatMessage)
    {
        LinearLayout messageContainer=new LinearLayout(ChatActivity.this);
        messageContainer.setOrientation(LinearLayout.VERTICAL);

        // звякування
        messageContainer.setTag(chatMessage);
        chatMessage.setView(messageContainer);

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
        String nik=etNik.getText().toString();


        if(nik.equals(chatMessage.getAuthor())) {// перевірка чи це моє повідомлення
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
        //0x1f600= U+1f600

        String txt = chatMessage.getText();
                for(String smile: emoji.keySet())
                {
                    txt=txt.replace(smile,emoji.get(smile));
                }


//        tv.setText(chatMessage.getText());
        tv.setText(txt);
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

     boolean needScroll=false;
        for(ChatMessage chatMessage:chatMessages)
        {
            if(chatMessage.getView()==null)// перевірка чи ще повідомлення не відображалось
            {

                chatContainer.addView(chatMessageView(chatMessage));
                needScroll=true;// є нові повідомленн - потрібно прокрутити контейнер
            }

        }
        if(needScroll)
        {
            svContainer.post(()->
            svContainer.fullScroll(View.FOCUS_DOWN));
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

    class User{
        private  String nik;
        public String toJson()
        {
           // return String.format("{\"nik\":\"%s\"}",nik);
            return  new Gson().toJson(this);
        }
        public User(){}
        public  User(String json) throws JSONException {
            JSONObject obj = new JSONObject(json);
            setNik(obj.getString("nik"));
        }
        public  String getNik()
        {
            return nik;
        }

        public void setNik(String nik) {
            this.nik = nik;
        }
    }
}