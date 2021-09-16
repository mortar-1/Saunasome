package projekti;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SaunojaService saunojaService;

    public void addAttributesToModelForPageNotifications(Model model) {

        addNotificationsForAdminToModel(model);

        saunojaService.addCurrentSaunojaToModel(model);

        saunojaService.addFollowingFollowedByBlockedToModel(model);
    }

    public void addNotificationsForAdminToModel(Model model) {

        model.addAttribute("notifications", getNotificationsForAdmin());
    }

    public void addNotificationsForCurrentSaunojaToModel(Model model) {

        model.addAttribute("notifications", getNotificationsForCurrentSaunoja());
    }

    @PreAuthorize("#usernameAuthor == authentication.principal.username or hasAuthority('ADMIN')")
    public String deleteNotification(Long id, String usernameAuthor) {

        if (notificationRepository.existsById(id)) {

            Notification notification = notificationRepository.getOne(id);

            Saunoja recipient = notification.getRecipient();

            notificationRepository.delete(notification);

            if (recipient != saunojaService.getCurrentSaunoja()) {

                return "redirect:/notifications";
            }
        }

        return "redirect:/wall";
    }

    public List<Notification> getNotificationsForAdmin() {

        return notificationRepository.findAll();
    }

    public List<Notification> getNotificationsForCurrentSaunoja() {

        Saunoja saunoja = saunojaService.getCurrentSaunoja();

        return notificationRepository.findByRecipientOrderByCreated(saunoja);
    }

    public Boolean hasErrorsInNewNotification(NewNotiflicationOrAccountFreeze newNotiflicationOrAccountFreeze, BindingResult bindingResult) {

        if (newNotiflicationOrAccountFreeze.getContent() != null && newNotiflicationOrAccountFreeze.getContent().length() >= 200) {

            bindingResult.rejectValue("content", "error.newNotification", "Ilmoitus on liian pitkä.");
        }

        if (newNotiflicationOrAccountFreeze.getContent() == null || newNotiflicationOrAccountFreeze.getContent().isBlank()) {

            bindingResult.rejectValue("content", "error.newNotification", "Ilmoitus ei saa olla tyhjä.");
        }

        return bindingResult.hasErrors();
    }

    public void newNotification(String username, NewNotiflicationOrAccountFreeze newNotiflicationOrAccountFreeze) {

        Notification notification = new Notification();

        notification.setAuthor(saunojaService.getCurrentSaunoja());

        notification.setRecipient(saunojaService.getByUsername(username));

        notification.setContent(newNotiflicationOrAccountFreeze.getContent());

        notification.setCreated(LocalDateTime.now());

        notification.setIsAlert(newNotiflicationOrAccountFreeze.getIsAlert());

        notificationRepository.save(notification);
    }

}
