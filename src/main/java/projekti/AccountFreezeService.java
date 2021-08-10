package projekti;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

@Service
class AccountFreezeService {

    @Autowired
    private AccountFreezeRepository accountFreezeRepository;

    @Autowired
    private SaunojaService saunojaService;

    @PreAuthorize("hasAuthority('GOD')")
    public void freezeAccount(String username, Integer timeToExpiration, String timeUnit) {

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
