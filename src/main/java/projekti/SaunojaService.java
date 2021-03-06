package projekti;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SaunojaService {

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private DeletedSaunojaRepository deletedSaunojaRepository;

    @Autowired
    private Environment environment;

    @Autowired
    private FollowRepository followRespository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PhotoService photoService;

    @Autowired
    private SaunojaRepository saunojaRepository;

    public void addAttributesToModelForPageSaunoja(Model model, String username) {

        photoService.addProfilePictureToModel(model, username);

        photoService.addPhotosToModel(model, username);

        addIsFollowingAndIsBlockingToModel(model, username);

        addCurrentSaunojaToModel(model);

        addFollowingFollowedByBlockedToModel(model);

        addBlockedByProfilePageAuthor(model, username);

        addProfileAuthorToModel(model, username);

        addIsFollowingCurrentSaunojaToModel(model, username);
    }

    public void addBlockedByProfilePageAuthor(Model model, String authorUsername) {

        ArrayList<Block> blocked = (ArrayList<Block>) blockRepository.findByBlocker(getByUsername(authorUsername));

        ArrayList<String> blockedUsernames = new ArrayList<>();

        for (Block current : blocked) {

            blockedUsernames.add(current.getBlocked().getUsername());
        }

        Collections.sort(blockedUsernames);

        model.addAttribute("authorBlockedUsernames", blockedUsernames);
    }

    public void addCurrentSaunojaToModel(Model model) {

        model.addAttribute("currentSaunoja", getCurrentSaunoja());
    }

    public void addFollowingFollowedByBlockedToModel(Model model) {

        ArrayList<Follow> following = (ArrayList<Follow>) followRespository.findByFollower(getCurrentSaunoja());

        ArrayList<Follow> followedBy = (ArrayList<Follow>) followRespository.findByFollowed(getCurrentSaunoja());

        ArrayList<Block> blocked = (ArrayList<Block>) blockRepository.findByBlocker(getCurrentSaunoja());

        Collections.sort(following, Follow.COMPARE_BY_FOLLOWED);

        Collections.sort(followedBy, Follow.COMPARE_BY_FOLLOWER);

        Collections.sort(blocked, Block.COMPARE_BY_BLOCKED);

        ArrayList<String> followingUsernames = new ArrayList<>();

        for (Follow current : following) {

            followingUsernames.add(current.getFollowed().getUsername());
        }

        ArrayList<String> blockedUsernames = new ArrayList<>();

        for (Block current : blocked) {

            blockedUsernames.add(current.getBlocked().getUsername());
        }

        model.addAttribute("following", following);

        model.addAttribute("followedBy", followedBy);

        model.addAttribute("blocked", blocked);
    }

    public void addIsFollowingAndIsBlockingToModel(Model model, String objectUsername) {

        Saunoja subject = getCurrentSaunoja();

        Saunoja object = getByUsername(objectUsername);

        if (!isNotFollowing(subject, object)) {

            model.addAttribute("isfollowing", "true");

            model.addAttribute("followCreated", followingFromWhen(subject, object));
        }

        if (isNotFollowing(subject, object)) {

            model.addAttribute("isfollowing", "false");
        }

        if (!haveNotBlocked(subject, object)) {

            model.addAttribute("isBlocking", "true");

            model.addAttribute("blockCreated", blockingFromWhen(subject, object));
        }

        if (haveNotBlocked(subject, object)) {

            model.addAttribute("isBlocking", "false");
        }
    }

    public void addIsFollowingCurrentSaunojaToModel(Model model, String authorUsername) {

        Saunoja author = getByUsername(authorUsername);

        if (!isNotFollowing(author, getCurrentSaunoja())) {

            model.addAttribute("isFollowingCurrentSaunoja", "true");

            model.addAttribute("followingCurrentSaunojaFromWhen", followingFromWhen(author, getCurrentSaunoja()));
        }
    }

    public void addProfileAuthorToModel(Model model, String username) {

        model.addAttribute("saunoja", saunojaRepository.findByUsername(username));
    }

    public void addValuesFromBeforeToModel(Model model, NewSaunoja newSaunoja) {

        model.addAttribute("firstNameFromBefore", newSaunoja.getFirstName());

        model.addAttribute("lastNameFromBefore", newSaunoja.getLastName());

        model.addAttribute("usernameFromBefore", newSaunoja.getUsername());
    }

    public Boolean areNotSame(Saunoja subject, Saunoja object) {

        return !subject.equals(object);
    }

    @PreAuthorize("hasAuthority('GOD')")
    public void authorize(String username, String action) {

        Saunoja saunoja = saunojaRepository.findByUsername(username);

        if (action.equals("addAdmin")) {

            if (!saunoja.getRoles().contains("ADMIN")) {

                saunoja.getRoles().add("ADMIN");

                notificationService.newNotification(username, new NewNotiflicationOrAccountFreeze("Sinulla on nyt ADMIN tason k??ytt??oikeudet.", Boolean.FALSE, null, null));
            }
        }

        if (action.equals("removeAdmin")) {

            if (saunoja.getRoles().contains("ADMIN")) {

                saunoja.getRoles().remove("ADMIN");

                notificationService.newNotification(username, new NewNotiflicationOrAccountFreeze("Sinulta poistettiin ADMIN tason k??ytt??oikeudet.", Boolean.TRUE, null, null));
            }
        }

        if (action.equals("addGod")) {

            if (!saunoja.getRoles().contains("GOD")) {

                saunoja.getRoles().add("GOD");

                notificationService.newNotification(username, new NewNotiflicationOrAccountFreeze("Sinulla on nyt GOD tason k??ytt??oikeudet.", Boolean.FALSE, null, null));
            }
        }

        if (action.equals("removeGod")) {

            if (saunoja.getRoles().contains("GOD")) {

                saunoja.getRoles().remove("GOD");

                notificationService.newNotification(username, new NewNotiflicationOrAccountFreeze("Sinulta poistettiin GOD tason k??ytt??oikeudet.", Boolean.TRUE, null, null));
            }
        }

        if (action.equals("addFrozen")) {

            if (!saunoja.getRoles().contains("FROZEN")) {

                if (saunoja.getRoles().contains("ADMIN")) {

                    saunoja.getRoles().remove("ADMIN");
                }

                if (saunoja.getRoles().contains("GOD")) {

                    saunoja.getRoles().remove("GOD");
                }

                saunoja.getRoles().add("FROZEN");

                notificationService.newNotification(username, new NewNotiflicationOrAccountFreeze("Sinut laitettiin avantoon.", Boolean.TRUE, null, null));
            }
        }

        if (action.equals("removeFrozen")) {

            if (saunoja.getRoles().contains("FROZEN")) {

                saunoja.getRoles().remove("FROZEN");

                notificationService.newNotification(username, new NewNotiflicationOrAccountFreeze("Sinut otettiin takaisin avannosta.", Boolean.FALSE, null, null));
            }
        }

        saunojaRepository.save(saunoja);
    }

    @PreAuthorize("!#blocked.roles.contains('ADMIN')")
    public void block(Saunoja blocker, Saunoja blocked) {

        if (haveNotBlocked(blocker, blocked)) {

            unFollow(blocker, blocked);

            unFollow(blocked, blocker);

            blockRepository.save(new Block(blocker, blocked, LocalDateTime.now()));
        }
    }

    public LocalDateTime blockingFromWhen(Saunoja blocker, Saunoja blocked) {

        Block block = blockRepository.findByBlockerAndBlocked(blocker, blocked);

        return block.getCreated();
    }

    public void createGod() throws IOException {

        List<String> roles = new ArrayList<>();

        roles.add("USER");

        roles.add("ADMIN");

        roles.add("GOD");

        saunojaRepository.save(new Saunoja("V??in??m??inen", passwordEncoder.encode("!Sauna5"), "Ariel", "M??rtengren", roles, LocalDateTime.now(), null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
    }

    public void createNewAccount(NewSaunoja newSaunoja) throws IOException {

        String username = newSaunoja.getUsername();

        String firstName = newSaunoja.getFirstName();

        String lastName = newSaunoja.getLastName();

        String password = newSaunoja.getPassword();

        MultipartFile photo = newSaunoja.getPhoto();

        if (saunojaRepository.findByUsernameIgnoreCase(username) != null && deletedSaunojaRepository.findByUsernameIgnoreCase(username) != null) {

            return;
        }

        List<String> roles = new ArrayList<>();

        roles.add("USER");

        Saunoja saunoja = new Saunoja(username, passwordEncoder.encode(password), firstName, lastName, roles, LocalDateTime.now(), null, new ArrayList<Photo>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        saunojaRepository.save(saunoja);

        if (photo.getBytes().length > 0) {

            photoService.addNewPhoto(getByUsername(username), photo.getBytes(), "", true, true);
        }
    }

    public void createSomeSaunojas() throws IOException {

        if (saunojaRepository.findAll().isEmpty()) {

            createGod();

            if (Arrays.asList(environment.getActiveProfiles()).contains("dev")) {

                List<String> roles = new ArrayList<>();

                roles.add("USER");

                saunojaRepository.save(new Saunoja("landepaukku", passwordEncoder.encode("!Sauna5"), "Antti", "Isotalo", roles, LocalDateTime.now(), null, new ArrayList<Photo>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));

                saunojaRepository.save(new Saunoja("voikukka", passwordEncoder.encode("!Sauna5"), "Anniina", "Isotalo", roles, LocalDateTime.now(), null, new ArrayList<Photo>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));

                roles.add("ADMIN");

                saunojaRepository.save(new Saunoja("Saunatonttu", passwordEncoder.encode("!Sauna5"), "Sauna", "Tonttu", roles, LocalDateTime.now(), null, new ArrayList<Photo>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
            }
        }
    }

    @PreAuthorize("#username == authentication.principal.username or hasAuthority('GOD')")
    public String deleteSaunoja(String username) {

        String returnString = "redirect:/wall";

        if (!getCurrentSaunoja().getRoles().contains("GOD")) {

            returnString = "redirect:/main?accountDeleted=true";

            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

            logout(request);
        }

        Saunoja saunoja = saunojaRepository.findByUsername(username);

        if (saunoja != null) {

            saunoja.getRoles().clear();

            saunojaRepository.save(saunoja);

            saunojaRepository.delete(saunoja);

            deletedSaunojaRepository.save(new DeletedSaunoja(username, LocalDateTime.now()));
        }

        return returnString;
    }

    public void follow(Saunoja follower, Saunoja followed) {

        if (areNotSame(follower, followed) && haveNotBlocked(followed, follower) && isNotFollowing(follower, followed)) {

            if (!haveNotBlocked(follower, followed)) {

                unBlock(follower, followed);
            }

            followRespository.save(new Follow(follower, followed, LocalDateTime.now()));
        }
    }

    public void followUnfollowBlockUnblock(String action, Saunoja subject, Saunoja object) {

        if (action.equals("follow")) {

            follow(subject, object);
        }

        if (action.equals("unfollow")) {

            unFollow(subject, object);
        }

        if (action.equals("block")) {

            block(subject, object);
        }

        if (action.equals("unblock")) {

            unBlock(subject, object);
        }
    }

    public LocalDateTime followingFromWhen(Saunoja follower, Saunoja followed) {

        Follow follow = followRespository.findByFollowerAndFollowed(follower, followed);

        return follow.getCreated();
    }

    public Saunoja getByAuthentication(Authentication auth) {

        return saunojaRepository.findByUsername(auth.getName());
    }

    public Saunoja getByUsername(String username) {

        return saunojaRepository.findByUsername(username);
    }

    public Authentication getCurrentAuthentication() {

        return SecurityContextHolder.getContext().getAuthentication();
    }

    public Saunoja getCurrentSaunoja() {

        return saunojaRepository.findByUsername(getCurrentUsername());
    }

    public String getCurrentUsername() {

        return getCurrentAuthentication().getName();
    }

    public Boolean hasErrorsOnAccountDeletion(newDeleteAccount newDeleteAccount, BindingResult bindingResult) {

        String currentPassword = getCurrentSaunoja().getPassword();

        if (!passwordEncoder.matches(newDeleteAccount.getPassword(), currentPassword)) {

            bindingResult.rejectValue("password", "error.newDeleteAccount", "Salasana v????rin.");
        }

        return bindingResult.hasErrors();
    }

    public Boolean hasErrorsOnPasswordUpdate(NewPassword newPassword, BindingResult bindingResult) {

        String currentPassword = getCurrentSaunoja().getPassword();

        if (!passwordEncoder.matches(newPassword.getOldPassword(), currentPassword)) {

            bindingResult.rejectValue("oldPassword", "error.newPassword", "Salasana v????rin.");
        }

        if (!newPassword.getNewPassword().equals(newPassword.getConfirmNewPassword())) {

            bindingResult.rejectValue("confirmNewPassword", "error.newPassword", "Salasanat eiv??t t??sm????.");
        }

        return bindingResult.hasErrors();
    }

    public Boolean hasErrorsOnRegistration(NewSaunoja newSaunoja, BindingResult bindingResult) {

        Double binarybitesToMegabitesCoefficient = 0.00000095367432;

        if (!newSaunoja.getPassword().equals(newSaunoja.getConfirmPassword())) {

            bindingResult.rejectValue("confirmPassword", "error.newSaunoja", "Salasanat eiv??t t??sm????.");
        }

        if (saunojaRepository.findByUsernameIgnoreCase(newSaunoja.getUsername()) != null) {

            bindingResult.rejectValue("username", "error.newSaunoja", "L??ylytunnus on varattu.");
        }

        if (deletedSaunojaRepository.findByUsernameIgnoreCase(newSaunoja.getUsername()) != null) {

            bindingResult.rejectValue("username", "error.newSaunoja", "L??ylytunnuksen k??ytt?? on estetty.");
        }

        if (newSaunoja.getUsername().contains(" ")) {

            bindingResult.rejectValue("username", "error.newSaunoja", "L??ylytunnus ei saa sis??lt???? v??lily??ntej??.");
        }

        if (newSaunoja.getUsername().length() < 2 || newSaunoja.getUsername().length() > 20 || newSaunoja.getUsername().isBlank()) {

            bindingResult.rejectValue("username", "error.newSaunoja", "L??ylytunnuksen pituden tulee olla v??lill?? 2 - 20 merkki??.");
        }

        if (newSaunoja.getPhoto().getSize() * binarybitesToMegabitesCoefficient >= 5) {

            bindingResult.rejectValue("photo", "error.newSaunoja", "Kuva on liian suuri");
        }

        return bindingResult.hasErrors();
    }

    public Boolean haveNotBlocked(Saunoja blocker, Saunoja blocked) {

        return blockRepository.findByBlockerAndBlocked(blocker, blocked) == null;
    }

    public Boolean isNotFollowing(Saunoja follower, Saunoja followed) {

        return followRespository.findByFollowerAndFollowed(follower, followed) == null;
    }

    public void logout(HttpServletRequest request) {

        new SecurityContextLogoutHandler().logout(request, null, null);
    }

    public List<SimpleGrantedAuthority> rolesToGrantedAuthority(Saunoja saunoja) {

        List<SimpleGrantedAuthority> grantedAuthorities = new ArrayList<>();

        saunoja.getRoles().forEach((current) -> {

            grantedAuthorities.add(new SimpleGrantedAuthority(current));
        });

        return grantedAuthorities;
    }

    public void setProfilePictureId(Saunoja saunoja, Long id) {

        saunoja.setProfilePictureId(id);

        saunojaRepository.save(saunoja);
    }

    public void unBlock(Saunoja blocker, Saunoja blocked) {

        if (!haveNotBlocked(blocker, blocked)) {

            blockRepository.deleteByBlockerAndBlocked(blocker, blocked);
        }
    }

    public void unFollow(Saunoja follower, Saunoja followed) {

        if (!isNotFollowing(follower, followed)) {

            followRespository.deleteByFollowerAndFollowed(follower, followed);
        }
    }

    public void updatePassword(NewPassword newPassword) {

        Saunoja saunoja = getCurrentSaunoja();

        saunoja.setPassword(passwordEncoder.encode(newPassword.getNewPassword()));

        saunojaRepository.save(saunoja);
    }
}
