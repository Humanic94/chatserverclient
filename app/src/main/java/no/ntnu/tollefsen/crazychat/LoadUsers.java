package no.ntnu.tollefsen.crazychat;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by Jorgen on 14.10.2016.
 */ /** * Created by mikael on 14.10.2016. */
public class LoadUsers extends AsyncTask<String,Long,List<LoadUsers.User>> {
    static SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    static SimpleDateFormat DFSHORT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

    interface Callback { void update(List<User> usrs); }
    Callback callback;
    public LoadUsers(Callback callback) {
        this.callback = callback; }
    @Override protected List<User> doInBackground(String... path) {
        List<User> result = new ArrayList<>();
        HttpURLConnection con = null; try {
            URL url = new URL(path[0]);
            con = (HttpURLConnection)url.openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));
            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                @Override public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    String value = json.getAsString();
                    try {
                        return value.length() > 25 ? DF.parse(value) : DFSHORT.parse(value);
                    } catch (ParseException e) { e.printStackTrace(); }
                    return null; } });
            Gson gson = builder.create();
            User[] users = gson.fromJson(br,User[].class);
            result.addAll(Arrays.asList(users));
        } catch (IOException e) { e.printStackTrace(); }
        finally { if(con != null) {con.disconnect();} } return result; }
    @Override protected void onPostExecute(List<User> users) {
        callback.update(users); }
    public static class User {
        Long id;
        String name;
        Date created;
        Date updated; } }

