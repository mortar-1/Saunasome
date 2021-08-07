package projekti;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CommentController {

    @Autowired
    private CommentService commentService;

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
    public String commentPhoto(@PathVariable Long id, @RequestParam String content) {

        commentService.commentPhoto(id, content);

        return "redirect:/photo/{id}/view";
    }

    @PostMapping("/wall/{id}/comment")
    public String commentMessage(@PathVariable Long id, @RequestParam String content) {

        commentService.commentMessage(id, content);

        return "redirect:/wall/" + id;
    }

}
