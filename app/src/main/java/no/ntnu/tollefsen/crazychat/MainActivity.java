package no.ntnu.tollefsen.crazychat;

import android.Manifest;
import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import no.ntnu.tollefsen.crazychat.domain.Conversation;
import no.ntnu.tollefsen.crazychat.domain.MessageService;
import no.ntnu.tollefsen.crazychat.domain.User;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
 

    public static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    public static final int REQUEST_CODE_NEW_MESSAGE = 1000;
    public static final int REQUEST_CODE_GET_CONTACT = 1001;

    private RecyclerView conversationView;
    private ConversationAdapter conversationAdapter;
    private MessageService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        new LoadUsers(new LoadUsers.Callback(){
            @Override
            public void update(List<LoadUsers.User> usrs) {
                //update
            } }).execute("http://192.168.1.44:8080/AndroidMessenger/services/chat/users");
        */
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //List<Conversation> allConversations = getJSONArrayToObjects("http://192.168.1.44:8080/AndroidMessengerServer/services/chat/conversations");
        //getJSON("http://192.168.1.44:8080/AndroidMessengerServer/services/chat/conversations", 2000);

        String JSON_USERS = getJSON("http://192.168.1.44:8080/AndroidMessengerServer/services/chat/users", 2000);
        Type listType = new TypeToken<ArrayList<User>>(){}.getType();
        List<User> users = new Gson().fromJson(JSON_USERS, listType);
        //List<Conversation> conversations = new ArrayList<>();
        for(User u : users){
            System.out.println("Name: " + u.getName());
            System.out.println("ID: " + u.getUid());
            System.out.println("Photo: " + u.getPhotoURI());
        }


        /*
        String JSON_CONVERSATIONS = getJSON("http://192.168.1.44:8080/AndroidMessengerServer/services/chat/conversations", 2000);
        Type listTypeC = new TypeToken<ArrayList<Conversation>>(){}.getType();
        System.out.println("JSON string: " + JSON_CONVERSATIONS);
        List<Conversation> c = new Gson().fromJson(JSON_CONVERSATIONS, listTypeC);
        System.out.println("Conversations: " + c.toString());
*/

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view -> addConversation());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        // Mikael added code below
        service = MessageService.getSingleton(this);
        service.load(this);
        List<Conversation> conversations = service.getConversations();

        //List<Conversation> conversations = getJSONArrayToObjects("http://192.168.1.44:8080/AndroidMessengerServer/services/chat/conversations");
        conversationAdapter = new ConversationAdapter(this,conversations);

        conversationView = (RecyclerView)findViewById(R.id.converationView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        conversationView.setLayoutManager(linearLayoutManager);
        conversationView.setHasFixedSize(true);
        conversationView.setAdapter(conversationAdapter);
        conversationAdapter.setOnClickListener(conversationId -> {
            if(service.getOwner() == null) {
                service.setOwner(getCurrentUser());
            }

            Intent intent = new Intent(this, MessageActivity.class);
            intent.putExtra(MessageActivity.INTENT_CONVERSATION_ID, conversationId);
            startActivityForResult(intent,REQUEST_CODE_NEW_MESSAGE);
        });
    }

    private void addConversation() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            service.setOwner(getCurrentUser());
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(intent, REQUEST_CODE_GET_CONTACT);
        }
    }


    public User getCurrentUser() {
        User result = null;

        String[] columnNames = new String[] {ContactsContract.Profile._ID,ContactsContract.Profile.DISPLAY_NAME, ContactsContract.Profile.PHOTO_THUMBNAIL_URI};
        Cursor c = getContentResolver().query(ContactsContract.Profile.CONTENT_URI, columnNames, null, null, null);
        while(c != null && c.moveToNext()) {
            long id = c.getLong(0);
            String name = c.getString(1);
            String photoURI = c.getString(2);

            result = new User(id,name,photoURI);
        }
        if(c != null) {
            c.close();
        }

        return result;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                addConversation();
            } else {
                Snackbar.make(findViewById(R.id.toolbar),"Application needs permission to read your account and contacts", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem menuItem = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);

        SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());
        searchView.setSearchableInfo(searchableInfo);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_GET_CONTACT) {
            Uri contactData = data.getData();
            Cursor c = managedQuery(contactData,null,null,null,null);
            if (c.moveToFirst()) {
                long id = c.getInt(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                String name = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                String thumb = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_URI));
                User user = new User(id,name,thumb);
                Intent intent = new Intent(this, MessageActivity.class);
                intent.putExtra(MessageActivity.INTENT_USER, user);
                startActivityForResult(intent,REQUEST_CODE_NEW_MESSAGE);
            }
        } else if(requestCode == REQUEST_CODE_NEW_MESSAGE) {
            // Remove last conversation if no messages was sent
            service.getConversations().removeIf(c -> c.getMessageCount() == 0);
            conversationAdapter.setItems(service.getConversations());
            conversationAdapter.notifyDataSetChanged();
            service.save(this);
        }
    }

    //gets the JSON object string from the specified url.
    public String getJSON(String url, int timeout) {
        HttpURLConnection c = null;
        try {
            URL u = new URL(url);
            c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("GET");
            c.setRequestProperty("Content-length", "0");
            c.setUseCaches(false);
            c.setAllowUserInteraction(false);
            c.setConnectTimeout(timeout);
            c.setReadTimeout(timeout);
            c.connect();
            int status = c.getResponseCode();

            switch (status) {
                case 200:
                    System.out.println("Code 200");
                case 201:
                    System.out.println("Code 201");
                    BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line+"\n");
                    }
                    br.close();
                    return sb.toString();
            }

        } catch (MalformedURLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (c != null) {
                try {
                    c.disconnect();
                } catch (Exception ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return null;
    }
}
