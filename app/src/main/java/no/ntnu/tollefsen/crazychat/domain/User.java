package no.ntnu.tollefsen.crazychat.domain;

import java.io.Serializable;

/**
 * Created by mikael on 26.09.16.
 */

public class User implements Serializable {
    public static final long serialVersionUID = 1L;

    private String name;
    private long id;
    private String photoURI;

    public User(Long id, String name){
        this.id = id;
        this.name = name;

    }
    public User(Long id, String name, String photoURI) {
        this.id = id;
        this.name = name;
        this.photoURI = photoURI;
    }

    public String getName() {
        return name;
    }

    public long getUid() {
        return id;
    }


    public String getPhotoURI() {
        return photoURI;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (id != user.id) return false;
        if (name != null ? !name.equals(user.name) : user.name != null) return false;
        return photoURI != null ? photoURI.equals(user.photoURI) : user.photoURI == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (int) (id ^ (id >>> 32));
        result = 31 * result + (photoURI != null ? photoURI.hashCode() : 0);
        return result;
    }
}
