package pe.sanmiguel.bienestar.proyecto_gtics.Controller;

import lombok.Getter;
import lombok.Setter;
import org.apache.http.HttpStatus;
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

    @PostMapping(value = {"/valida_dni", "/valida_dni/"})
    public ResponseEntity<HashMap<String, Object>> validarDNI(@RequestBody DniRequest dniRequest) {

        String dni = dniRequest.getDni();
        HashMap<String, Object> responseJson = new HashMap<>();

        try {
            Optional<Usuario> usuarioOptional = usuarioRepository.verifyDNI(dni);

            if (usuarioOptional.isPresent()){
                responseJson.put("OK", usuarioOptional.get());
                return ResponseEntity.status(HttpStatus.SC_OK).body(responseJson);
            } else {
                responseJson.put("ERROR", null);
                return ResponseEntity.badRequest().body(responseJson);
            }
        } catch (Exception e){
            responseJson.put("ERROR", null);
            return ResponseEntity.badRequest().body(responseJson);
        }
    }
}
