package projekti;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
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

            return "register";
        }

        saunojaService.createNewAccount(newSaunoja.getUsername(), newSaunoja.getPassword(), newSaunoja.getFirstName(), newSaunoja.getLastName(), newSaunoja.getPhoto());

        return "redirect:/main";
    }

    @GetMapping("/saunojat/{username}")
    public String viewSaunoja(Model model, @PathVariable String username, @ModelAttribute NewPhoto newPhoto) {

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

        saunojat = saunojaRepository.findByFirstNameIgnoreCaseContainingOrLastNameIgnoreCaseContainingOrUsernameIgnoreCaseContaining(string, string, string);

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

    @PostConstruct
    @Profile("dev")
    public void atStart() throws IOException {

        saunojaService.createSomeSaunojas();
    }

}
