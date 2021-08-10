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
public class NotificationController {

    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private SaunojaService saunojaService;
    
    @PostMapping("/saunojat/{username}/notificate")
    public String newNotification(Model model, @PathVariable String username, @Valid @ModelAttribute NewNotification newNotification, BindingResult bindingResult) throws UnsupportedEncodingException {
        
        if (notificationService.hasErrorsInNewNotification(newNotification, bindingResult)) {
            
            saunojaService.addAttributesToModelForPageSaunoja(model, username);
            
            return "saunoja";
        }
        
        notificationService.newNotification(username, newNotification);
        
        String encodedUsername = URLEncoder.encode(username, "UTF-8");

        return "redirect:/saunojat/" + encodedUsername;
    }
    
}
