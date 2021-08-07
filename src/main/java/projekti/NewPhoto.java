package projekti;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@NoArgsConstructor
@Data

public class NewPhoto {

    @NotNull
    private MultipartFile photo;
    
    private String description;
    
}
