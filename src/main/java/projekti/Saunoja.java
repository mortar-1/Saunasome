package projekti;

import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    private List<String> roles;
    
    private LocalDateTime created;
    
    private Long profilepictureId;
    
    
    @Override
    public int compareTo(Saunoja other) {
        
        return username.compareToIgnoreCase(other.username);
    }
    
    
   
       
}
