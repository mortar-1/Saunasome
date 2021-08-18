package projekti;

import java.time.LocalDateTime;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

@Service
class AccountFreezeService {

    @Autowired
    private AccountFreezeRepository accountFreezeRepository;

    @Autowired
    private SaunojaService saunojaService;

    @PreAuthorize("hasAuthority('GOD')")
    public void freezeAccount(String username, NewAccountFreeze newAccountFreeze) {
        
        if (Pattern.matches("[1-9]+", newAccountFreeze.getTimeToExpiration())) {
            
            return;
        }
        
        Integer timeToExpiration = Integer.valueOf(newAccountFreeze.getTimeToExpiration());
        
        String timeUnit = newAccountFreeze.getTimeUnit();
                
        AccountFreeze accountFreeze = new AccountFreeze();

        accountFreeze.setAuthor(saunojaService.getCurrentSaunoja());

        accountFreeze.setRecipient(saunojaService.getByUsername(username));

        accountFreeze.setCreated(LocalDateTime.now());

        if (timeUnit.equals("forever")) {

            accountFreeze.setExpires(null);

        } else {

            LocalDateTime expires = LocalDateTime.now();

            if (timeUnit != null && timeUnit.equals("min")) {
                expires = expires.plusMinutes(timeToExpiration);
            }

            if (timeUnit != null && timeUnit.equals("h")) {
                expires = expires.plusHours(timeToExpiration);
            }

            if (timeUnit != null && timeUnit.equals("d")) {
                expires = expires.plusDays(timeToExpiration);
            }

            accountFreeze.setExpires(expires);
        }

        saunojaService.authorize(username, "addFrozen");

        accountFreezeRepository.save(accountFreeze);
    }

    public Boolean hasErrorsOnCreation(NewAccountFreeze newAccountFreeze, BindingResult bindingResult) {

        if (newAccountFreeze.getTimeToExpiration().isBlank() && !newAccountFreeze.getTimeUnit().equalsIgnoreCase("forever")) {

            bindingResult.rejectValue("timeToExpiration", "error.newAccountFreeze", "Täsmennä luvulla avannossaolo aika.");
        }
        
        if (!Pattern.matches("[0-9]+", newAccountFreeze.getTimeToExpiration())) {
            
            bindingResult.rejectValue("timeToExpiration", "error.newAccountFreeze", "Syötä vain numeroita.");
        }

        return bindingResult.hasErrors();
    }

    @Transactional
    public void unFreezeAccount(String username) {

        accountFreezeRepository.deleteByRecipient(saunojaService.getByUsername(username));

        saunojaService.removeFreezeFromRoles(username);
    }

    public void checkIfFrozen(Model model) {

        Saunoja saunoja = saunojaService.getCurrentSaunoja();

        if (saunoja.getRoles().contains("FROZEN")) {

            AccountFreeze accountFreeze = accountFreezeRepository.findByRecipient(saunoja);

            model.addAttribute("accountFreeze", accountFreeze);

            if (accountFreeze != null && accountFreeze.getExpires() == null) {

                return;
            }

            if (accountFreeze != null && accountFreeze.getExpires().isBefore(LocalDateTime.now())) {

                unFreezeAccount(saunoja.getUsername());
            }
        }
    }

}
