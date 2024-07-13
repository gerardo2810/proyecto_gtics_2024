package pe.sanmiguel.bienestar.proyecto_gtics.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Sede;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Usuario;
import pe.sanmiguel.bienestar.proyecto_gtics.Repository.SedeRepository;
import pe.sanmiguel.bienestar.proyecto_gtics.Repository.UsuarioRepository;

@Controller
public class ImpersonationController {

    @Autowired
    private UserDetailsManager userDetailsManager;

    final UsuarioRepository usuarioRepository;
    final SedeRepository sedeRepository;

    public ImpersonationController(UsuarioRepository usuarioRepository, SedeRepository sedeRepository) {
        this.usuarioRepository = usuarioRepository;
        this.sedeRepository = sedeRepository;
    }

    @GetMapping("/impersonate")
    public String impersonateUser(@RequestParam("username") String username, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        // Ensure the current user has the role of SUPERADMIN
        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("SUPERADMIN"))) {

            UserDetails userDetails = userDetailsManager.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            // Set the new authentication context
            SecurityContext securityContext = SecurityContextHolder.getContext();
            securityContext.setAuthentication(authentication);

            // Close the current session
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }

            SecurityContextHolder.getContext().setAuthentication(authentication);
            System.out.println("Autenticacion: " + authentication.getName());

            // Create a new session
            HttpSession newSession = request.getSession(true);

            // Set the new security context in the new session
            newSession.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

            // Setting the session for the new user
            Usuario usuario = usuarioRepository.findByCorreo(username);
            newSession.setAttribute("usuario", usuario);

            Sede sedeSession = sedeRepository.sedeAdminID(usuario.getIdUsuario());
            if (sedeSession != null) {
                newSession.setAttribute("idSede", sedeSession.getIdSede());
            }

            switch (usuario.getRol()) {
                case 1:
                    usuarioRepository.actualizarEstadoLogueoSuperadmin(usuario.getIdUsuario());
                    return "redirect:/superadmin";
                case 2:
                    usuarioRepository.actualizarEstadoLogueo(usuario.getIdUsuario());
                    return "redirect:/adminsede";
                case 3:
                    usuarioRepository.actualizarEstadoLogueo(usuario.getIdUsuario());
                    return "redirect:/farmacista";
                case 4:
                    usuarioRepository.actualizarEstadoLogueo(usuario.getIdUsuario());
                    return "redirect:/paciente";
                default:
                    return "redirect:/";
            }
        }
        redirectAttributes.addFlashAttribute("error", "No tienes permiso para suplantar usuarios.");
        return "redirect:/";
    }
}
