package pe.sanmiguel.bienestar.proyecto_gtics.Controller;

import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.sanmiguel.bienestar.proyecto_gtics.DniAPI;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DniController {

    private final DniAPI dniAPI;

    @Autowired
    public DniController(DniAPI dniAPI) {
        this.dniAPI = dniAPI;
    }

    @GetMapping("/dni")
    public List<String> getDniInfo(@RequestParam String dni) {
        return dniAPI.getDni(dni);
    }
}