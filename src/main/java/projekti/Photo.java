package projekti;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
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
public class Photo extends AbstractPersistable<Long> {

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] content;

    @ManyToOne
    private Saunoja author;

    private String description;

    private Boolean isProfilePicture;
        
    private ArrayList<String> likes;
    
    @OneToMany(cascade = CascadeType.ALL, targetEntity = Comment.class, mappedBy = "photo")
    private List<Comment> comments;
    
    private LocalDateTime created;
}
