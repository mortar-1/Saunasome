package projekti;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface AccountFreezeRepository extends JpaRepository<AccountFreeze, Long> {

    @Modifying
    @Transactional
    @Query("select a from AccountFreeze a where a.expires < :datetime")
    List<AccountFreeze> findByExpires(@Param("datetime") LocalDateTime localDateTime);

    AccountFreeze findByRecipient(Saunoja recipient);
    
    @Transactional
    void deleteByRecipient(Saunoja recipient);
}
