package projekti;

import java.time.LocalDateTime;
import java.util.ArrayList;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Comment extends AbstractPersistable<Long> {
    
    @ManyToOne
    private Saunoja author;

    @Column(length = 2000)
    private String content;

    private LocalDateTime created;
    
    @ManyToOne
    private Message message;
    
    @ManyToOne
    private Photo photo;
    
    private ArrayList<String> likes;
    
}
