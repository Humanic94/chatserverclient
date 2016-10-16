package no.ntnu.tollefsen.crazychat.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by mikael on 26.09.16.
 */

public class Message implements Serializable {
    public static final long serialVersionUID = 1L;

    private User sender;
    private String text;
    private Date timestamp;

    public Message(User sender, String text) {
        this.sender = sender;
        this.text = text;
        this.timestamp = new Date();
    }

    public User getSender() {
        return sender;
    }

    public String getText() {
        return text != null ? text : "";
    }

    public Date getTimestamp() {
        return timestamp;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        if (sender != null ? !sender.equals(message.sender) : message.sender != null) return false;
        if (text != null ? !text.equals(message.text) : message.text != null) return false;
        return timestamp != null ? timestamp.equals(message.timestamp) : message.timestamp == null;

    }

    @Override
    public int hashCode() {
        int result = sender != null ? sender.hashCode() : 0;
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        return result;
    }
}
