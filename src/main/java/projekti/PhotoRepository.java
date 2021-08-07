package projekti;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhotoRepository extends JpaRepository<Photo, Long> {

    List<Photo> findByAuthor(Saunoja author);
    
    Photo findByAuthorAndIsProfilePicture(Saunoja author, Boolean isProfilePicture);
    
}
