package pe.sanmiguel.bienestar.proyecto_gtics.Controller;

import lombok.Getter;
import lombok.Setter;
import org.apache.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.*;
import pe.sanmiguel.bienestar.proyecto_gtics.Repository.*;
import pe.sanmiguel.bienestar.proyecto_gtics.TokenRandomGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@CrossOrigin /* Por si se desea utilizar de IPs externas */
@RequestMapping("/chatbot_gtics")
public class ChatBotController {

    public ChatBotController(UsuarioRepository usuarioRepository, SedeStockRepository sedeStockRepository, MedicamentoRepository medicamentoRepository, OrdenRepository ordenRepository, OrdenContenidoRepository ordenContenidoRepository, SedeRepository sedeRepository, SedeFarmacistaRepository sedeFarmacistaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.sedeStockRepository = sedeStockRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.ordenRepository = ordenRepository;
        this.ordenContenidoRepository = ordenContenidoRepository;
        this.sedeRepository = sedeRepository;
        this.sedeFarmacistaRepository = sedeFarmacistaRepository;
    }

    @Getter
    @Setter
    public static class DniRequest {
        private String dni;
    }

    @Getter
    @Setter
    public static class ItemsRequest {
        private Map<String, String> items;
    }

    @Getter
    @Setter
    public static class GenerateRequest {
        private Map<String, String> items;
        private String idPaciente;
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

    final UsuarioRepository usuarioRepository;
    final SedeStockRepository sedeStockRepository;
    final MedicamentoRepository medicamentoRepository;
    final OrdenRepository ordenRepository;
    final OrdenContenidoRepository ordenContenidoRepository;

    final SedeRepository sedeRepository;
    final SedeFarmacistaRepository sedeFarmacistaRepository;


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
            List<String> listaSelectedMeds = parseJSONMessage(items);

            if (!listaSelectedMeds.isEmpty()){

                List<Medicamento> MedsSeleccionados = getMedicamentosFromLista(listaSelectedMeds);
                List<String> cantidadesMeds = getCantidadesFromLista(listaSelectedMeds);

                StringBuilder ordenCompletaBuilder = new StringBuilder("Su orden es: \n");
                double precioTotal = 0.0;
                int cantMeds = MedsSeleccionados.size();

                for (int i = 0; i<cantMeds; i++){

                    String eachNombre = MedsSeleccionados.get(i).getNombre();
                    String eachCantidad = cantidadesMeds.get(i);
                    BigDecimal eachPrecio = MedsSeleccionados.get(i).getPrecioVenta();

                    ordenCompletaBuilder.append(eachNombre)
                            .append(" - ")
                            .append(eachCantidad)
                            .append(" - S/. ")
                            .append(eachPrecio)
                            .append("\n");

                    BigDecimal subtotal = eachPrecio.multiply(new BigDecimal(eachCantidad));
                    precioTotal += subtotal.doubleValue();
                }

                ordenCompletaBuilder.append("\n Precio total: S/. ").append(precioTotal);

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
    }

