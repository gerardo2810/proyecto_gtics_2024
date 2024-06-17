package pe.sanmiguel.bienestar.proyecto_gtics.Controller;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.sanmiguel.bienestar.proyecto_gtics.DniAPI;
import pe.sanmiguel.bienestar.proyecto_gtics.EmailService;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Doctor;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Medicamento;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Usuario;
import pe.sanmiguel.bienestar.proyecto_gtics.PasswordService;
import pe.sanmiguel.bienestar.proyecto_gtics.Repository.UsuarioRepository;
import pe.sanmiguel.bienestar.proyecto_gtics.SHA256;
import pe.sanmiguel.bienestar.proyecto_gtics.ValidationGroup.DniApiValidationGroup;
import pe.sanmiguel.bienestar.proyecto_gtics.ValidationGroup.LoginValidationsGroup;
import pe.sanmiguel.bienestar.proyecto_gtics.ValidationGroup.RegisterValidationsGroup;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
//@RequestMapping("/login")
public class Login1 {
final UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private DniAPI dniAPI;

    public Login1(UsuarioRepository usuarioRepository, DniAPI dniAPI) {
        this.dniAPI = dniAPI;
        this.usuarioRepository = usuarioRepository;}

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);


    @GetMapping("/")
    public String login(Model model,  HttpServletRequest request) {
        if (!model.containsAttribute("usuario")) {
            model.addAttribute("usuario", new Usuario());
        }
        Object logoutMessage = request.getSession().getAttribute("logoutMessage");
        if (logoutMessage != null) {
            logger.info("Logout message found in session: " + logoutMessage);
            model.addAttribute("logoutMessage", logoutMessage);
            request.getSession().removeAttribute("logoutMessage");
        } else {
            logger.info("No logout message found in session.");
        }
        return "index";
    }

    @GetMapping("/new")
    public String nuevoUsuario(Model model,@ModelAttribute("usuario")  Usuario usuario) {
        model.addAttribute("usuario", new Usuario());

        return "login/prueba3";
    }
    @Autowired
    private EmailService emailService;


    @PostMapping("/DNIapi")
    public String dniApi(@ModelAttribute("usuario") @Validated(DniApiValidationGroup.class) Usuario usuario,
                         BindingResult bindingResult,
                         Model model,
                         @RequestParam(value = "dni") String dni) {

        if (bindingResult.hasErrors()) {
            System.out.println(bindingResult.getAllErrors());
            return "login/prueba3";
        } else {
            ResponseEntity<String> responseDni = DniAPI.getDni(dni);
            List<String> values = DniAPI.responseToList(responseDni);

            String apiDni = values.get(4);
            String apiNombres = values.get(0);
            String apiApellidos = (values.get(1) + " " + values.get(2));

            model.addAttribute("dni", apiDni);
            model.addAttribute("nombres", apiNombres);
            model.addAttribute("apellidos", apiApellidos);
            model.addAttribute("dniApi", apiDni);

            System.out.println(values);
            return "login/prueba3";
        }
    }

    @PostMapping("/save")
    public String guardarNuevoUsuario(@ModelAttribute("usuario") @Validated(RegisterValidationsGroup.class) Usuario usuario,
                                      BindingResult bindingResult, RedirectAttributes attributes, Model model) {

        if (!bindingResult.hasErrors()) {
            usuario.setRol(5);
            usuario.setEstadoUsuario(1);
            String temporaryPassword = passwordService.generateTemporaryPassword();
            usuario.setEstadoContra(2);
            System.out.println(temporaryPassword);
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);
            String encodedPassword = passwordEncoder.encode(temporaryPassword);
            usuario.setContrasena(encodedPassword);
            System.out.println(encodedPassword);
            usuarioRepository.save(usuario);
            System.out.println(temporaryPassword);
            System.out.println(encodedPassword);

            // Enviar el correo de bienvenida
            String subject = "Bienvenido(a) a Bienestar San Miguel";
            Map<String, Object> variables = new HashMap<>();
            Map<String, Object> variables2 = new HashMap<>();

            variables.put("nombre", usuario.getNombres());
            variables2.put("contra", temporaryPassword);

            try {
                emailService.sendHtmlEmail2(usuario.getCorreo(), subject,"login/correo", variables, variables2);
                attributes.addFlashAttribute("mensaje", "Usuario creado correctamente y correo enviado.");
                return "redirect:/";
            } catch (MessagingException e) {
                e.printStackTrace();
                attributes.addFlashAttribute("mensaje", "Usuario creado, pero hubo un error al enviar el correo: " + e.getMessage());
            }

            return "redirect:/";
        } else {
            model.addAttribute("usuario", usuario);
            return "login/prueba3";
        }
    }

  @GetMapping("/recuperarContra")
    public String recuperarContra() {return "login/recuperarContrasena";}

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "login/error"; // Nombre de la vista que se debe mostrar
    }





   /* @PostMapping("/cambiarcontra")
    public String cambiarContra(@ModelAttribute("usuario") @Validated(LoginValidationsGroup.class) Usuario usuario,
                                BindingResult bindingResult,
                                @RequestParam("contrasenaNueva") String contrasenaNueva,
                                RedirectAttributes attributes,
                                Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Error en el formulario");
            return "/cambiarcontra";
        }

        Usuario usuarioExistente = usuarioRepository.findByCorreo(usuario.getCorreo());

        if (usuarioExistente == null) {
            attributes.addFlashAttribute("error", "Usuario no encontrado");
            return "redirect:/";
        }

        // Validar la nueva contraseña
        if (contrasenaNueva == null) {
            attributes.addFlashAttribute("error", "La contraseña debe tener al menos 6 caracteres");
            return "redirect:/cambiarcontra";
        }

        // Encriptar la nueva contraseña
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(contrasenaNueva);
        usuarioExistente.setContrasena(encodedPassword);

        usuarioExistente.setRol(5);
        usuarioExistente.setEstadoUsuario(1);
        usuario.setEstadoContra(1);

        // Guardar los cambios en la base de datos
        usuarioRepository.save(usuarioExistente);

        // Agregar un mensaje de éxito
        attributes.addFlashAttribute("success", "Contraseña cambiada exitosamente");

        return "redirect:/";
    } */



    @PostMapping("/loguearse")
    public String login(@ModelAttribute("usuario") @Validated(LoginValidationsGroup.class) Usuario usuario,
                        BindingResult bindingResult,
                        RedirectAttributes attributes,
                        Model model) {

        Usuario usuarioExistente = usuarioRepository.findByCorreo(usuario.getCorreo());


        if (!bindingResult.hasErrors()) {
            if (usuarioExistente != null && SHA256.cipherPassword(usuario.getContrasena()).equals(usuarioExistente.getContrasena())) {
                if (usuarioExistente.getRol() == 1) {
                    return "redirect:/superadmin";
                } else if (usuarioExistente.getRol()==2) {
                    return "redirect:/adminsede";
                } else if (usuarioExistente.getRol()==3) {
                    return "redirect:/farmacista";
                } else {
                    return "redirect:/paciente";
                }
            } else if (usuarioExistente != null &&  usuario.getContrasena().equals(usuarioExistente.getContrasena())){
                // Si las credenciales son incorrectas, redirige de vuelta al formulario de inicio de sesión con un mensaje de error
                return "redirect:login/cambiarcontra";
            }else{
                attributes.addFlashAttribute("error", "Credenciales incorrectas");
                return "redirect:/paciente";
            }
        } else {
           model.addAttribute("usuario", usuario);
           if (usuarioExistente.getEstadoUsuario() == 2) {
               attributes.addFlashAttribute("error", "Usuario a falta de aprobación");
           }
            return "index";
        }

    }
}

//@GetMapping("")
//public String inicio() {return "login/login";}

    /*@GetMapping("/")

    public String login(@ModelAttribute("usuario")  Usuario usuario,RedirectAttributes attributes,
                        Model model) {
        model.addAttribute("usuario", usuario);

        return "index";
    }*/