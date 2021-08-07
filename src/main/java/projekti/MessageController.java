package projekti;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private SaunojaService saunojaService;

    @GetMapping("/wall")
    public String viewWall(Model model) {

        messageService.addMessagesToModel(model, saunojaService.getCurrentSaunoja());

        messageService.addWelcomeMessageToModel(model);

        saunojaService.addCurrentSaunojaToModel(model);

        saunojaService.addFollowingFollowedByBlockedToModel(model);

        return "wall";
    }

    @PostMapping("/wall")
    public String postNewMessage(@RequestParam String newMessageContent) {

        messageService.postNew(newMessageContent);

        return "redirect:/wall";
    }

    @GetMapping("/wall/{id}")
    public String viewMessage(Model model, @PathVariable Long id) {

        if (messageService.messageNotFound(id)) {

            return "messageNotFound";
        }

        messageService.addPresentNextPreviousToModel(model, id);

        saunojaService.addCurrentSaunojaToModel(model);

        saunojaService.addFollowingFollowedByBlockedToModel(model);

        model.addAttribute("comments", commentService.getLast10MessageComments(id));

        return "message";
    }

    @PostMapping("/wall/{id}/like")
    public String likeMessage(@PathVariable Long id, @RequestParam String action, @RequestParam String redirectTo) {

        messageService.like(id, action);

        return "redirect:" + redirectTo;
    }

    @PostMapping("/wall/{id}/delete")
    public String deleteMessage(@PathVariable Long id, @RequestParam String usernameAuthor) {

        messageService.deleteMessage(id, usernameAuthor);

        return "redirect:/wall/";
    }

}
