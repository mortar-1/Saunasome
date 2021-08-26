package projekti;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Saunoja extends AbstractPersistable<Long> implements Comparable<Saunoja> {

    private String username;

    private String password;

    private String firstName;

    private String lastName;

    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "roles")
    @Column(name = "role")
    private List<String> roles;

    private LocalDateTime created;

    private Long profilepictureId;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "author")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Photo> photos = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "author")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "author")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Message> messages = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "follower")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Follow> following = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "followed")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Follow> followedBy = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "blocker")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Block> blocking = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "blocked")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Block> blockedBy = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "author")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Notification> sentNotifications = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "recipient")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Notification> recievedNotifications = new ArrayList<>();

    @Override
    public int compareTo(Saunoja other) {

        return username.compareToIgnoreCase(other.username);
    }

}
