package pe.sanmiguel.bienestar.proyecto_gtics.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Usuario;
import pe.sanmiguel.bienestar.proyecto_gtics.Repository.UsuarioRepository;



@Controller
@RequestMapping("/login")
public class Login1 {
final UsuarioRepository usuarioRepository;

    public Login1(UsuarioRepository usuarioRepository) {this.usuarioRepository = usuarioRepository;}

    //@GetMapping("")
    //public String inicio() {return "login/login";}

   @GetMapping("/new")
   public String nuevoUsuario() {
     return "login/prueba";
   }

  @PostMapping("/save")
   public String guardarNuevoUsuario(@ModelAttribute Usuario usuario, RedirectAttributes attributes){
       usuario.setRol(3);
       usuario.setEstadoUsuario(2);

        usuarioRepository.save(usuario);
      return "redirect:/";
  }

  @GetMapping("/recuperarContra")
    public String recuperarContra() {return "login/recuperarContrasena";}


    @PostMapping("/loguearse")
    public String login(@RequestParam("correo") String correo,
                        @RequestParam("contrasena") String contrasena,
                        RedirectAttributes attributes) {

        // Buscar el usuario por correo electrónico en la base de datos
        Usuario usuario = usuarioRepository.findByCorreo(correo);

        // Verificar si se encontró un usuario y si la contraseña coincide
        if (usuario != null && usuario.getContrasena().equals(contrasena)) {
            // Si las credenciales son correctas, redirige a la página de inicio o al panel de control
            return "redirect:/paciente";
        } else {
            // Si las credenciales son incorrectas, redirige de vuelta al formulario de inicio de sesión con un mensaje de error
            attributes.addFlashAttribute("error", "Credenciales incorrectas");
            return "redirect:/login";
        }
    }
}
