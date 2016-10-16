package no.ntnu.tollefsen.crazychat.domain;

/**
 * Created by mikael on 26.09.16.
 */

public class MessageException extends Exception {
    public MessageException(String message) {
        super(message);
    }

    public MessageException(String message, Throwable cause) {
        super(message, cause);
    }
}
