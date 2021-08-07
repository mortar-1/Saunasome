package projekti;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockRepository extends JpaRepository<Block, Long> {
    
    Block findByBlockerAndBlocked(Saunoja blocker, Saunoja blocked);
    
    List<Block> findByBlocker(Saunoja blocker);
    
    long deleteByBlockerAndBlocked(Saunoja blocker, Saunoja blocked);
}
