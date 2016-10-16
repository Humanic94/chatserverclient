package no.ntnu.tollefsen.crazychat;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

import no.ntnu.tollefsen.crazychat.domain.Conversation;
import no.ntnu.tollefsen.crazychat.domain.Message;
import no.ntnu.tollefsen.crazychat.domain.MessageService;

public class SearchActivity extends AppCompatActivity {

    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView conversationView = (RecyclerView) findViewById(R.id.messageView);
        conversationView.setLayoutManager(new LinearLayoutManager(this));
        conversationView.setHasFixedSize(true);
        messageAdapter = new MessageAdapter(this);
        messageAdapter.setLayoutLeft(R.layout.conversation_row);
        messageAdapter.setLayoutRight(R.layout.conversation_row);
        messageAdapter.setOnClickListener(conversationId -> {
            Intent intent = new Intent(this, MessageActivity.class);
            intent.putExtra(MessageActivity.INTENT_CONVERSATION_ID, conversationId);
            startActivity(intent);
        });

        conversationView.setAdapter(messageAdapter);

        Intent intent = getIntent();
        if(Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);
            doSearch(query);
        }
    }


    /**
     *
     * @param query
     */
    private void doSearch(String query) {
        query = query.toLowerCase();

        List<Message> result = new ArrayList<>();
        for(Conversation item : MessageService.getSingleton(this).getConversations()) {
            for(Message message : item.getMessages()) {
                if (message.getText().toLowerCase().contains(query)) {
                    result.add(message);
                }
            }
        }

        doPresentResult(result);
    }


    /**
     *
     * @param results
     */
    private void doPresentResult(List<Message> results) {
        messageAdapter.getItems().addAll(results);
        messageAdapter.notifyDataSetChanged();
    }
}
