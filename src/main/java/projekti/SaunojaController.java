package projekti;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SaunojaController {

    @Autowired
    private SaunojaRepository saunojaRepository;

    @Autowired
    private SaunojaService saunojaService;

    @Autowired
    private AccountFreezeService accountFreezeService;

    private List<Saunoja> saunojat = new ArrayList<>();

    @GetMapping("/main")
    public String createNewOrSignIn(Model model) {

        saunojaService.addCurrentSaunojaToModel(model);

        saunojaService.addFollowingFollowedByBlockedToModel(model);

        return "main";
    }

    @GetMapping("/register")
    public String viewAccountForm(Model model, @ModelAttribute NewSaunoja newSaunoja) {

        saunojaService.addCurrentSaunojaToModel(model);

        saunojaService.addFollowingFollowedByBlockedToModel(model);

        return "register";
    }

    @PostMapping("/register")
    public String createNewAccount(Model model, @Valid @ModelAttribute NewSaunoja newSaunoja, BindingResult bindingResult) throws IOException {

        if (saunojaService.hasErrorsOnRegistration(newSaunoja, bindingResult)) {

            saunojaService.addCurrentSaunojaToModel(model);

            saunojaService.addValuesFromBeforeToModel(model, newSaunoja);

            return "register";
        }

        saunojaService.createNewAccount(newSaunoja);

        return "redirect:/main?created=true";
    }

    @GetMapping("/saunojat/{username}")
    public String viewSaunoja(Model model, @PathVariable String username, @ModelAttribute NewPhoto newPhoto, @ModelAttribute NewNotification newNotification, @ModelAttribute NewAccountFreeze newAccountFreeze) {

        if (saunojaRepository.findByUsername(username) == null) {

            return "saunojaNotFound";
        }

        accountFreezeService.checkIfFrozen(model);

        saunojaService.addAttributesToModelForPageSaunoja(model, username);

        return "saunoja";
    }

    @GetMapping("/saunojat")
    public String viewSaunojat(Model model) {

        model.addAttribute("saunojat", saunojat);

        saunojaService.addCurrentSaunojaToModel(model);

        saunojaService.addFollowingFollowedByBlockedToModel(model);

        return "saunojat";
    }

    @PostMapping("/saunojat")
    public String findSaunoja(@RequestParam String string) {

        saunojat = saunojaRepository.findByFirstNameIgnoreCaseContainingOrLastNameIgnoreCaseContainingOrUsernameIgnoreCaseContainingOrderByUsername(string, string, string);

        Collections.sort(saunojat);

        return "redirect:/saunojat";
    }

    @Transactional
    @PostMapping(path = "/saunojat/{username}/follow")
    public String follow(@RequestParam String action, @PathVariable String username) throws UnsupportedEncodingException {

        Saunoja subject = saunojaService.getCurrentSaunoja();

        Saunoja object = saunojaService.getByUsername(username);

        saunojaService.followUnfollowBlockUnblock(action, subject, object);

        String encodedUsername = URLEncoder.encode(username, "UTF-8");

        return "redirect:/saunojat/" + encodedUsername;
    }

    @PostMapping("/saunojat/{username}/authorize")
    public String auhtorize(@PathVariable String username, @RequestParam String action) throws UnsupportedEncodingException {

        saunojaService.authorize(username, action);

        String encodedUsername = URLEncoder.encode(username, "UTF-8");

        return "redirect:/saunojat/" + encodedUsername;
    }

    @PostMapping("/saunojat/{username}/delete")
    public String deleteSaunoja(Model model, @PathVariable String username, @Valid @ModelAttribute newDeleteAccount newDeleteAccount, BindingResult bindingResult, @ModelAttribute NewPassword newPassword) {
        
        if (saunojaService.hasErrorsOnAccountDeletion(newDeleteAccount, bindingResult)) {
            
            saunojaService.addCurrentSaunojaToModel(model);

            saunojaService.addFollowingFollowedByBlockedToModel(model);
            
            return "accountManagement";
        }
        
        return saunojaService.deleteSaunoja(username);
    }

    @GetMapping("/tili")
    public String viewAccountManagement(Model model, @ModelAttribute NewPassword newPassword, @ModelAttribute newDeleteAccount newDeleteAccount) {

        saunojaService.addCurrentSaunojaToModel(model);

        saunojaService.addFollowingFollowedByBlockedToModel(model);

        return "accountManagement";
    }

    @PostMapping("/tili")
    public String updatePassword(Model model, @Valid @ModelAttribute NewPassword newPassword, BindingResult bindingResult) throws UnsupportedEncodingException {

        if (saunojaService.hasErrorsOnPasswordUpdate(newPassword, bindingResult)) {

            saunojaService.addCurrentSaunojaToModel(model);

            saunojaService.addFollowingFollowedByBlockedToModel(model);

            return "accountManagement";
        }

        saunojaService.updatePassword(newPassword);

        return "redirect:/wall";
    }

    @PostConstruct
    public void atStart() throws IOException {

        saunojaService.createSomeSaunojas();
    }

}
