package pe.sanmiguel.bienestar.proyecto_gtics.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
     return "login/registro";
   }

  @PostMapping("/save")
   public String guardarNuevoUsuario(Usuario usuario, RedirectAttributes attributes){
       usuarioRepository.save(usuario);
      return "redirect:/index";
  }

  @GetMapping("/recuperarContra")
    public String recuperarContra() {return "login/recuperarContrasena";}



}
