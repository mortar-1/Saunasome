package projekti;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    
    List<Follow> findByFollower(Saunoja follower);
    
    List<Follow> findByFollowed(Saunoja followed);
    
    long deleteByFollowerAndFollowed(Saunoja follower, Saunoja followed);
        
    Follow findByFollowerAndFollowed(Saunoja follower, Saunoja followed);

}
