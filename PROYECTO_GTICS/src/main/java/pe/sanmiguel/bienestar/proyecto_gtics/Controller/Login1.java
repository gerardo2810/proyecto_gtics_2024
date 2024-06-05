package pe.sanmiguel.bienestar.proyecto_gtics.Controller;

import jakarta.mail.MessagingException;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.sanmiguel.bienestar.proyecto_gtics.EmailService;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Usuario;
import pe.sanmiguel.bienestar.proyecto_gtics.PasswordService;
import pe.sanmiguel.bienestar.proyecto_gtics.Repository.UsuarioRepository;
import pe.sanmiguel.bienestar.proyecto_gtics.SHA256;
import pe.sanmiguel.bienestar.proyecto_gtics.ValidationGroup.LoginValidationsGroup;
import pe.sanmiguel.bienestar.proyecto_gtics.ValidationGroup.RegisterValidationsGroup;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;


@Controller
//@RequestMapping("/login")
public class Login1 {
final UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordService passwordService;

    public Login1(UsuarioRepository usuarioRepository) {this.usuarioRepository = usuarioRepository;}

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
        return "login/prueba";
    }
    @Autowired
    private EmailService emailService;

    @PostMapping("/save")
    public String guardarNuevoUsuario(@ModelAttribute("usuario") @Validated(RegisterValidationsGroup.class) Usuario usuario,
                                      BindingResult bindingResult, RedirectAttributes attributes, Model model) {

        if (!bindingResult.hasErrors()) {
            usuario.setRol(4);
            usuario.setEstadoUsuario(2);
            String temporaryPassword = passwordService.generateTemporaryPassword();
            usuario.setContrasena(temporaryPassword);
            usuario.setEstadoContra(2);
            usuarioRepository.save(usuario);


            // Enviar el correo de bienvenida
            String subject = "Bienvenido(a) a Bienestar San Miguel";
            Map<String, Object> variables = new HashMap<>();
            Map<String, Object> variables2 = new HashMap<>();

            variables.put("nombre", usuario.getNombres());
            variables2.put("contra", usuario.getContrasena());

            //String filename="src/main/resources/templates/login/correo.html";
            //Path pathToFile = Paths.get(filename);
            //System.out.println(pathToFile.toAbsolutePath());
            //String htmlFilePath = pathToFile.toAbsolutePath().toString();


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
            return "login/prueba";
        }
    }

  @GetMapping("/recuperarContra")
    public String recuperarContra() {return "login/recuperarContrasena";}

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "/login/error"; // Nombre de la vista que se debe mostrar
    }

    @PostMapping("/cambiarcontra")
    public String cambiarContra(@ModelAttribute("usuario") @Validated(LoginValidationsGroup.class) Usuario usuario,
                                BindingResult bindingResult,
                                RedirectAttributes attributes,
                                Model model) {

        Usuario usuarioExistente = usuarioRepository.findByCorreo(usuario.getCorreo());







    }


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
                //} else if (usuarioExistente.getRol()==2) {
                 //   return "redirect:/adminsede";
                } else if (usuarioExistente.getRol()==3) {
                    return "redirect:/farmacista";
                } else {
                    return "redirect:/paciente";
                }
            } else if (usuarioExistente != null &&  usuario.getContrasena().equals(usuarioExistente.getContrasena())){
                // Si las credenciales son incorrectas, redirige de vuelta al formulario de inicio de sesión con un mensaje de error
                return "redirect:/cambiarcontra";
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