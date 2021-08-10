package projekti;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Notification extends AbstractPersistable<Long> {

    @OneToOne
    private Saunoja author;

    @OneToOne
    private Saunoja recipient;

    private String content;

    private LocalDateTime created;

    private LocalDateTime expires;

}