    @PostMapping(value = {"/valida_stock", "/valida_stock/"})
    public ResponseEntity<HashMap<String, Object>> validateStock(@RequestBody Map<String, String> items) {

        HashMap<String, Object> responseJson = new HashMap<>();

        try {
            List<String> listaSelectedMeds = parseJSONMessage(items);

            if (!listaSelectedMeds.isEmpty()){

                List<Medicamento> MedsSeleccionados = getMedicamentosFromLista(listaSelectedMeds);
                List<String> cantidadesMeds = getCantidadesFromLista(listaSelectedMeds);

                verificationStock verify = new verificationStock(MedsSeleccionados, cantidadesMeds);

                List<Medicamento> medicamentosSinStock = verify.getMedicamentosSinStock();
                List<Medicamento> medicamentosConStock = verify.getMedicamentosConStock();
                List<String> cantidadesFaltantes = verify.getCantidadesFaltantes();
                List<String> cantidadesExistentes = verify.getCantidadesExistentes();


                if (!medicamentosSinStock.isEmpty()){
                    StringBuilder ordenCompletaBuilder = new StringBuilder("Su orden es: \n");
                    double precioTotalOrden = 0.0;
                    int cantMeds = medicamentosConStock.size();

                    for (int i = 0; i<cantMeds; i++){

                        String eachNombre = medicamentosConStock.get(i).getNombre();
                        String eachCantidad = cantidadesExistentes.get(i);
                        BigDecimal eachPrecio = medicamentosConStock.get(i).getPrecioVenta();

                        ordenCompletaBuilder.append(eachNombre)
                                .append(" - ")
                                .append(eachCantidad)
                                .append(" - S/. ")
                                .append(eachPrecio)
                                .append("\n");

                        BigDecimal subtotal = eachPrecio.multiply(new BigDecimal(eachCantidad));
                        precioTotalOrden += subtotal.doubleValue();
                    }

                    ordenCompletaBuilder.append("\n Precio total: S/. ").append(precioTotalOrden);

                    String ordenCompleta = ordenCompletaBuilder.toString();



                    StringBuilder preOrdenCompletaBuilder = new StringBuilder("Su preorden es: \n");
                    double precioTotalPreOrden = 0.0;
                    int cantMedsPreOrden = medicamentosSinStock.size();

                    for (int j = 0; j<cantMedsPreOrden; j++){

                        String eachNombre = medicamentosSinStock.get(j).getNombre();
                        String eachCantidad = cantidadesFaltantes.get(j);
                        BigDecimal eachPrecio = medicamentosSinStock.get(j).getPrecioVenta();

                        preOrdenCompletaBuilder.append(eachNombre)
                                .append(" - ")
                                .append(eachCantidad)
                                .append(" - S/. ")
                                .append(eachPrecio)
                                .append("\n");

                        BigDecimal subtotal = eachPrecio.multiply(new BigDecimal(eachCantidad));
                        precioTotalPreOrden += subtotal.doubleValue();
                    }

                    preOrdenCompletaBuilder.append("\n Precio total: S/. ").append(precioTotalPreOrden);

                    String preOrdenCompleta = preOrdenCompletaBuilder.toString();


                    responseJson.put("Orden", ordenCompleta);
                    responseJson.put("PreOrden", preOrdenCompleta);
                    responseJson.put("OrderType", 2);

                } else {

                    StringBuilder ordenCompletaBuilder = new StringBuilder("Su orden es: \n");
                    double precioTotalOrden = 0.0;
                    int cantMeds = medicamentosConStock.size();

                    for (int i = 0; i<cantMeds; i++){

                        String eachNombre = medicamentosConStock.get(i).getNombre();
                        String eachCantidad = cantidadesExistentes.get(i);
                        BigDecimal eachPrecio = medicamentosConStock.get(i).getPrecioVenta();

                        ordenCompletaBuilder.append(eachNombre)
                                .append(" - ")
                                .append(eachCantidad)
                                .append(" - S/. ")
                                .append(eachPrecio)
                                .append("\n");

                        BigDecimal subtotal = eachPrecio.multiply(new BigDecimal(eachCantidad));
                        precioTotalOrden += subtotal.doubleValue();
                    }

                    ordenCompletaBuilder.append("\n Precio total: S/. ").append(precioTotalOrden);

                    String ordenCompleta = ordenCompletaBuilder.toString();

                    responseJson.put("Orden", ordenCompleta);
                    responseJson.put("PreOrden", null);

                    responseJson.put("OrderType", 1);
                }


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



    @PostMapping(value = {"/generate_order", "/generate_order/"})
    public ResponseEntity<HashMap<String, Object>> generateOrder(@RequestBody GenerateRequest generateRequest,
                                                                 @RequestHeader(value = "TOKEN") String SecretToken) {

        if (!SecretToken.equals("1869c2798e4c841d696c448e324cbd8b")){
            HashMap<String, Object> responseJson = new HashMap<>();
            responseJson.put("ERROR", null);
            return ResponseEntity.badRequest().body(responseJson);
        }

        Map<String, String> items = generateRequest.getItems();
        String idPaciente = generateRequest.getIdPaciente();

        HashMap<String, Object> responseJson = new HashMap<>();

        try {
            List<String> listaSelectedMeds = parseJSONMessage(items);

            if (!listaSelectedMeds.isEmpty()){

                List<Medicamento> MedsSeleccionados = getMedicamentosFromLista(listaSelectedMeds);
                List<String> cantidadesMeds = getCantidadesFromLista(listaSelectedMeds);

                verificationStock verify = new verificationStock(MedsSeleccionados, cantidadesMeds);

                List<Medicamento> medicamentosSinStock = verify.getMedicamentosSinStock();
                List<Medicamento> medicamentosConStock = verify.getMedicamentosConStock();
                List<String> cantidadesFaltantes = verify.getCantidadesFaltantes();
                List<String> cantidadesExistentes = verify.getCantidadesExistentes();


                if (!medicamentosSinStock.isEmpty()){
                    StringBuilder ordenCompletaBuilder = new StringBuilder("Su orden es: \n");
                    double precioTotalOrden = 0.0;
                    int cantMeds = medicamentosConStock.size();

                    for (int i = 0; i<cantMeds; i++){

                        String eachNombre = medicamentosConStock.get(i).getNombre();
                        String eachCantidad = cantidadesExistentes.get(i);
                        BigDecimal eachPrecio = medicamentosConStock.get(i).getPrecioVenta();

                        ordenCompletaBuilder.append(eachNombre)
                                .append(" - ")
                                .append(eachCantidad)
                                .append(" - S/. ")
                                .append(eachPrecio)
                                .append("\n");

                        BigDecimal subtotal = eachPrecio.multiply(new BigDecimal(eachCantidad));
                        precioTotalOrden += subtotal.doubleValue();
                    }

                    ordenCompletaBuilder.append("\n Precio total: S/. ").append(precioTotalOrden);

                    String ordenCompleta = ordenCompletaBuilder.toString();



                    StringBuilder preOrdenCompletaBuilder = new StringBuilder("Su preorden es: \n");
                    double precioTotalPreOrden = 0.0;
                    int cantMedsPreOrden = medicamentosSinStock.size();

                    for (int j = 0; j<cantMedsPreOrden; j++){

                        String eachNombre = medicamentosSinStock.get(j).getNombre();
                        String eachCantidad = cantidadesFaltantes.get(j);
                        BigDecimal eachPrecio = medicamentosSinStock.get(j).getPrecioVenta();

                        preOrdenCompletaBuilder.append(eachNombre)
                                .append(" - ")
                                .append(eachCantidad)
                                .append(" - S/. ")
                                .append(eachPrecio)
                                .append("\n");

                        BigDecimal subtotal = eachPrecio.multiply(new BigDecimal(eachCantidad));
                        precioTotalPreOrden += subtotal.doubleValue();
                    }

                    // Se comienza a generar la Orden
                    LocalDateTime now = LocalDateTime.now();

                    // Convertir la hora actual a la zona horaria de Perú
                    ZoneId peruZoneId = ZoneId.of("America/Lima");
                    ZonedDateTime peruTime = now.atZone(ZoneId.systemDefault()).withZoneSameInstant(peruZoneId);

                    // Si solo necesitas LocalDateTime, puedes convertirlo de nuevo
                    LocalDateTime peruLocalDateTime = peruTime.toLocalDateTime();

                    Orden newOrden = new Orden();
                    newOrden.setFechaIni(peruLocalDateTime);
                    newOrden.setPrecioTotal((float) precioTotalOrden);
                    newOrden.setIdFarmacista(109);

                    Usuario pacienteOnStore;
                    Optional<Usuario> usuarioOptional = usuarioRepository.findById(Integer.valueOf(idPaciente));

                    pacienteOnStore = usuarioOptional.orElse(null);
                    newOrden.setPaciente(pacienteOnStore);

                    newOrden.setTipoOrden(2);
                    newOrden.setEstadoOrden(2);
                    newOrden.setSede(sedeRepository.getSedeByIdSede(1));
                    System.out.println(pacienteOnStore);
                    newOrden.setSeguroUsado("false");
                    System.out.println(pacienteOnStore);

                    System.out.println("Orden antes de guardar: " + newOrden);

                    // CREANDO PREORDEN

                    Orden ordenPadre = newOrden;
                    ordenRepository.save(ordenPadre);
                    System.out.println("Orden guardada: " + ordenPadre);

                    int i = 0;
                    for (Medicamento med : medicamentosConStock){

                        OrdenContenidoId contenidoId = new OrdenContenidoId();
                        contenidoId.setIdOrden(ordenPadre.getIdOrden());
                        contenidoId.setIdMedicamento(med.getIdMedicamento());

                        OrdenContenido contenido = new OrdenContenido();
                        contenido.setId(contenidoId);
                        contenido.setIdOrden(ordenPadre);
                        contenido.setIdMedicamento(med);
                        contenido.setCantidad(Integer.parseInt(cantidadesExistentes.get(i)));

                        ordenContenidoRepository.save(contenido);
                        i++;
                    }

                    System.out.println(cantidadesFaltantes);
                    int j = 0;
                    for (String cant: cantidadesFaltantes) {
                        if (Integer.parseInt(cant) > 30){
                            cantidadesFaltantes.set(j,"30");
                        }
                        j++;
                    }

                    // AQUI CREANDO PREORDEN

                    Orden newPreOrden = new Orden();

                    newPreOrden.setFechaIni(ordenPadre.getFechaIni());

                    newPreOrden.setPrecioTotal((float) precioTotalPreOrden);
                    newPreOrden.setIdFarmacista(109);
                    newPreOrden.setPaciente(ordenPadre.getPaciente());
                    newPreOrden.setTipoOrden(3);
                    newPreOrden.setEstadoOrden(8);
                    newPreOrden.setOrdenParent(ordenPadre.getIdOrden());
                    newPreOrden.setEstadoPreOrden(2);
                    newPreOrden.setSede(sedeRepository.getSedeByIdSede(1));
                    newPreOrden.setSeguroUsado(ordenPadre.getSeguroUsado());
                    newPreOrden.setDoctor(ordenPadre.getDoctor());
                    newPreOrden.setTracking(TokenRandomGenerator.generator());

                    ordenRepository.save(newPreOrden);
                    System.out.println("Orden guardada: " + newPreOrden);


                    int k = 0;
                    for (Medicamento med : medicamentosSinStock){

                        OrdenContenidoId contenidoId = new OrdenContenidoId();
                        contenidoId.setIdOrden(newPreOrden.getIdOrden());
                        contenidoId.setIdMedicamento(med.getIdMedicamento());

                        OrdenContenido contenido = new OrdenContenido();
                        contenido.setId(contenidoId);
                        contenido.setIdOrden(newPreOrden);
                        contenido.setIdMedicamento(med);
                        contenido.setCantidad(Integer.parseInt(cantidadesFaltantes.get(k)));

                        ordenContenidoRepository.save(contenido);
                        i++;
                    }


                    int idVerOrdenCreada = ordenPadre.getIdOrden();


                    String urlLink = "https://bienestarsanmiguel.xyz/paciente/tracking?id=" + idVerOrdenCreada;



                    preOrdenCompletaBuilder.append("\n Precio total: S/. ").append(precioTotalPreOrden);

                    String preOrdenCompleta = preOrdenCompletaBuilder.toString();



                    responseJson.put("Orden", ordenCompleta);
                    responseJson.put("PreOrden", preOrdenCompleta);
                    responseJson.put("OrderType", 2);
                    responseJson.put("Link", urlLink);




                } else {

                    StringBuilder ordenCompletaBuilder = new StringBuilder("Su orden es: \n");
                    double precioTotalOrden = 0.0;
                    int cantMeds = medicamentosConStock.size();

                    for (int i = 0; i<cantMeds; i++){

                        String eachNombre = medicamentosConStock.get(i).getNombre();
                        String eachCantidad = cantidadesExistentes.get(i);
                        BigDecimal eachPrecio = medicamentosConStock.get(i).getPrecioVenta();

                        ordenCompletaBuilder.append(eachNombre)
                                .append(" - ")
                                .append(eachCantidad)
                                .append(" - S/. ")
                                .append(eachPrecio)
                                .append("\n");

                        BigDecimal subtotal = eachPrecio.multiply(new BigDecimal(eachCantidad));
                        precioTotalOrden += subtotal.doubleValue();
                    }

                    // Se comienza a generar la Orden
                    LocalDateTime now = LocalDateTime.now();

                    // Convertir la hora actual a la zona horaria de Perú
                    ZoneId peruZoneId = ZoneId.of("America/Lima");
                    ZonedDateTime peruTime = now.atZone(ZoneId.systemDefault()).withZoneSameInstant(peruZoneId);

                    // Si solo necesitas LocalDateTime, puedes convertirlo de nuevo
                    LocalDateTime peruLocalDateTime = peruTime.toLocalDateTime();

                    Orden newOrden = new Orden();
                    newOrden.setFechaIni(peruLocalDateTime);
                    newOrden.setPrecioTotal((float) precioTotalOrden);
                    newOrden.setIdFarmacista(109);

                    Usuario pacienteOnStore;
                    Optional<Usuario> usuarioOptional = usuarioRepository.findById(Integer.valueOf(idPaciente));

                    pacienteOnStore = usuarioOptional.orElse(null);
                    newOrden.setPaciente(pacienteOnStore);

                    newOrden.setTipoOrden(2);
                    newOrden.setEstadoOrden(2);
                    newOrden.setSede(sedeRepository.getSedeByIdSede(1));
                    System.out.println(pacienteOnStore);
                    newOrden.setSeguroUsado("false");
                    System.out.println(pacienteOnStore);

                    System.out.println("Orden antes de guardar: " + newOrden);

                    System.out.println(pacienteOnStore);
                    newOrden.setPaciente(pacienteOnStore);


                    System.out.println(newOrden);

                    ordenRepository.save(newOrden);
                    System.out.println("Orden guardada: " + newOrden);

                    int i = 0;
                    for (Medicamento med : medicamentosConStock){

                        OrdenContenidoId contenidoId = new OrdenContenidoId();
                        contenidoId.setIdOrden(newOrden.getIdOrden());
                        contenidoId.setIdMedicamento(med.getIdMedicamento());

                        OrdenContenido contenido = new OrdenContenido();
                        contenido.setId(contenidoId);
                        contenido.setIdOrden(newOrden);
                        contenido.setIdMedicamento(med);
                        contenido.setCantidad(Integer.parseInt(cantidadesExistentes.get(i)));

                        //----VERIFICAR Y REDUCIR EL STOCK DE UN MEDICAMENTO POR SU ID Y SU SEDE----//
                        if(Integer.parseInt(cantidadesExistentes.get(i)) <= sedeStockRepository.verificarCantidadStockPorSede(1, med.getIdMedicamento())){
                            sedeStockRepository.reducirStockPorSede(1,med.getIdMedicamento(), Integer.parseInt(cantidadesExistentes.get(i)));
                        }
                        //-------------------------------------------------------------------------//

                        ordenContenidoRepository.save(contenido);
                        i++;
                    }

                    int idVerOrdenCreada = newOrden.getIdOrden();


                    String urlLink = "https://bienestarsanmiguel.xyz/paciente/tracking?id=" + idVerOrdenCreada;




                    ordenCompletaBuilder.append("\n Precio total: S/. ").append(precioTotalOrden);
                    String ordenCompleta = ordenCompletaBuilder.toString();

                    responseJson.put("Orden", ordenCompleta);
                    responseJson.put("PreOrden", null);
                    responseJson.put("OrderType", 1);
                    responseJson.put("Link", urlLink);
                }


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

    @GetMapping(value = {"/obtain_meds", "/obtain_meds/"})
    public ResponseEntity<HashMap<String, Object>> obtainMeds(
            @RequestHeader(value = "OPT", required = true) String option) {

        HashMap<String, Object> responseJson = new HashMap<>();

        try {
            List<Medicamento> listaMedicamentos = medicamentoRepository.findAll();

            if (option.equals("1")) {
                StringBuilder medsDetailsBuilder = new StringBuilder();

                for (Medicamento med : listaMedicamentos) {
                    medsDetailsBuilder.append("Nombre: ").append(med.getNombre()).append("\n")
                            .append("Descripción: ").append(med.getDescripcion()).append("\n")
                            .append("Categoría: ").append(med.getCategorias()).append("\n\n");
                }

                if (!listaMedicamentos.isEmpty()) {
                    responseJson.put("MedList", medsDetailsBuilder.toString());
                    return ResponseEntity.status(HttpStatus.SC_OK).body(responseJson);
                } else {
                    responseJson.put("ERROR", "No se encontraron medicamentos.");
                    return ResponseEntity.badRequest().body(responseJson);
                }

            } else if (option.equals("2")) {
                StringBuilder medsIdsAndNamesBuilder = new StringBuilder();

                for (Medicamento med : listaMedicamentos) {
                    medsIdsAndNamesBuilder.append("ID: ").append(med.getIdMedicamento()).append(", ")
                            .append("Nombre: ").append(med.getNombre()).append("\n");
                }

                if (!listaMedicamentos.isEmpty()) {
                    responseJson.put("MedList", medsIdsAndNamesBuilder.toString());
                    return ResponseEntity.status(HttpStatus.SC_OK).body(responseJson);
                } else {
                    responseJson.put("ERROR", "No se encontraron medicamentos.");
                    return ResponseEntity.badRequest().body(responseJson);
                }
            } else {
                responseJson.put("ERROR", "Opción no válida.");
                return ResponseEntity.badRequest().body(responseJson);
            }

        } catch (Exception e) {
            responseJson.put("ERROR", "Se produjo un error al obtener los medicamentos.");
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


    @Getter
    public class verificationStock{
        private final List<Medicamento> medicamentosSinStock;
        private final List<Medicamento> medicamentosConStock;
        private final List<String> cantidadesFaltantes;
        private final List<String> cantidadesExistentes;

        public verificationStock(List<Medicamento> medicamentosSeleccionados, List<String> listaCantidades) {
            List<Medicamento> medicamentosSinStock = new ArrayList<>();
            List<Medicamento> medicamentosConStock = new ArrayList<>();
            List<String> cantidadesFaltantes = new ArrayList<>();
            List<String> cantidadesExistentes = new ArrayList<>();

            int i = 0;

            SedeStockId sedeStockId = new SedeStockId();
            sedeStockId.setIdSede(1);

            for (Medicamento med : medicamentosSeleccionados) {

                sedeStockId.setIdMedicamento(med.getIdMedicamento());
                Optional<SedeStock> sedeStockOptional = sedeStockRepository.findById(sedeStockId);

                if (sedeStockOptional.isPresent()) {
                    SedeStock sedeStock = sedeStockOptional.get();

                    Medicamento medicamentoToVerify = sedeStock.getIdMedicamento();

                    if(Integer.parseInt(listaCantidades.get(i)) > sedeStock.getCantidad()){
                        medicamentosSinStock.add(medicamentoToVerify);
                        cantidadesFaltantes.add(String.valueOf(Integer.parseInt(listaCantidades.get(i)) - sedeStock.getCantidad()));

                        medicamentosConStock.add(medicamentoToVerify);
                        cantidadesExistentes.add(String.valueOf(sedeStock.getCantidad()));

                    } else if (sedeStock.getCantidad() > 0) {
                        medicamentosConStock.add(medicamentoToVerify);
                        cantidadesExistentes.add(listaCantidades.get(i));
                    }

                } else {
                    medicamentosSinStock.add(med);
                    cantidadesFaltantes.add(listaCantidades.get(i));
                }

                i++;
            }

            this.medicamentosSinStock = medicamentosSinStock;
            this.medicamentosConStock = medicamentosConStock;
            this.cantidadesFaltantes = cantidadesFaltantes;
            this.cantidadesExistentes = cantidadesExistentes;
        }
    }


    public List<String> parseJSONMessage(Map<String, String> items) {

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
        return listaSelectedMeds;
    }

}
