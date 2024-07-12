package pe.sanmiguel.bienestar.proyecto_gtics.Controller;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Usuario;
import pe.sanmiguel.bienestar.proyecto_gtics.Repository.UsuarioRepository;

import java.util.HashMap;
import java.util.Optional;

@RestController
@CrossOrigin /* Por si se desea utilizar de IPs externas */
@RequestMapping("/chatbot_gtics")
public class ChatBotController {

    @Getter
    @Setter
    public static class DniRequest {
        private String dni;
    }

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping(value = "/valida_dni")
    public ResponseEntity<Object> validarDni(@RequestBody DniRequest dniRequest) {
        try {
            String dni = dniRequest.getDni();
            Usuario usuario = usuarioRepository.verifyDNI(dni).orElse(null);

            if (usuario != null) {
                return ResponseEntity.ok(usuario);
            } else {
                return ResponseEntity.ok("Usuario no encontrado");
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Error en el formato del DNI");
        }
    }
}
