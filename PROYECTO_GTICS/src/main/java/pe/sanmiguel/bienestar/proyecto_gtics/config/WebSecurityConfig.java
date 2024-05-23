package pe.sanmiguel.bienestar.proyecto_gtics.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration
public class WebSecurityConfig {

    final DataSource dataSource;

    public WebSecurityConfig(DataSource dataSource){
        this.dataSource = dataSource;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsManager users(DataSource dataSource){
        JdbcUserDetailsManager users = new JdbcUserDetailsManager(dataSource);
        String sq11 = "SELECT correo, contrasena, estado_usuario FROM usuario where correo = ?";
        String sq12 = "SELECT u.correo, r.nombre FROM proyecto_gtics.usuario u inner join rol r on (u.idRol = r.id) where u.correo = ? and u.estado_usuario = 1";

        users.setUsersByUsernameQuery(sq11);
        users.setAuthoritiesByUsernameQuery(sq12);
        return users;
    }

    @Bean
    public SecurityFilterChain filterChain (HttpSecurity http) throws Exception {

        http.authorizeHttpRequests()
                //Rutas para autenticación
                /*.requestMatchers("/superadmin", "/superadmin/**").authenticated()
                .requestMatchers("/adminsede", "/adminsede/**").authenticated()
                .requestMatchers("/farmacista", "/farmacista/**").authenticated()
                .requestMatchers("/paciente", "/paciente/**").authenticated()*/
                //Autenticación por rol
                .requestMatchers("/superadmin", "/superadmin/**").hasAnyAuthority("SUPERADMIN")
                .requestMatchers("/adminsede", "/adminsede/**").hasAnyAuthority("SUPERADMIN", "ADMINSEDE")
                .requestMatchers("/farmacista", "/farmacista/**").hasAnyAuthority("SUPERADMIN", "FARMACISTA")
                .requestMatchers("/paciente", "/paciente/**").hasAnyAuthority("SUPERADMIN", "PACIENTE")
                .anyRequest().permitAll();

        http.formLogin()
                .loginPage("/")
                .loginProcessingUrl("/processlogin")
                .successHandler(new AuthenticationSuccessHandler() {
                    @Override
                    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                        String rol = "";

                        for (GrantedAuthority role : authentication.getAuthorities()){
                            rol = role.getAuthority();
                            break;
                        }

                        if (rol.equals("SUPERADMIN")){
                            response.sendRedirect("/superadmin");
                        } else if (rol.equals("ADMINSEDE")) {
                            response.sendRedirect("/adminsede");
                            
                        } else if (rol.equals("FARMACISTA")) {
                            response.sendRedirect("/farmacista");
                        }else {
                            response.sendRedirect("/paciente");
                        }


                    }
                });

        return http.build();
    }

}
