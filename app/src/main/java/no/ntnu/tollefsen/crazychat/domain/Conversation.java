package no.ntnu.tollefsen.crazychat.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mikael on 26.09.16.
 */
public class Conversation implements Serializable {
    public static final long serialVersionUID = 1L;

    private long id;
    private List<Message> messages;
    private User owner;
    private List<User> recipients;

    private Conversation() {}


    public Conversation(User owner, User... recipients) {
        this.owner = owner;
        this.recipients = new ArrayList<>(Arrays.asList(recipients));
        this.messages = new ArrayList<>();
    }

    public Message addMessage(String text) {
        Message result = new Message(getOwner(),text);

        messages.add(result);

        return  result;
    }

    public void addRecipient(User recipient) {
        this.recipients.add(recipient);
    }

    public void addRecipients(List<User> recipients) {
        this.recipients.addAll(recipients);
    }

    public List<Message> getMessages() {
        return messages;
    }

    public List<User> getRecipients() {
        return recipients;
    }

    public Message getLastMessage() throws MessageException {
        if(messages.size() == 0) {
            throw new MessageException("Conversation has no messages");
        }

        return messages.get(messages.size()-1);
    }

    public int getMessageCount() {
        return messages.size();
    }

    public User getOwner() {
        return owner;
    }

    public boolean isOwner(User user) {
        return user != null && owner != null && owner.equals(user);
    }
}
