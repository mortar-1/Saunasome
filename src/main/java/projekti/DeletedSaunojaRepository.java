package projekti;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DeletedSaunojaRepository extends JpaRepository<DeletedSaunoja, Long> {
    
    DeletedSaunoja findByUsernameIgnoreCase(String username);

}
