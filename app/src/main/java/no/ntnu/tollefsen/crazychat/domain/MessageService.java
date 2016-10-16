package no.ntnu.tollefsen.crazychat.domain;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by mikael on 16.09.2016.
 */

public class MessageService {
    public static final String CONVERSATIONS_FILENAME = "conversations.dat";
    public static MessageService SINGLETON;

    private ArrayList<Conversation> conversations;

    private User owner;

    private MessageService() {
        conversations = new ArrayList<>();
    }


    public static synchronized MessageService getSingleton(Context context) {
        if(SINGLETON == null) {
            SINGLETON = new MessageService();
        }

        return SINGLETON;
    }

    public synchronized List<Conversation> getConversations() {
        return conversations;
    }

    public User getOwner() {
        return owner;
    }
    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Conversation getConversation(int conversationId) throws MessageException {
        if(conversationId > -1 && conversationId < conversations.size())
            return conversations.get(conversationId);
        else
            throw new MessageException("No such conversation " + conversationId);
    }

    public synchronized int createConversation(User... recipients) {
        Conversation result = new Conversation(getOwner(),recipients);
        conversations.add(result);
        return conversations.size()-1;
    }

    public void sendMessage(int conversationId, CharSequence text) throws MessageException {
        Conversation conversation = getConversation(conversationId);
        conversation.addMessage(text.toString());
    }

    public void save(Context context) {
        try (
            FileOutputStream fos = context.openFileOutput(CONVERSATIONS_FILENAME,Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(fos))
        ) {
            long time = System.currentTimeMillis();
            oos.writeObject(conversations);
            System.out.printf("Time to write %d conversations %dms: \n",
                               conversations.size(),System.currentTimeMillis()-time);
        } catch (IOException e) {
            Log.e("MessageService", "Failed to store " + CONVERSATIONS_FILENAME);
        }
    }

    public void load(Context context) {
        try (
            FileInputStream fis = context.openFileInput(CONVERSATIONS_FILENAME);
            ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(fis))
        ) {
            long time = System.currentTimeMillis();
            conversations = (ArrayList<Conversation>) ois.readObject();
            System.out.printf("Time to load %d conversations %dms: \n",
                    conversations.size(),System.currentTimeMillis()-time);
        } catch (IOException | ClassNotFoundException e) {
            Log.e("MessageService", "Failed to load " + CONVERSATIONS_FILENAME);
        }
    }
}
