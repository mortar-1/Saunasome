package projekti;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AccountFreezeController {

    @Autowired
    private AccountFreezeService accountFreezeService;
    
    
    @PostMapping("/saunojat/{username}/freeze")
    public String freezeAccount(@PathVariable String username, @RequestParam Integer timeToExpiration, @RequestParam String timeUnit) throws UnsupportedEncodingException {
                
        accountFreezeService.freezeAccount(username, timeToExpiration, timeUnit);
       
        String encodedUsername = URLEncoder.encode(username, "UTF-8");

        return "redirect:/saunojat/" + encodedUsername;
    }
    
    @PostMapping("/saunojat/{username}/unfreeze")
    public String unFreezeAccount(@PathVariable String username) throws UnsupportedEncodingException {
                
        accountFreezeService.unFreezeAccount(username);
       
        String encodedUsername = URLEncoder.encode(username, "UTF-8");

        return "redirect:/saunojat/" + encodedUsername;
    }
    
}
