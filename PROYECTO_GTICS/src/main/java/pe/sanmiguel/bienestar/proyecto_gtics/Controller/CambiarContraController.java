package pe.sanmiguel.bienestar.proyecto_gtics.Controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.sanmiguel.bienestar.proyecto_gtics.Repository.UsuarioRepository;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Usuario;
import pe.sanmiguel.bienestar.proyecto_gtics.SHA256;
import pe.sanmiguel.bienestar.proyecto_gtics.ValidationGroup.CambioContraGroup;
import pe.sanmiguel.bienestar.proyecto_gtics.ValidationGroup.LoginValidationsGroup;
import pe.sanmiguel.bienestar.proyecto_gtics.PasswordService;
import pe.sanmiguel.bienestar.proyecto_gtics.ValidationGroup.RegisterValidationsGroup;

import java.io.IOException;


@Controller
//@RequestMapping(value = "/cambiarcontra")
public class CambiarContraController {

    final UsuarioRepository usuarioRepository;
    private final PasswordService passwordService;
    Usuario usuarioSession;


    public CambiarContraController(UsuarioRepository usuarioRepository, PasswordService passwordService) {this.usuarioRepository = usuarioRepository;
        this.passwordService = passwordService;
    }

    @GetMapping("/cambiarcontra")
    public String login(Model model,  HttpServletRequest request) {
        if (!model.containsAttribute("usuario")) {
            model.addAttribute("usuario", new Usuario());
        }
        return "login/cambiarcontra";
    }

    @PostMapping("/cambiarcontra/actualizar_contrasena")
    public String actualizarContrasena(@RequestParam(value = "newContrasena", required = true) String newContrasena,
                                       @RequestParam(value = "confirmContrasena", required = true) String confirmContrasena,
                                       RedirectAttributes attr, Model model,
                                       HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        HttpSession session = request.getSession();
        usuarioSession = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuarioSession);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);
        String encodedPassword = passwordEncoder.encode(newContrasena);

            if (newContrasena.equals(confirmContrasena)){
                    usuarioRepository.actualizarContrasenaUsuario(encodedPassword, usuarioSession.getIdUsuario());
                    usuarioRepository.actualizarRol(4, usuarioSession.getIdUsuario());
                    usuarioRepository.actualizarEstadoContra(1,usuarioSession.getIdUsuario());
                    attr.addFlashAttribute("msgSuccess", "Contraseña actualizada correctamente.");
            } else {
                attr.addFlashAttribute("msg", "Las contraseñas no coinciden.");
            }

        return "redirect:/";
    }


    @PostMapping("/cambiarcontra/actualizar")
    public String cambiarContra(@ModelAttribute("usuario") @Validated(CambioContraGroup.class) Usuario usuario,
                                BindingResult bindingResult,
                                RedirectAttributes attributes, HttpServletRequest request, HttpServletResponse response,
                                Model model, Authentication authentication) {

        HttpSession session = request.getSession();
        usuarioSession = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuarioSession);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);
        String encodedPassword = passwordEncoder.encode(usuarioSession.getContrasena());

        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Error en el formulario");
            return "login/cambiarcontra";
        }

        usuarioRepository.actualizarContrasenaUsuario(passwordEncoder.encode(usuario.getContrasena()), usuarioSession.getIdUsuario());
        usuarioRepository.actualizarRol(4, usuarioSession.getIdUsuario());
        usuarioRepository.actualizarEstadoContra(1,usuarioSession.getIdUsuario());


        // Agregar un mensaje de éxito
        attributes.addFlashAttribute("mensaje", "Contraseña cambiada exitosamente");

        return "redirect:/";
    }

}
