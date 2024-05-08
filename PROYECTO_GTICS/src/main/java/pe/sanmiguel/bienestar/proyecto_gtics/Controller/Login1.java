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

    @GetMapping("")
    public String inicio() {return "login/login";}

   @GetMapping("/new")
   public String nuevoUsuario() {
     return "login/prueba";
   }

  @PostMapping("/save")
   public String guardarNuevoUsuario(@ModelAttribute Usuario usuario, RedirectAttributes attributes){
       usuario.setRol(3);
       usuario.setEstadoUsuario(1);

        usuarioRepository.save(usuario);
      return "redirect:/";
  }

  @GetMapping("/recuperarContra")
    public String recuperarContra() {return "login/recuperarContrasena";}



}
