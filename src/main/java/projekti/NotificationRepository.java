package projekti;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByAuthor(Saunoja author);
    
    List<Notification> findByRecipientOrderByCreated(Saunoja recipient);
   
}
