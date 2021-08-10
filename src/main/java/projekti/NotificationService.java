package projekti;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SaunojaService saunojaService;

    public void newNotification(String username, NewNotification newNotification) {

        Notification notification = new Notification();

        notification.setAuthor(saunojaService.getCurrentSaunoja());

        notification.setRecipient(saunojaService.getByUsername(username));

        notification.setContent(newNotification.getContent());

        notification.setCreated(LocalDateTime.now());

    }

    public Boolean hasErrorsInNewNotification(NewNotification newNotification, BindingResult bindingResult) {

        if (newNotification.getContent() != null && newNotification.getContent().length() >= 200) {

            bindingResult.rejectValue("content", "error.newNotification", "Ilmoitus on liian pitkä.");
        }

        if (newNotification.getContent() == null || newNotification.getContent().isBlank()) {

            bindingResult.rejectValue("content", "error.newNotification", "Ilmoitus ei saa olla tyhjä.");
        }

        return bindingResult.hasErrors();
    }

    public void deleteNotification(Long id) {

        if (notificationRepository.existsById(id)) {

            notificationRepository.deleteById(id);

        }
    }
}
