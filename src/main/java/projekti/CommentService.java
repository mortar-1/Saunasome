package projekti;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private SaunojaService saunojaService;

    public void commentMessage(Long id, String content) {

        Comment comment = newCommentWithTimeAuthorContentSet(content);

        comment.setMessage(messageRepository.getOne(id));

        commentRepository.save(comment);
    }

    public void commentPhoto(Long id, String content) {

        Comment comment = newCommentWithTimeAuthorContentSet(content);

        comment.setPhoto(photoRepository.getOne(id));

        commentRepository.save(comment);
    }

    public List<Comment> getLast10MessageComments(Long id) {

        Pageable pageable = PageRequest.of(0, 10, Sort.by("created").descending());

        return commentRepository.findByMessageId(id, pageable);
    }

    public List<Comment> getLast10PhotoComments(Long id) {

        Pageable pageable = PageRequest.of(0, 10, Sort.by("created").descending());

        return commentRepository.findByPhotoId(id, pageable);
    }

    public Comment newCommentWithTimeAuthorContentSet(String content) {

        Comment comment = new Comment();

        comment.setCreated(LocalDateTime.now());

        comment.setAuthor(saunojaService.getCurrentSaunoja());

        comment.setContent(content);
        
        comment.setLikes(new ArrayList<String>());

        return comment;
    }
    
    public void like(Long id, String action) {

        Comment comment = commentRepository.getOne(id);

        if (action.equals("like")) {

            if (!comment.getLikes().contains(saunojaService.getCurrentUsername())) {

                comment.getLikes().add(saunojaService.getCurrentUsername());
            }
        }

        if (action.equals("unlike")) {

            if (comment.getLikes().contains(saunojaService.getCurrentUsername())) {

                comment.getLikes().remove(saunojaService.getCurrentUsername());
            }
        }

        commentRepository.save(comment);
    }

    @PreAuthorize("#usernameAuthor == authentication.principal.username or hasAuthority('ADMIN')")
    public void delete(Long id, String usernameAuthor) {

        commentRepository.deleteById(id);
    }

}
