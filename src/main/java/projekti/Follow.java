package projekti;

import java.time.LocalDateTime;
import java.util.Comparator;
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
public class Follow extends AbstractPersistable<Long>{
    
    @ManyToOne
    private Saunoja follower;
    
    @ManyToOne
    private Saunoja followed;
    
    private LocalDateTime created;
    
    public static Comparator<Follow> COMPARE_BY_FOLLOWER = new Comparator<Follow>() {
        @Override
        public int compare(Follow one, Follow other) {
            
            return one.follower.compareTo(other.follower);
        }
    };
    
    public static Comparator<Follow> COMPARE_BY_FOLLOWED = new Comparator<Follow>() {
        @Override
        public int compare(Follow one, Follow other) {
            
            return one.followed.compareTo(other.followed);
        }
    };
    
}
