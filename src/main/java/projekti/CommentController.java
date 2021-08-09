package projekti;

import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CommentController {

    @Autowired
    private CommentService commentService;
    
    @Autowired MessageService messageService;
    
    @Autowired PhotoService photoService;

    @PostMapping("/comment/{id}/delete")
    public String deleteComment(@PathVariable Long id, @RequestParam String redirectTo, @RequestParam String usernameAuthor) {

        commentService.delete(id, usernameAuthor);

        return "redirect:" + redirectTo;
    }
    
    @PostMapping("/comment/{id}/like")
    public String likeComment(@PathVariable Long id, @RequestParam String redirectTo, @RequestParam String action) {
        
        commentService.like(id, action);
        
        return "redirect:" + redirectTo;
    }

    @PostMapping("/photo/{id}/comment")
    public String commentPhoto(Model model, @PathVariable Long id, @Valid @ModelAttribute NewComment newComment, BindingResult bindingResult) {
        
        if (commentService.hasErrorsInNewComment(bindingResult, newComment)) {
            
            photoService.addAttributesToModelForPagePhoto(model, id);
            
            return "photo";
        }
        
        commentService.commentPhoto(id, newComment.getContent());

        return "redirect:/photo/{id}/view";
    }

    @PostMapping("/wall/{id}/comment")
    public String commentMessage(Model model, @PathVariable Long id, @Valid @ModelAttribute NewComment newComment, BindingResult bindingResult) {

        if (commentService.hasErrorsInNewComment(bindingResult, newComment)) {
            
            messageService.addAttributesToModelForPageMessage(model, id);
            
            return "message";
        }
        
        commentService.commentMessage(id, newComment.getContent());

        return "redirect:/wall/" + id;
    }

}
