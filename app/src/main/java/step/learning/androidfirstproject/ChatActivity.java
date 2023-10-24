package step.learning.androidfirstproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import step.learning.androidfirstproject.orm.ChatMessage;
import step.learning.androidfirstproject.orm.ChatResponse;

public class ChatActivity extends AppCompatActivity {

    private final String chatUrl = "https://chat.momentfor.fun/";
    private LinearLayout chatContainer;
    private final List<ChatMessage> chatMessages= new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatContainer=findViewById(R.id.chat_layout_container);

        new  Thread(this:: loadChatMessages).start();
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