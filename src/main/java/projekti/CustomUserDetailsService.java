package projekti;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private SaunojaRepository saunojaRepository;

    @Autowired
    private SaunojaService saunojaService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Saunoja saunoja = saunojaRepository.findByUsername(username);

        if (saunoja == null) {
            
            throw new UsernameNotFoundException("No such user: " + username);
        }

        return new org.springframework.security.core.userdetails.User(
                saunoja.getUsername(),
                saunoja.getPassword(),
                true,
                true,
                true,
                true,
                saunojaService.rolesToGrantedAuthority(saunoja));
    }
}
