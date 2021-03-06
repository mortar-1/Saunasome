package projekti;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Message extends AbstractPersistable<Long> {
    
    @Column(length = 2000)
    private String content;

    @ManyToOne
    private Saunoja author;

    private LocalDateTime created;

    private ArrayList<String> likes;

    @OneToMany(cascade = CascadeType.ALL, targetEntity = Comment.class, mappedBy = "message")
    private List<Comment> comments;

    private Boolean isNewPhotoMessage;

    private Long photoId;

}
