package projekti;

import java.time.LocalDateTime;
import java.util.Comparator;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Block extends AbstractPersistable<Long> {
    
    @OneToOne
    private Saunoja blocker;
    
    @OneToOne
    private Saunoja blocked;
    
    private LocalDateTime created;
    
    public static Comparator<Block> COMPARE_BY_BLOCKER = new Comparator<Block>() {
        @Override
        public int compare(Block one, Block other) {
            
            return one.blocker.compareTo(other.blocker);
        }
    };
    
    public static Comparator<Block> COMPARE_BY_BLOCKED = new Comparator<Block>() {
        @Override
        public int compare(Block one, Block other) {
            
            return one.blocked.compareTo(other.blocked);
        }
    };
    

}
