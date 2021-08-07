package projekti;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    List<Comment> findByMessageId(Long id, Pageable pageable);
    
    List<Comment> findByPhotoId(Long id, Pageable pageable);
    

}
