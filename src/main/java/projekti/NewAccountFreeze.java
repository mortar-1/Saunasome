package projekti;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NewAccountFreeze {
    
    private String timeToExpiration;
    
    private String timeUnit;
        
}
