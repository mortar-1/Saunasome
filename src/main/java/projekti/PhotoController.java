package projekti;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.h2.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PhotoController {

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private PhotoService photoService;

    @Autowired
    private AccountFreezeService accountFreezeService;

    @Autowired
    private SaunojaService saunojaService;

    @GetMapping("/photo/{id}")
    public void showPhoto(@PathVariable Long id, HttpServletResponse response) throws IOException {

        response.setContentType("image/jpg");

        Photo photo = photoRepository.getOne(id);

        InputStream is = new ByteArrayInputStream(photo.getContent());

        IOUtils.copy(is, response.getOutputStream());
    }

    @GetMapping("/photo/{id}/view")
    public String viewPhoto(Model model, @PathVariable Long id, @ModelAttribute NewComment newComment) {

        if (!photoRepository.existsById(id)) {

            return "photoNotFound";
        }

        accountFreezeService.checkIfFrozen(model);

        photoService.addAttributesToModelForPagePhoto(model, id);

        return "photo";
    }

    @PostMapping("/saunojat/{username}/photo")
    public String addPhoto(Model model, @PathVariable String username, @Valid @ModelAttribute NewPhoto newPhoto, @RequestParam(defaultValue = "false") boolean isProfilepicture, BindingResult bindingResult) throws IOException {

        Saunoja author = saunojaService.getByUsername(username);

        if (photoService.hasErrorsOnAddingNewPhoto(author, newPhoto, bindingResult)) {

            saunojaService.addAttributesToModelForPageSaunoja(model, username);

            model.addAttribute("descriptionFromBefore", newPhoto.getDescription());

            return "saunoja";
        }

        photoService.addNewPhoto(author, newPhoto.getPhoto().getBytes(), newPhoto.getDescription(), isProfilepicture, false);
        
        String encodedUsername = URLEncoder.encode(username, "UTF-8");

        return "redirect:/saunojat/" + encodedUsername;
    }

    @PostMapping("/photo/{id}/delete")
    public String deletePhoto(@PathVariable Long id, @RequestParam String redirectTo, @RequestParam String usernameAuthor) throws IOException {

        photoService.deletePhoto(id, usernameAuthor);

        return "redirect:" + redirectTo;
    }

    @PostMapping("/photo/{id}/like")
    public String likePhoto(@PathVariable Long id, @RequestParam String action) {

        photoService.like(id, action);

        return "redirect:/photo/" + id + "/view";
    }

    @PostMapping("/photo/{id}/profilepicture")
    public String setAsProfilePicture(@PathVariable Long id) {

        photoService.setAsProfilePicture(id);

        return "redirect:/photo/" + id + "/view";
    }
}
