package projekti;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("production")
@EnableGlobalMethodSecurity(securedEnabled = true, proxyTargetClass = true)
@EnableWebSecurity
public class ProductionSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers("/logo.png").permitAll()
                .antMatchers("/css/**").permitAll()
                .antMatchers("/javascript/**").permitAll()
                .antMatchers("/main").permitAll()
                .antMatchers("/register").permitAll()
                .antMatchers("/notifications").hasAuthority("ADMIN")
                .antMatchers(HttpMethod.POST).authenticated()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/main")
                .loginProcessingUrl("/main")
                .defaultSuccessUrl("/wall", true)
                .failureUrl("/main?error=true")
                .and()
                .logout()
                .logoutSuccessUrl("/main?logout=true");
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {

        auth.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder());

    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        
        return new BCryptPasswordEncoder();
    }

}
