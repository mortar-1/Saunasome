package projekti;

import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

@Service
public class PhotoService {

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private SaunojaService saunojaService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private MessageRepository messageRespository;

    public void addAttributesToModelForPagePhoto(Model model, Long id) {

        model.addAttribute("comments", commentService.getLast10PhotoComments(id));

        addPhotoToModel(model, id);

        saunojaService.addCurrentSaunojaToModel(model);

        saunojaService.addFollowingFollowedByBlockedToModel(model);

        saunojaService.addBlockedByProfilePageAuthor(model, photoRepository.getOne(id).getAuthor().getUsername());
    }

    public boolean photoExists(Long id) {

        if (photoRepository.findById(id) == null) {

            return false;
        }

        return true;
    }

    public void setAsProfilePicture(Long id) {

        Photo photo = photoRepository.getOne(id);

        Saunoja author = photo.getAuthor();

        for (Photo current : photoRepository.findByAuthor(author)) {

            current.setIsProfilePicture(false);
        }

        photo.setIsProfilePicture(true);

        author.setProfilePictureId(id);

        saunojaService.setProfilePictureId(author, id);

        photoRepository.save(photo);
    }

    public Boolean hasTenPictures(Saunoja author) {

        return photoRepository.findByAuthor(author).size() == 10;
    }

    @Async
    @PreAuthorize("!hasAuthority('FROZEN')")
    public void addNewPhoto(Saunoja author, byte[] content, String description, boolean isProfilepicture, boolean isFirstPhoto) throws IOException {

        
        if (!hasTenPictures(author)) {
            

            Photo photo = createNewPhoto(author, content);

            photo.setDescription(description);

            if (isProfilepicture && !photoRepository.findByAuthor(author).isEmpty()) {
                
                for (Photo current : photoRepository.findByAuthor(author)) {

                    current.setIsProfilePicture(false);
                }
            }

            photo.setIsProfilePicture(isProfilepicture);
          
            photoRepository.save(photo);
            
            if (saunojaService.getCurrentAuthentication() != null && !isFirstPhoto) {

                messageService.newPhotoMessage(photo);
            }

            if (isProfilepicture) {
               
                saunojaService.setProfilePictureId(author, photo.getId());
            }
        }
    }

    public Boolean hasErrorsOnAddingNewPhoto(Saunoja author, NewPhoto newPhoto, BindingResult bindingResult) throws IOException {

        Double binarybitesToMegabitesCoefficient = 0.00000095367432;

        if (newPhoto.getDescription().length() > 100) {

            bindingResult.rejectValue("description", "error.newPhoto", "Kuvaus on liian pitkä");
        }

        if (newPhoto.getPhoto().getSize() * binarybitesToMegabitesCoefficient >= 5) {

            bindingResult.rejectValue("photo", "error.newSaunoja", "Kuva on liian suuri ladattavaksi (enintään 5 MB).");
        }

        if (newPhoto.getPhoto().getBytes().length == 0) {

            bindingResult.rejectValue("photo", "error.newSaunoja", "Et valinnut kuvaa.");
        }

        if (hasTenPictures(author)) {

            bindingResult.rejectValue("photo", "error.newSaunoja", "Sinulla voi olla enimmillään 10 kuvaa. Poista jokin kuva, jotta voit lisätä uuden kuvan.");
        }

        return bindingResult.hasErrors();
    }

    public void addProfilePictureToModel(Model model, String Saunoja) {

        if (photoRepository.findByAuthorAndIsProfilePicture(saunojaService.getByUsername(Saunoja), true) == null) {

            model.addAttribute("profilePicture", "null");
        } else {

            model.addAttribute("profilePicture", photoRepository.findByAuthorAndIsProfilePicture(saunojaService.getByUsername(Saunoja), true));
        }
    }

    public void addPhotosToModel(Model model, String Saunoja) {

        model.addAttribute("photos", photoRepository.findByAuthor(saunojaService.getByUsername(Saunoja)));
    }

    public void addPhotoToModel(Model model, Long id) {

        Photo photo = photoRepository.getOne(id);

        Saunoja author = photo.getAuthor();

        ArrayList<Photo> photos = (ArrayList<Photo>) photoRepository.findByAuthor(author);

        int nextPhotoIndex = photos.indexOf(photo) + 1;
        int previousPhotoIndex = photos.indexOf(photo) - 1;

        Boolean isFirst = false;

        Boolean isLast = false;

        if (nextPhotoIndex >= photos.size()) {

            nextPhotoIndex = photos.size() - 1;

            isLast = true;
        }

        if (previousPhotoIndex < 0) {

            previousPhotoIndex = 0;

            isFirst = true;
        }

        Long nextId = photos.get(nextPhotoIndex).getId();

        Long previousId = photos.get(previousPhotoIndex).getId();

        model.addAttribute("nextId", nextId);

        model.addAttribute("previousId", previousId);

        model.addAttribute("photo", photo);

        model.addAttribute("isLast", isLast);

        model.addAttribute("isFirst", isFirst);
    }

    public void like(Long id, String action) {

        Photo photo = photoRepository.getOne(id);

        if (action.equals("like")) {

            if (!photo.getLikes().contains(saunojaService.getCurrentUsername())) {

                photo.getLikes().add(saunojaService.getCurrentUsername());
            }
        }

        if (action.equals("unlike")) {

            if (photo.getLikes().contains(saunojaService.getCurrentUsername())) {

                photo.getLikes().remove(saunojaService.getCurrentUsername());
            }
        }

        photoRepository.save(photo);
    }

    public Photo createNewPhoto(Saunoja author, byte[] content) {

        Photo newPhoto = new Photo();

        newPhoto.setContent(content);

        newPhoto.setAuthor(author);

        newPhoto.setDescription("");

        newPhoto.setLikes(new ArrayList<>());

        newPhoto.setCreated(LocalDateTime.now());

        return newPhoto;
    }

    @PreAuthorize("#usernameAuthor == authentication.principal.username or hasAuthority('ADMIN')")
    public String deletePhoto(Long id, String usernameAuthor) throws IOException {
        
        Message newPhotoMessage = messageRespository.findByPhotoId(id);

        if (newPhotoMessage != null) {
            
            messageRespository.delete(newPhotoMessage);
        }

        Photo photo = photoRepository.getOne(id);

        Saunoja author = photo.getAuthor();

        if (photo.getIsProfilePicture() && photoRepository.findByAuthor(photo.getAuthor()).size() > 1) {

            if (photoRepository.findByAuthor(author).indexOf(photo) == 0) {

                photoRepository.findByAuthor(author).get(1).setIsProfilePicture(true);

                saunojaService.setProfilePictureId(author, photoRepository.findByAuthor(author).get(1).getId());
            } else {

                photoRepository.findByAuthor(author).get(0).setIsProfilePicture(true);

                saunojaService.setProfilePictureId(author, photoRepository.findByAuthor(author).get(1).getId());
            }
        }

        photoRepository.delete(photo);

        if (photoRepository.findByAuthor(photo.getAuthor()).isEmpty()) {

            saunojaService.setProfilePictureId(author, null);
        }

        return "redirect:/saunojat/" + URLEncoder.encode(author.getUsername(), "UTF-8");
    }

}
