package projekti;

import javax.validation.Valid;
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
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private AccountFreezeService accountFreezeService;

    @GetMapping("/wall")
    public String viewWall(Model model, @ModelAttribute NewMessage newMessage) {
        
        accountFreezeService.checkIfFrozen(model);

        messageService.addAttributesToModelForPageWall(model);

        return "wall";
    }

    @PostMapping("/wall")
    public String postNewMessage(Model model, @Valid @ModelAttribute NewMessage newMessage, BindingResult bindingResult) {

        if (messageService.hasErrorsInNewMessage(newMessage, bindingResult)) {

            messageService.addAttributesToModelForPageWall(model);

            return "wall";
        }

        messageService.postNew(newMessage.getContent());

        return "redirect:/wall";
    }

    @GetMapping("/wall/{id}")
    public String viewMessage(Model model, @PathVariable Long id, @ModelAttribute NewComment newComment) {

        if (messageService.messageNotFound(id)) {

            return "messageNotFound";
        }
        
        accountFreezeService.checkIfFrozen(model);
                
        messageService.addAttributesToModelForPageMessage(model, id);

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
