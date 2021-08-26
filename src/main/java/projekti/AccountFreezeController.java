package projekti;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AccountFreezeController {

    @Autowired
    private AccountFreezeService accountFreezeService;
    
    @Autowired
    private SaunojaService saunojaService;
    
    
    @PostMapping("/saunojat/{username}/freeze")
    public String freezeAccount(Model model, @PathVariable String username, @Valid @ModelAttribute NewAccountFreeze newAccountFreeze, BindingResult bindingResult) throws UnsupportedEncodingException {
        
        if(accountFreezeService.hasErrorsOnCreation(newAccountFreeze, bindingResult)) {
            
            saunojaService.addAttributesToModelForPageSaunoja(model, username);
            
            return "saunoja";
        }
        
        accountFreezeService.freezeAccount(username, newAccountFreeze);
       
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
