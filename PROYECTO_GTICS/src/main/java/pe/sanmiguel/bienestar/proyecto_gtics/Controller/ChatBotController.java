package pe.sanmiguel.bienestar.proyecto_gtics.Controller;

import lombok.Getter;
import lombok.Setter;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Medicamento;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Usuario;
import pe.sanmiguel.bienestar.proyecto_gtics.Repository.MedicamentoRepository;
import pe.sanmiguel.bienestar.proyecto_gtics.Repository.UsuarioRepository;

import java.math.BigDecimal;
import java.util.*;


@RestController
@CrossOrigin /* Por si se desea utilizar de IPs externas */
@RequestMapping("/chatbot_gtics")
public class ChatBotController {

    @Getter
    @Setter
    public static class DniRequest {
        private String dni;
    }

    @Getter
    @Setter
    public class ItemsRequest {
        private Map<String, String> items;
    }

    @Getter
    @Setter
    public static class MedBrief {
        private Integer idMedicamento;
        private String nombre;
        private String categoria;
        private String descripcion;
        private BigDecimal precioVenta;
        private String recetable;
        private Integer estado;

        public MedBrief(Integer idMedicamento, String nombre, String categoria, String descripcion, BigDecimal precioVenta, String recetable, Integer estado) {
            this.idMedicamento = idMedicamento;
            this.nombre = nombre;
            this.categoria = categoria;
            this.descripcion = descripcion;
            this.precioVenta = precioVenta;
            this.recetable = recetable;
            this.estado = estado;
        }
    }

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MedicamentoRepository medicamentoRepository;

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

    @PostMapping("/receiveItems")
    public String receiveItems(@RequestBody Map<String, String> items) {
        // Procesar los datos recibidos
        items.forEach((key, value) -> {
            String med = null;
            String cant = null;

            // Parsear el valor de 'itemX' como un objeto 'Item'
            String[] parts = value.split(",");
            for (String part : parts) {
                String[] pair = part.split(":");
                if (pair.length == 2) {
                    String field = pair[0].trim().replace("'", "");
                    String fieldValue = pair[1].trim().replace("'", "");
                    if (field.equals("med")) {
                        med = fieldValue;
                    } else if (field.equals("cant")) {
                        cant = fieldValue;
                    }
                }
            }

            System.out.println("Key: " + key + ", Med: " + med + ", Cant: " + cant);
        });

        return "Items received successfully!";
    }


    @GetMapping(value = {"/obtain_meds", "/obtain_meds/"})
    public ResponseEntity<HashMap<String, Object>> obtainMeds() {

        HashMap<String, Object> responseJson = new HashMap<>();

        try {
            List<Medicamento> listaMedicamentos = medicamentoRepository.findAll();
            List<MedBrief> listMedBrief = new ArrayList<>();

            for (Medicamento med : listaMedicamentos) {
                MedBrief newMedBrief = new MedBrief(med.getIdMedicamento(), med.getNombre(), med.getCategorias(), med.getDescripcion(), med.getPrecioVenta(), med.getRecetable(), med.getEstado());
                listMedBrief.add(newMedBrief);
            }


            if (!listaMedicamentos.isEmpty()){
                responseJson.put("MedList", listMedBrief);
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
