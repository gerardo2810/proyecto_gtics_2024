package pe.sanmiguel.bienestar.proyecto_gtics.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Usuario;
import pe.sanmiguel.bienestar.proyecto_gtics.ValidationGroup.RegisterValidationsGroup;


@Controller
public class LoginController {

    //@GetMapping("/")

    public String login(@ModelAttribute("usuario")  Usuario usuario,RedirectAttributes attributes,
                        Model model) {
        model.addAttribute("usuario", usuario);

        return "index";
    }

}
