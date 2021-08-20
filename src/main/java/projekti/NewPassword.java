package projekti;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NewPassword {

    private String oldPassword;

    @ValidPassword
    private String newPassword;

    private String confirmNewPassword;

}
