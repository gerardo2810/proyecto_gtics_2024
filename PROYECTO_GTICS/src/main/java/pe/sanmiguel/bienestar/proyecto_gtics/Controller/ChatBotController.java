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
import java.util.stream.Collectors;


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



    @PostMapping(value = {"/valida_orden", "/valida_orden/"})
    public ResponseEntity<HashMap<String, Object>> validateOrden(@RequestBody Map<String, String> items) {

        HashMap<String, Object> responseJson = new HashMap<>();

        try {
            List<String> listaSelectedMeds = new ArrayList<>();
            items.forEach((key, value) -> {
                String med = null;
                String cant = null;

                // Parsear el valor de 'itemX' como un objeto 'Item'
                String[] parts = value.split(" - ");
                for (String part : parts) {
                    String[] pair = part.split(":");
                    if (pair.length == 2) {
                        String field = pair[0].trim().replace("'", "");
                        String fieldValue = pair[1].trim().replace("'", "");
                        if (field.equals("med")) {
                            med = fieldValue;
                            listaSelectedMeds.add(med);
                        } else if (field.equals("cant")) {
                            cant = fieldValue;
                            if (med != null){
                                listaSelectedMeds.add(cant);
                            }
                        }

                    }
                }
                System.out.println("Key: " + key + ", Med: " + med + ", Cant: " + cant);
            });

            if (!listaSelectedMeds.isEmpty()){

                List<Medicamento> MedsSeleccionados = getMedicamentosFromLista(listaSelectedMeds);
                List<String> cantidadesMeds = getCantidadesFromLista(listaSelectedMeds);
                double precioTotal = 0.0;

                StringBuilder ordenCompletaBuilder = new StringBuilder("Su orden es:\\n");
                int cantMeds = MedsSeleccionados.size();

                for (int i = 0; i<cantMeds; i++){

                    String eachNombre = MedsSeleccionados.get(i).getNombre();
                    String eachCantidad = cantidadesMeds.get(i);
                    BigDecimal eachPrecio = MedsSeleccionados.get(i).getPrecioVenta();

                    ordenCompletaBuilder.append(eachNombre)
                            .append(" - ")
                            .append(eachCantidad)
                            .append(" - ")
                            .append(eachPrecio)
                            .append("\\n");

                    BigDecimal subtotal = eachPrecio.multiply(new BigDecimal(eachCantidad));
                    precioTotal += subtotal.doubleValue();
                }

                ordenCompletaBuilder.append("\\n Precio total: ").append(precioTotal);

                String ordenCompleta = ordenCompletaBuilder.toString();

                responseJson.put("Orden", ordenCompleta);
                return ResponseEntity.status(HttpStatus.SC_OK).body(responseJson);

            } else {
                responseJson.put("ERROR", null);
                return ResponseEntity.badRequest().body(responseJson);
            }
        } catch (Exception e){
            responseJson.put("ERROR", null);
            return ResponseEntity.badRequest().body(responseJson);
        }

        // Procesar los datos recibidos

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


    @GetMapping(value = {"/inicio_orden", "/inicio_orden/"})
    public ResponseEntity<HashMap<String, Object>>  initOrden(@RequestHeader(value = "ID") String HeaderID) {

        HashMap<String, Object> responseJson = new HashMap<>();

        // Iniciar las variables de items
        Map<String, String> item1 = Map.of(
                "item2", "null",
                "item3", "null",
                "item4", "null",
                "item5", "null",
                "item6", "null",
                "item7", "null",
                "item8", "null",
                "item9", "null",
                "item10", "null"
        );

        Map<String, String> item2 = Map.of(
                "item3", "null",
                "item4", "null",
                "item5", "null",
                "item6", "null",
                "item7", "null",
                "item8", "null",
                "item9", "null",
                "item10", "null"
        );

        Map<String, String> item3 = Map.of(
                "item4", "null",
                "item5", "null",
                "item6", "null",
                "item7", "null",
                "item8", "null",
                "item9", "null",
                "item10", "null"
        );

        Map<String, String> item4 = Map.of(
                "item5", "null",
                "item6", "null",
                "item7", "null",
                "item8", "null",
                "item9", "null",
                "item10", "null"
        );

        Map<String, String> item5 = Map.of(
                "item6", "null",
                "item7", "null",
                "item8", "null",
                "item9", "null",
                "item10", "null"
        );

        Map<String, String> item6 = Map.of(
                "item7", "null",
                "item8", "null",
                "item9", "null",
                "item10", "null"
        );

        Map<String, String> item7 = Map.of(
                "item8", "null",
                "item9", "null",
                "item10", "null"
        );

        Map<String, String> item8 = Map.of(
                "item9", "null",
                "item10", "null"
        );

        Map<String, String> item9 = Map.of(
                "item10", "null"
        );

        switch (HeaderID) {

            case "1":
                responseJson.put("OK", item1);
                return ResponseEntity.status(HttpStatus.SC_OK).body(responseJson);
            case "2":
                responseJson.put("OK", item2);
                return ResponseEntity.status(HttpStatus.SC_OK).body(responseJson);
            case "3":
                responseJson.put("OK", item3);
                return ResponseEntity.status(HttpStatus.SC_OK).body(responseJson);
            case "4":
                responseJson.put("OK", item4);
                return ResponseEntity.status(HttpStatus.SC_OK).body(responseJson);
            case "5":
                responseJson.put("OK", item5);
                return ResponseEntity.status(HttpStatus.SC_OK).body(responseJson);
            case "6":
                responseJson.put("OK", item6);
                return ResponseEntity.status(HttpStatus.SC_OK).body(responseJson);
            case "7":
                responseJson.put("OK", item7);
                return ResponseEntity.status(HttpStatus.SC_OK).body(responseJson);
            case "8":
                responseJson.put("OK", item8);
                return ResponseEntity.status(HttpStatus.SC_OK).body(responseJson);
            case "9":
                responseJson.put("OK", item9);
                return ResponseEntity.status(HttpStatus.SC_OK).body(responseJson);
            default:
                responseJson.put("ERROR", null);
                return ResponseEntity.badRequest().body(responseJson);

        }
    }



    public List<Medicamento> getMedicamentosFromLista(List<String> listaSelectedIds) {
        List<Optional<Medicamento>> optionals = new ArrayList<>();
        List<Medicamento> seleccionados;
        for (int i = 0; i < listaSelectedIds.size(); i += 2) {
            optionals.add(medicamentoRepository.findById(Integer.valueOf(listaSelectedIds.get(i))));
        }
        seleccionados = optionals.stream().flatMap(Optional::stream).collect(Collectors.toList());

        return seleccionados;
    }

    public List<String> getCantidadesFromLista(List<String> listaSelectedIds) {
        List<String> cantidades = new ArrayList<>();
        for (int i = 0; i + 1 < listaSelectedIds.size(); i += 2) {
            cantidades.add(listaSelectedIds.get(i + 1));
        }
        return cantidades;
    }

}
