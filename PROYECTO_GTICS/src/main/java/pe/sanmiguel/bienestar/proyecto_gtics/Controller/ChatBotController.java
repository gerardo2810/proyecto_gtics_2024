package pe.sanmiguel.bienestar.proyecto_gtics.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Usuario;
import pe.sanmiguel.bienestar.proyecto_gtics.Repository.UsuarioRepository;

import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/chatbot_gtics")
public class ChatBotController {

    private static final String SECURITY_TOKEN = "acceso123ah";

    public boolean isValidToken(String token) {
        return SECURITY_TOKEN.equals(token);
    }

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping(value = "/valida_dni/{dni}")
    public ResponseEntity<HashMap<String, Object>> validaDni(@RequestHeader("Authorization") String token, @PathVariable("dni") String dni)  {
        if (!isValidToken(token)) {
            return ResponseEntity.status(403).body(null); // Forbidden if token is not valid
        }

        try {
            Optional<Usuario> userDNI = usuarioRepository.verifyDNI(dni);

            HashMap<String, Object> respuesta = new HashMap<>();

            if (userDNI.isPresent()) {
                respuesta.put("result", "ok");
                respuesta.put("usuario", userDNI.get());
            } else {
                respuesta.put("result", "no existe");
            }
            return ResponseEntity.ok(respuesta);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}