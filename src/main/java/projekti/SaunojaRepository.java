package projekti;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaunojaRepository extends JpaRepository<Saunoja, Long> {

    Saunoja findByUsername(String username);

    List<Saunoja> findByFirstNameIgnoreCaseContainingOrLastNameIgnoreCaseContainingOrUsernameIgnoreCaseContaining(String string1, String string2, String string3);
   
}
