package projekti;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

@Service
public class MessageService {

    @Autowired
    private CommentService commentService;

    @Autowired
    private FollowRepository followRepository;
    
    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SaunojaService saunojaService;

    public void addAttributesToModelForPageMessage(Model model, Long id) {

        addPresentNextPreviousToModel(model, id);

        saunojaService.addCurrentSaunojaToModel(model);

        saunojaService.addFollowingFollowedByBlockedToModel(model);

        model.addAttribute("comments", commentService.getLast10MessageComments(id));

        saunojaService.addBlockedByProfilePageAuthor(model, messageRepository.getOne(id).getAuthor().getUsername());
    }

    public void addAttributesToModelForPageWall(Model model) {

        addMessagesToModel(model, saunojaService.getCurrentSaunoja());

        addWelcomeMessageToModel(model);

        saunojaService.addCurrentSaunojaToModel(model);

        saunojaService.addFollowingFollowedByBlockedToModel(model);

        notificationService.addNotificationsForCurrentSaunojaToModel(model);
    }

    public void addMessagesToModel(Model model, Saunoja saunoja) {

        if (saunoja.getRoles().contains("ADMIN")) {

            model.addAttribute("messages", get100MessagesForAdmin());
        } else {

            model.addAttribute("messages", get25Messages(saunoja));
        }
    }

    public void addPresentNextPreviousToModel(Model model, Long id) {

        Message message = messageRepository.getOne(id);

        List<Message> messages = get25Messages(saunojaService.getCurrentSaunoja());

        if (saunojaService.getCurrentAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {

            messages = get100MessagesForAdmin();
        }

        if (!messages.isEmpty()) {

            int nextMessageIndex = messages.indexOf(message) + 1;
            int previousMessageIndex = messages.indexOf(message) - 1;

            if (nextMessageIndex >= messages.size()) {

                nextMessageIndex = messages.size() - 1;
            }

            if (previousMessageIndex < 0) {

                previousMessageIndex = 0;
            }

            Long nextId = messages.get(nextMessageIndex).getId();

            Long previousId = messages.get(previousMessageIndex).getId();

            model.addAttribute("nextId", nextId);

            model.addAttribute("previousId", previousId);

            model.addAttribute("message", message);
        }
    }

    public void addWelcomeMessageToModel(Model model) {

        Message message = newEmptyMessage();

        message.setAuthor(saunojaService.getByUsername("V??in??m??inen"));

        message.setContent("Tervetuloa leppoisaan seuraamme " + saunojaService.getCurrentUsername()
                + "! Istut nyt lauteilla. T????ll?? kuulet kaikki vitsit ja tarinat niilt?? saunojilta, joita sin?? p????t??t kuunnella. Klikkaamalla omaa profiilikuvaasi sivun yl??laidassa, saat n??kyviin ne saunojat joita sin?? kuuntelet ja jotka kuuntelevat sinua. Antoisia l??ylyj??!");

        model.addAttribute("welcomeMessage", message);
    }

    @PreAuthorize("#usernameAuthor == authentication.principal.username or hasAuthority('ADMIN')")
    public void deleteMessage(Long id, String usernameAuthor) {

        messageRepository.deleteById(id);
    }

    public List<Message> get100MessagesForAdmin() {

        Pageable pageable = PageRequest.of(0, 100, Sort.by("created").descending());

        return (List<Message>) messageRepository.findAll(pageable).getContent();
    }

    public List<Message> get25Messages(Saunoja saunoja) {

        Pageable pageable = PageRequest.of(0, 25, Sort.by("created").descending());

        List<Saunoja> saunojat = new ArrayList<>();

        saunojat.add(saunoja);

        followRepository.findByFollower(saunoja)
                .stream()
                .forEach(follow -> saunojat.add(follow.getFollowed()));

        return messageRepository.findByAuthorIn(saunojat, pageable);
    }

    public List<Message> getNext50MessagesForAdmin(int current) {

        Pageable pageable = PageRequest.of(current, current + 50, Sort.by("created").descending());

        return (List<Message>) messageRepository.findAll(pageable);
    }

    public Boolean hasErrorsInNewMessage(NewMessage newMessage, BindingResult bindingResult) {

        if (newMessage.getContent() != null && newMessage.getContent().length() >= 2000) {

            bindingResult.rejectValue("content", "error.message", "Viesti on liian pitk??.");
        }

        if (newMessage.getContent() == null || newMessage.getContent().isBlank()) {

            bindingResult.rejectValue("content", "error.message", "Viesti ei saa olla tyhj??.");
        }

        return bindingResult.hasErrors();
    }

    public void like(Long id, String action) {

        Message message = messageRepository.getOne(id);

        if (action.equals("like")) {

            if (!message.getLikes().contains(saunojaService.getCurrentUsername())) {

                message.getLikes().add(saunojaService.getCurrentUsername());
            }
        }

        if (action.equals("unlike")) {

            if (message.getLikes().contains(saunojaService.getCurrentUsername())) {

                message.getLikes().remove(saunojaService.getCurrentUsername());
            }
        }

        messageRepository.save(message);
    }

    public Boolean messageNotFound(Long id) {

        return !messageRepository.existsById(id);
    }

    public Message newEmptyMessage() {

        Message message = new Message();

        message.setAuthor(saunojaService.getCurrentSaunoja());

        message.setCreated(LocalDateTime.now());

        message.setLikes(new ArrayList<>());

        message.setIsNewPhotoMessage(Boolean.FALSE);

        return message;
    }

    public void newPhotoMessage(Photo photo) {

        Message message = newEmptyMessage();

        message.setAuthor(photo.getAuthor());

        message.setIsNewPhotoMessage(Boolean.TRUE);

        message.setContent("lis??si uuden kuvan.");

        message.setPhotoId(photo.getId());

        messageRepository.save(message);
    }

    @PreAuthorize("hasAuthority('USER') and !hasAuthority('FROZEN')")
    public void postNew(String newMessageContent) {

        Message message = newEmptyMessage();

        message.setContent(newMessageContent);

        messageRepository.save(message);
    }

}
