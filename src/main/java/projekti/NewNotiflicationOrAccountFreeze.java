package projekti;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NewNotiflicationOrAccountFreeze {

    private String content;

    private Boolean isAlert;

    private String timeToExpiration;

    private String timeUnit;

}
