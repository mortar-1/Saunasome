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
    private MessageRepository messageRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private SaunojaService saunojaService;
    
    @Autowired
    private CommentService commentService;
        
    public void addAttributesToModelForPageWall(Model model) {
        
        addMessagesToModel(model, saunojaService.getCurrentSaunoja());

        addWelcomeMessageToModel(model);

        saunojaService.addCurrentSaunojaToModel(model);

        saunojaService.addFollowingFollowedByBlockedToModel(model);
    }
    
    public void addAttributesToModelForPageMessage(Model model, Long id) {
                
        addPresentNextPreviousToModel(model, id);

        saunojaService.addCurrentSaunojaToModel(model);

        saunojaService.addFollowingFollowedByBlockedToModel(model);

        model.addAttribute("comments", commentService.getLast10MessageComments(id));
        
        saunojaService.addBlockedByProfilePageAuthor(model, messageRepository.getOne(id).getAuthor().getUsername());
    }

    public Boolean messageNotFound(Long id) {

        return !messageRepository.existsById(id);
    }

    public void postNew(String newMessageContent) {

        Message message = newEmptyMessage();

        message.setContent(newMessageContent);

        messageRepository.save(message);
    }
    
    public Boolean hasErrorsInNewMessage(NewMessage newMessage, BindingResult bindingResult) {

        if (newMessage.getContent() != null && newMessage.getContent().length() >= 2000) {

            bindingResult.rejectValue("content", "error.message", "Viesti on liian pitkä.");
        }

        if (newMessage.getContent() == null || newMessage.getContent().isBlank()) {

            bindingResult.rejectValue("content", "error.message", "Viesti ei saa olla tyhjä.");
        }
        
        return bindingResult.hasErrors();
    }

    public void newPhotoMessage(Long photoId) {

        Message message = newEmptyMessage();

        message.setIsNewPhotoMessage(Boolean.TRUE);

        message.setContent("lisäsi uuden kuvan.");

        message.setPhotoId(photoId);

        messageRepository.save(message);
    }

    public Message newEmptyMessage() {

        Message message = new Message();

        message.setAuthor(saunojaService.getCurrentSaunoja());

        message.setCreated(LocalDateTime.now());

        message.setLikes(new ArrayList<>());

        message.setIsNewPhotoMessage(Boolean.FALSE);

        return message;
    }

    public void addWelcomeMessageToModel(Model model) {

        Message message = newEmptyMessage();

        message.setAuthor(saunojaService.getByUsername("Ahti"));

        message.setContent("Tervetuloa leppoisaan seuraamme " + saunojaService.getCurrentUsername()
                + "! Istut nyt lauteilla ja täällä kuulet kaikki vitsit ja tarinat niiltä saunojilta, joita sinä päätät kuunnella.");

        model.addAttribute("welcomeMessage", message);
    }

    public void addMessagesToModel(Model model, Saunoja saunoja) {

        if (saunoja.getRoles().contains("ADMIN")) {

            model.addAttribute("messages", get100MessagesForAdmin());
        } else {

            model.addAttribute("messages", get25Messages(saunoja));
        }
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

    public List<Message> get25Messages(Saunoja saunoja) {

        Pageable pageable = PageRequest.of(0, 25, Sort.by("created").descending());

        List<Saunoja> saunojat = new ArrayList<>();

        saunojat.add(saunoja);

        followRepository.findByFollower(saunoja)
                .stream()
                .forEach(follow -> saunojat.add(follow.getFollowed()));

        return messageRepository.findByAuthorIn(saunojat, pageable);
    }

    public List<Message> get100MessagesForAdmin() {

        Pageable pageable = PageRequest.of(0, 100, Sort.by("created").descending());

        return (List<Message>) messageRepository.findAll(pageable).getContent();
    }

    public List<Message> getNext50MessagesForAdmin(int current) {

        Pageable pageable = PageRequest.of(current, current + 50, Sort.by("created").descending());

        return (List<Message>) messageRepository.findAll(pageable);
    }

    @PreAuthorize("#usernameAuthor == authentication.principal.username or hasAuthority('ADMIN')")
    public void deleteMessage(Long id, String usernameAuthor) {

        messageRepository.deleteById(id);
    }

}
