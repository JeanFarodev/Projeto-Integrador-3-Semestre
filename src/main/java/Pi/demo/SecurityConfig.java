package Pi.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Ativa o suporte ao CSRF porque o seu JS já envia o Token no cabeçalho certinho!
            .csrf(csrf -> csrf.ignoringRequestMatchers("/login", "/logout")) 
            .authorizeHttpRequests(auth -> auth
                // Permite o acesso livre para as páginas e recursos essenciais
                .requestMatchers("/css/**","/img/**", "/js/**", "/images/**", "/login", "/cadastro", "/registrar","/webjars/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/caixa/dashboard", true)
                .permitAll()
            )
            .logout(logout -> logout
            .logoutUrl("/logout") 
            .logoutSuccessUrl("/login?logout") 
            .invalidateHttpSession(true) 
            .clearAuthentication(true) 
            .permitAll()
        );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}