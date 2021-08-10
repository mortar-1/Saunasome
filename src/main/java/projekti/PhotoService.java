package projekti;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

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

        author.setProfilepictureId(id);

        saunojaService.setProfilepictureId(author, id);

        photoRepository.save(photo);
    }

    @PreAuthorize("hasAuthority('USER') and !hasAuthority('FROZEN')")
    public void addNewPhoto(Saunoja author, byte[] content, String description, boolean isProfilepicture, boolean isDefaultphoto) throws IOException {

        if (photoRepository.findByAuthor(author).size() < 10) {

            Photo newPhoto = createNewPhoto(author, content);

            newPhoto.setDescription(description);

            if (isProfilepicture && !photoRepository.findByAuthor(author).isEmpty()) {

                for (Photo current : photoRepository.findByAuthor(author)) {

                    current.setIsProfilePicture(false);
                }
            }

            newPhoto.setIsProfilePicture(isProfilepicture);

            photoRepository.save(newPhoto);

            if (saunojaService.getCurrentAuthentication() != null && !isDefaultphoto) {

                messageService.newPhotoMessage(newPhoto.getId());
            }

            if (isProfilepicture) {

                saunojaService.setProfilepictureId(author, newPhoto.getId());
            }
        }
    }

    public Boolean hasErrorsOnAddingNewPhoto(NewPhoto newPhoto, BindingResult bindingResult) throws IOException {

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

        return bindingResult.hasErrors();
    }

    public void addFirstPhoto(MultipartFile photo, Saunoja author) throws IOException {

        if (photo.isEmpty()) {

            addDefaultPhoto(author);
        } else {

            addNewPhoto(author, photo.getBytes(), "", true, false);
        }
    }

    public void addDefaultPhoto(Saunoja author) throws IOException {

        File defaultPhoto = new File("photos/defaultPicture.png");

        BufferedImage image = ImageIO.read(defaultPhoto);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        ImageIO.write(image, "jpg", bos);

        addNewPhoto(author, bos.toByteArray(), "", true, true);
    }

    public void addProfilePictureToModel(Model model, String Saunoja) {

        model.addAttribute("profilePicture", photoRepository.findByAuthorAndIsProfilePicture(saunojaService.getByUsername(Saunoja), true));
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
    public void deletePhoto(Long id, String usernameAuthor) throws IOException {

        Photo photo = photoRepository.getOne(id);

        Saunoja author = photo.getAuthor();

        if (photoRepository.findByAuthor(photo.getAuthor()).size() == 1) {

            addDefaultPhoto(photo.getAuthor());
        }

        if (photo.getIsProfilePicture() && photoRepository.findByAuthor(photo.getAuthor()).size() > 1) {

            if (photoRepository.findByAuthor(author).indexOf(photo) == 0) {

                photoRepository.findByAuthor(author).get(1).setIsProfilePicture(true);

                saunojaService.setProfilepictureId(author, photoRepository.findByAuthor(author).get(1).getId());
            } else {

                photoRepository.findByAuthor(author).get(0).setIsProfilePicture(true);

                saunojaService.setProfilepictureId(author, photoRepository.findByAuthor(author).get(1).getId());
            }
        }

        photoRepository.delete(photo);
    }

}
