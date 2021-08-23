package projekti;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SaunojaService {

    @Autowired
    Environment environment;

    @Autowired
    private SaunojaRepository saunojaRepository;

    @Autowired
    private PhotoService photoService;

    @Autowired
    private FollowRepository followRespository;

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private DeletedSaunojaRepository deletedSaunojaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Saunoja getByUsername(String username) {

        return saunojaRepository.findByUsername(username);
    }

    public Saunoja getByAuthentication(Authentication auth) {

        return saunojaRepository.findByUsername(auth.getName());
    }

    public Authentication getCurrentAuthentication() {

        return SecurityContextHolder.getContext().getAuthentication();
    }

    public String getCurrentUsername() {

        return getCurrentAuthentication().getName();
    }

    public Saunoja getCurrentSaunoja() {

        return saunojaRepository.findByUsername(getCurrentUsername());
    }

    public void addCurrentSaunojaToModel(Model model) {

        model.addAttribute("currentSaunoja", getCurrentSaunoja());
    }

    public void addProfileAuthorToModel(Model model, String username) {

        model.addAttribute("saunoja", saunojaRepository.findByUsername(username));
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

    public Boolean hasErrorsOnRegistration(NewSaunoja newSaunoja, BindingResult bindingResult) {

        Double binarybitesToMegabitesCoefficient = 0.00000095367432;

        if (!newSaunoja.getPassword().equals(newSaunoja.getConfirmPassword())) {

            bindingResult.rejectValue("confirmPassword", "error.newSaunoja", "Salasanat eivät täsmää.");
        }

        if (saunojaRepository.findByUsernameIgnoreCase(newSaunoja.getUsername()) != null) {

            bindingResult.rejectValue("username", "error.newSaunoja", "Löylytunnus on varattu.");
        }

        if (deletedSaunojaRepository.findByUsernameIgnoreCase(newSaunoja.getUsername()) != null) {

            bindingResult.rejectValue("username", "error.newSaunoja", "Löylytunnuksen käyttö on estetty.");
        }

        if (newSaunoja.getUsername().contains(" ")) {

            bindingResult.rejectValue("username", "error.newSaunoja", "Löylytunnus ei saa sisältää välilyöntejä.");
        }

        if (newSaunoja.getUsername().length() < 2 || newSaunoja.getUsername().length() > 20 || newSaunoja.getUsername().isBlank()) {

            bindingResult.rejectValue("username", "error.newSaunoja", "Löylytunnuksen pituden tulee olla välillä 2 - 20 merkkiä.");
        }

        if (newSaunoja.getPhoto().getSize() * binarybitesToMegabitesCoefficient >= 5) {

            bindingResult.rejectValue("photo", "error.newSaunoja", "Kuva on liian suuri");
        }

        return bindingResult.hasErrors();
    }

    public void addValuesFromBeforeToModel(Model model, NewSaunoja newSaunoja) {

        model.addAttribute("firstNameFromBefore", newSaunoja.getFirstName());

        model.addAttribute("lastNameFromBefore", newSaunoja.getLastName());

        model.addAttribute("usernameFromBefore", newSaunoja.getUsername());
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

        Saunoja saunoja = new Saunoja(username, passwordEncoder.encode(password), firstName, lastName, roles, LocalDateTime.now(), 1L, new ArrayList<Photo>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        saunojaRepository.save(saunoja);

        photoService.addFirstPhoto(photo, getByUsername(username));
    }

    public void setProfilepictureId(Saunoja saunoja, Long id) {

        saunoja.setProfilepictureId(id);

        saunojaRepository.save(saunoja);
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

    public void follow(Saunoja follower, Saunoja followed) {

        if (areNotSame(follower, followed) && haveNotBlocked(followed, follower) && isNotFollowing(follower, followed)) {

            if (!haveNotBlocked(follower, followed)) {

                unBlock(follower, followed);
            }

            followRespository.save(new Follow(follower, followed, LocalDateTime.now()));
        }
    }

    public void unFollow(Saunoja follower, Saunoja followed) {

        if (!isNotFollowing(follower, followed)) {

            followRespository.deleteByFollowerAndFollowed(follower, followed);
        }
    }

    @PreAuthorize("!#blocked.roles.contains('ADMIN')")
    public void block(Saunoja blocker, Saunoja blocked) {

        if (haveNotBlocked(blocker, blocked)) {

            unFollow(blocker, blocked);

            unFollow(blocked, blocker);

            blockRepository.save(new Block(blocker, blocked, LocalDateTime.now()));
        }
    }

    public void unBlock(Saunoja blocker, Saunoja blocked) {

        if (!haveNotBlocked(blocker, blocked)) {

            blockRepository.deleteByBlockerAndBlocked(blocker, blocked);
        }
    }

    public Boolean haveNotBlocked(Saunoja blocker, Saunoja blocked) {

        return blockRepository.findByBlockerAndBlocked(blocker, blocked) == null;
    }

    public Boolean areNotSame(Saunoja subject, Saunoja object) {

        return !subject.equals(object);
    }

    public Boolean isNotFollowing(Saunoja follower, Saunoja followed) {

        return followRespository.findByFollowerAndFollowed(follower, followed) == null;
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

    public void addAttributesToModelForPageSaunoja(Model model, String username) {

        photoService.addProfilePictureToModel(model, username);

        photoService.addPhotosToModel(model, username);

        addIsFollowingAndIsBlockingToModel(model, username);

        addCurrentSaunojaToModel(model);

        addFollowingFollowedByBlockedToModel(model);

        addBlockedByProfilePageAuthor(model, username);

        addProfileAuthorToModel(model, username);
    }

    public LocalDateTime followingFromWhen(Saunoja follower, Saunoja followed) {

        Follow follow = followRespository.findByFollowerAndFollowed(follower, followed);

        return follow.getCreated();
    }

    public LocalDateTime blockingFromWhen(Saunoja blocker, Saunoja blocked) {

        Block block = blockRepository.findByBlockerAndBlocked(blocker, blocked);

        return block.getCreated();
    }

    public List<SimpleGrantedAuthority> rolesToGrantedAuthority(Saunoja saunoja) {

        List<SimpleGrantedAuthority> grantedAuthorities = new ArrayList<>();

        saunoja.getRoles().forEach((current) -> {

            grantedAuthorities.add(new SimpleGrantedAuthority(current));
        });

        return grantedAuthorities;
    }

    @PreAuthorize("hasAuthority('GOD')")
    public void authorize(String username, String action) {

        Saunoja saunoja = saunojaRepository.findByUsername(username);

        if (action.equals("addAdmin")) {

            if (!saunoja.getRoles().contains("ADMIN")) {

                saunoja.getRoles().add("ADMIN");

                notificationService.newNotification(username, new NewNotification("Sinulla on nyt ADMIN tason käyttöoikeudet.", Boolean.FALSE));
            }
        }

        if (action.equals("removeAdmin")) {

            if (saunoja.getRoles().contains("ADMIN")) {

                saunoja.getRoles().remove("ADMIN");

                notificationService.newNotification(username, new NewNotification("Sinulta poistettiin ADMIN tason käyttöoikeudet.", Boolean.TRUE));
            }
        }

        if (action.equals("addGod")) {

            if (!saunoja.getRoles().contains("GOD")) {

                saunoja.getRoles().add("GOD");

                notificationService.newNotification(username, new NewNotification("Sinulla on nyt GOD tason käyttöoikeudet.", Boolean.FALSE));
            }
        }

        if (action.equals("removeGod")) {

            if (saunoja.getRoles().contains("GOD")) {

                saunoja.getRoles().remove("GOD");

                notificationService.newNotification(username, new NewNotification("Sinulta poistettiin GOD tason käyttöoikeudet.", Boolean.TRUE));
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

                notificationService.newNotification(username, new NewNotification("Sinut laitettiin avantoon.", Boolean.TRUE));
            }
        }

        if (action.equals("removeFrozen")) {

            if (saunoja.getRoles().contains("FROZEN")) {

                saunoja.getRoles().remove("FROZEN");

                notificationService.newNotification(username, new NewNotification("Sinut otettiin takaisin avannosta.", Boolean.FALSE));
            }
        }

        saunojaRepository.save(saunoja);
    }

    public void removeFreezeFromRoles(String username) {

        Saunoja saunoja = getByUsername(username);

        saunoja.getRoles().remove("FROZEN");

        saunojaRepository.save(saunoja);
    }

    @PreAuthorize("hasAuthority('GOD')")
    public void deleteSaunoja(String username) {

        Saunoja saunoja = saunojaRepository.findByUsername(username);

        if (saunoja != null) {

            saunoja.getRoles().clear();

            saunojaRepository.save(saunoja);

            saunojaRepository.delete(saunoja);

            deletedSaunojaRepository.save(new DeletedSaunoja(username, LocalDateTime.now()));
        }
    }

    public Boolean hasErrorsOnPasswordUpdate(NewPassword newPassword, BindingResult bindingResult) {

        String currentPassword = getCurrentSaunoja().getPassword();

        if (!passwordEncoder.matches(newPassword.getOldPassword(), currentPassword)) {

            bindingResult.rejectValue("oldPassword", "error.newPassword", "Salasana väärin.");
        }

        if (!newPassword.getNewPassword().equals(newPassword.getConfirmNewPassword())) {

            bindingResult.rejectValue("confirmNewPassword", "error.newPassword", "Salasanat eivät täsmää.");
        }

        return bindingResult.hasErrors();
    }

    public void updatePassword(NewPassword newPassword) {

        Saunoja saunoja = getCurrentSaunoja();

        saunoja.setPassword(passwordEncoder.encode(newPassword.getNewPassword()));

        saunojaRepository.save(saunoja);
    }

    public void createSomeSaunojas() throws IOException {

        if (Arrays.asList(environment.getActiveProfiles()).contains("dev")) {

            if (saunojaRepository.findAll().isEmpty()) {

                List<String> roles = new ArrayList<>();

                roles.add("USER");

                saunojaRepository.save(new Saunoja("landepaukku", passwordEncoder.encode("!Sauna5"), "Antti", "Isotalo", roles, LocalDateTime.now(), 1L, new ArrayList<Photo>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));

                saunojaRepository.save(new Saunoja("voikukka", passwordEncoder.encode("!Sauna5"), "Anniina", "Isotalo", roles, LocalDateTime.now(), 1L, new ArrayList<Photo>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));

                roles.add("ADMIN");

                saunojaRepository.save(new Saunoja("Saunatonttu", passwordEncoder.encode("!Sauna5"), "Sauna", "Tonttu", roles, LocalDateTime.now(), 1L, new ArrayList<Photo>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));

                photoService.addDefaultPhoto(getByUsername("landepaukku"));

                photoService.addDefaultPhoto(getByUsername("voikukka"));

                photoService.addDefaultPhoto(getByUsername("Saunatonttu"));
            }
        }

        createGod();
    }

    public void createGod() throws IOException {

        if (saunojaRepository.findAll().isEmpty()) {

            List<String> roles = new ArrayList<>();

            roles.add("USER");

            roles.add("ADMIN");

            roles.add("GOD");

            saunojaRepository.save(new Saunoja("Väinämöinen", passwordEncoder.encode("!Sauna5"), "Ariel", "Mörtengren", roles, LocalDateTime.now(), 1L, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));

            photoService.addDefaultPhoto(getByUsername("Väinämöinen"));
        }
    }

}
