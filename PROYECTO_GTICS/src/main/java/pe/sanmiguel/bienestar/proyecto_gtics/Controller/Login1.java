package pe.sanmiguel.bienestar.proyecto_gtics.Controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Usuario;
import pe.sanmiguel.bienestar.proyecto_gtics.Repository.UsuarioRepository;
import pe.sanmiguel.bienestar.proyecto_gtics.SHA256;
import pe.sanmiguel.bienestar.proyecto_gtics.ValidationGroup.LoginValidationsGroup;
import pe.sanmiguel.bienestar.proyecto_gtics.ValidationGroup.OptionalValidationsGroup;
import pe.sanmiguel.bienestar.proyecto_gtics.ValidationGroup.RegisterValidationsGroup;

import javax.imageio.spi.RegisterableService;


@Controller
//@RequestMapping("/login")
public class Login1 {
final UsuarioRepository usuarioRepository;

    public Login1(UsuarioRepository usuarioRepository) {this.usuarioRepository = usuarioRepository;}

    //@GetMapping("")
    //public String inicio() {return "login/login";}

    @GetMapping("/")

    public String login(@ModelAttribute("usuario")  Usuario usuario,RedirectAttributes attributes,
                        Model model) {
        model.addAttribute("usuario", usuario);

        return "index";
    }

    @GetMapping("/new")
    public String nuevoUsuario(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "login/prueba";
    }

  @PostMapping("/save")
   public String guardarNuevoUsuario(@ModelAttribute("usuario") @Validated(RegisterValidationsGroup.class) Usuario usuario, BindingResult bindingResult, RedirectAttributes attributes, Model model){


       if (!bindingResult.hasErrors()) {
           String contrasenaSinCifrar = usuario.getContrasena();
           String contrasenaHasheada = SHA256.cipherPassword(contrasenaSinCifrar);
           usuario.setContrasena(contrasenaHasheada);
           usuario.setRol(4);
           usuario.setEstadoUsuario(2);
           usuarioRepository.save(usuario);
           attributes.addFlashAttribute("mensaje", "Usuario creado correctamente");
           return "redirect:/";
       } else {
           model.addAttribute("usuario", usuario);

           return "login/prueba";
       }
  }

  @GetMapping("/recuperarContra")
    public String recuperarContra() {return "login/recuperarContrasena";}


    @PostMapping("/loguearse")
    public String login(@ModelAttribute("usuario") @Validated(LoginValidationsGroup.class) Usuario usuario,
                        BindingResult bindingResult,
                        RedirectAttributes attributes,
                        Model model) {

        Usuario usuarioExistente = usuarioRepository.findByCorreo(usuario.getCorreo());


        if (!bindingResult.hasErrors()) {
            if (usuarioExistente != null && SHA256.cipherPassword(usuario.getContrasena()).equals(usuarioExistente.getContrasena())) {
                // Si las credenciales son correctas, redirige a la página de inicio o al panel de control
                return "redirect:/paciente";
            } else {
                // Si las credenciales son incorrectas, redirige de vuelta al formulario de inicio de sesión con un mensaje de error
                attributes.addFlashAttribute("error", "Credenciales incorrectas");
                return "redirect:/";
            }
        } else {
           model.addAttribute("usuario", usuario);
            return "index";
        }

    }
}
