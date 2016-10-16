package no.ntnu.tollefsen.crazychat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import no.ntnu.tollefsen.crazychat.domain.Conversation;
import no.ntnu.tollefsen.crazychat.domain.MessageException;
import no.ntnu.tollefsen.crazychat.domain.MessageService;
import no.ntnu.tollefsen.crazychat.domain.User;

public class MessageActivity extends AppCompatActivity {

    public static final String INTENT_USER = "user";
    public static final String INTENT_CONVERSATION_ID = "conversation_id";

    User receiver;
    int conversationId = -1;
    private Conversation conversation;
    private MessageService service;

    MessageAdapter messageAdapter;
    RecyclerView messagesView;
    EditText messageText;
    ImageButton sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get receiver
        service = MessageService.getSingleton(getApplicationContext());
        service.load(this);

        handleExtras();

        messageAdapter = new MessageAdapter(this,conversation.getMessages());
        messagesView = (RecyclerView) findViewById(R.id.messageView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        messagesView.setLayoutManager(linearLayoutManager);
        messagesView.setHasFixedSize(true);
        messagesView.setAdapter(messageAdapter);
        messagesView.scrollToPosition(messageAdapter.getItemCount()-1);

        messageText = (EditText) findViewById(R.id.messageText);
        sendButton = (ImageButton) findViewById(R.id.send);

        messageText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                sendButton.setEnabled(messageText.getText().length() > 0);
            }
        });
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        sendButton.setOnClickListener(view -> {

            //Sends the user to the database over internet using HttpUrlConnection
            try {
                Long userid = receiver.getUid();
                String url = "http://192.168.1.44:8080/AndroidMessengerServer/services/chat/message/create?userId="+ userid + "&conversationId=" + conversationId + "&text="+ messageText.getText() + "";
                String USER_AGENT = "Mozilla/5.0";
                URL obj = null;
                try {
                    obj = new URL(url);
                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                    // optional default is GET
                    con.setRequestMethod("GET");

                    //add request header
                    con.setRequestProperty("User-Agent", USER_AGENT);

                    int responseCode = con.getResponseCode();
                    System.out.println("\nSending 'GET' request to URL : " + url);
                    System.out.println("Response Code : " + responseCode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                service.sendMessage(conversationId, messageText.getText());
                messageAdapter.notifyDataSetChanged();
                messagesView.scrollToPosition(messageAdapter.getItemCount()-1);
                messageText.setText("");
            } catch (MessageException e) {
                Log.i("MessageActivity","Error sending message",e);
            }
        });
    }

    private void handleExtras() {
        Intent intent = getIntent();
        receiver = (User) intent.getSerializableExtra(INTENT_USER);

        conversationId = intent.getIntExtra(INTENT_CONVERSATION_ID, -1);
        try {
            if (conversationId != -1) {
                conversation = service.getConversation(conversationId);
                List<User> recipients = conversation.getRecipients();
                if(recipients.size() > 0) {
                    receiver = recipients.get(0);
                 }
            } else {
                conversationId = service.createConversation(receiver);
                conversation = service.getConversation(conversationId);
            }

            Intent resultIntent = new Intent();
            resultIntent.putExtra(INTENT_CONVERSATION_ID,conversationId);
            setResult(Activity.RESULT_OK,resultIntent);
        } catch (MessageException e) {
            Log.i("MessageActivity","Failed to get conversation " + conversationId);
        }

        if (receiver != null && receiver.getName() != null) {
            setTitle(receiver.getName());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        service.save(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
