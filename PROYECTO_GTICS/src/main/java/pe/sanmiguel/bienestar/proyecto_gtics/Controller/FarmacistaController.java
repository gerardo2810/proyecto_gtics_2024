package pe.sanmiguel.bienestar.proyecto_gtics.Controller;

import com.google.api.Http;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.Getter;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.sanmiguel.bienestar.proyecto_gtics.*;
import pe.sanmiguel.bienestar.proyecto_gtics.Dto.MedicamentosSedeStockDto;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.*;
import pe.sanmiguel.bienestar.proyecto_gtics.Repository.*;
import pe.sanmiguel.bienestar.proyecto_gtics.ValidationGroup.DniApiValidationGroup;
import pe.sanmiguel.bienestar.proyecto_gtics.ValidationGroup.FarmacistaValidationsGroup;

import javax.print.attribute.standard.PresentationDirection;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Controller
public class FarmacistaController {


    /* Repositorios */
    final UsuarioRepository usuarioRepository;
    final SedeRepository sedeRepository;
    final SedeStockRepository sedeStockRepository;
    final SedeFarmacistaRepository sedeFarmacistaRepository;
    final MedicamentoRepository medicamentoRepository;
    final OrdenRepository ordenRepository;
    final OrdenContenidoRepository ordenContenidoRepository;
    final ReposicionRepository reposicionRepository;
    final EstadoPreOrdenRepository estadoPreOrdenRepository;
    final DoctorRepository doctorRepository;

    final ChatRepository chatRepository;

    public FarmacistaController(ChatRepository chatRepository, UsuarioRepository usuarioRepository, SedeRepository sedeRepository, SedeStockRepository sedeStockRepository, SedeFarmacistaRepository sedeFarmacistaRepository, MedicamentoRepository medicamentoRepository, OrdenRepository ordenRepository, OrdenContenidoRepository ordenContenidoRepository, ReposicionRepository reposicionRepository, EstadoPreOrdenRepository estadoPreOrdenRepository, DoctorRepository doctorRepository, DniAPI dniAPI) {
        this.usuarioRepository = usuarioRepository;
        this.sedeRepository = sedeRepository;
        this.sedeStockRepository = sedeStockRepository;
        this.sedeFarmacistaRepository = sedeFarmacistaRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.ordenRepository = ordenRepository;
        this.ordenContenidoRepository = ordenContenidoRepository;
        this.reposicionRepository  =reposicionRepository;
        this.estadoPreOrdenRepository = estadoPreOrdenRepository;
        this.doctorRepository = doctorRepository;
        this.dniAPI = dniAPI;
        this.chatRepository = chatRepository;
    }

    @Autowired
    private ChatService chatService;

    /* Repositorios */


    /* Variables Internas */

    Sede sedeSession;
    Usuario usuarioSession;
    List<Medicamento> medicamentosSeleccionados = new ArrayList<>();
    List<String> listaCantidades = new ArrayList<>();

    List<Medicamento> medicamentosSinStock = new ArrayList<>();
    List<Medicamento> medicamentosConStock = new ArrayList<>();
    List<String> cantidadesFaltantes = new ArrayList<>();
    List<String> cantidadesExistentes = new ArrayList<>();
    Orden ordenSaved = new Orden();

    Usuario pacienteOnStore = new Usuario();

    Integer idVerOrdenCreada;

    /* Variables Internas */


    @Autowired
    private EmailService emailService;

    final DniAPI dniAPI;

    @GetMapping(value = "/prueba_api")
    public String pruebaDniApi(){

        List<String> values = DniAPI.getDni("1145552");

        if (!values.isEmpty()){
            String apiDni = values.get(3);
            String apiNombres = values.get(0);
            String apiApellidos = (values.get(1) + " " + values.get(2));
        }

        System.out.println(values);

        return "redirect:/farmacista";
    }

    @GetMapping(value = "/correo_prueba")
    public String prueba(HttpServletRequest request, HttpServletResponse response, Authentication authentication){

        String toEmail = "a20191641@pucp.edu.pe";
        String subject = "Este es un correo de Prueba!";

        HttpSession session = request.getSession();
        Usuario usuarioSession = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuarioSession);
        String nombre = usuarioSession.getNombres() + " " + usuarioSession.getApellidos();

        Map<String, Object> variables = new HashMap<>();
        variables.put("nombre", nombre);

        try {
            emailService.sendHtmlEmail(toEmail, subject, "welcome-email", variables);
            System.out.println("Correo HTML enviado con éxito!");
            return "redirect:/farmacista";
        } catch (MessagingException e) {
            System.out.println("Error al enviar el correo HTML: " + e.getMessage());
            return "redirect:/farmacista";
        }
    }


    @GetMapping(value = {"/farmacista", "/farmacista/"})
    public String farmacistaInicio(Model model,
                                   HttpServletRequest request, HttpServletResponse response,
                                   Authentication authentication,
                                   @RequestParam(name = "categoria", required = false) String categoria) {

        //SESSION
        //Iniciamos la sesión
        HttpSession session = request.getSession();
        usuarioSession = usuarioRepository.findByCorreo(authentication.getName());
        if (usuarioSession == null) {
            usuarioSession = usuarioRepository.findByCorreo(authentication.getName());
            session.setAttribute("usuario", usuarioSession);
        }

        Integer idSede = (Integer) session.getAttribute("idSede");

        if (idSede == null) {
            Sede sedeSession = sedeFarmacistaRepository.buscarFarmacistaSede(usuarioSession.getIdUsuario()).getIdSede();

            // Verificar si sedeSession es nulo
            if (sedeSession != null) {
                idSede = sedeSession.getIdSede();
                session.setAttribute("idSede", idSede);
            } else {
                // Manejar el caso cuando sedeSession es nulo
                return "login/error"; // O alguna página de error apropiada
            }
        }
        // Log para verificar si el parámetro categoria llega correctamente
        System.out.println("Categoría recibida: " + categoria);

        // Normaliza el texto de la categoría
        if (categoria != null) {
            categoria = categoria.trim().toLowerCase();
        }

        //Obtener la lista de medicamentos según la categoría seleccionada
        List<MedicamentosSedeStockDto> listaMedicamentos;
        if (categoria != null && !categoria.isEmpty()) {
            listaMedicamentos = sedeStockRepository.findMedicamentosConStockByCategoria(categoria);
            System.out.println(listaMedicamentos);
        } else {
            listaMedicamentos = sedeStockRepository.findMedicamentosConStock();
        }
        System.out.println(listaMedicamentos);

        int numeroOrdenesPendientes = 0;
        model.addAttribute("sedeSession", sedeSession);
        model.addAttribute("listaMedicamentos", listaMedicamentos);
        model.addAttribute("numeroOrdenesPendientes", numeroOrdenesPendientes);
        model.addAttribute("categoriaSeleccionada", categoria);
        model.addAttribute("lista1",listaMedicamentos);


        return "farmacista/inicio";
    }
    @GetMapping("/farmacista/ver_detalles")
    public String farmacistaDetalles(Model model,
                                     HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        //Iniciamos la sesión
        HttpSession session = request.getSession();
        Usuario usuarioSession = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuarioSession);

        Sede sedeSession = sedeFarmacistaRepository.buscarFarmacistaSede(usuarioSession.getIdUsuario()).getIdSede();

        List<Medicamento> listaMedicamentos = medicamentoRepository.findAll();
        model.addAttribute("sedeSession", sedeSession);
        model.addAttribute("listaMedicamentos", listaMedicamentos);
        return "farmacista/ver_detalles";
    }

    @PostMapping("/farmacista/continuar_compra")
    public String fillContentOrder(@RequestParam("listaIds") List<String> listaSelectedIds){
        if (!listaSelectedIds.isEmpty()){
            medicamentosSeleccionados = getMedicamentosFromLista(listaSelectedIds);
            listaCantidades = getCantidadesFromLista(listaSelectedIds);
            return "redirect:/farmacista/formulario_paciente";
        } else {
            return "redirect:/farmacista";
        }
    }

    @GetMapping("/farmacista/formulario_paciente")
    public String formPacienteData(@ModelAttribute("usuario") Usuario usuario, Model model,
                                   HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        HttpSession session = request.getSession();
        Usuario usuarioSession = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuarioSession);

        sedeSession = sedeFarmacistaRepository.buscarFarmacistaSede(usuarioSession.getIdUsuario()).getIdSede();

        if(medicamentosSeleccionados.isEmpty()){
            return "redirect:/farmacista";
        } else {

            List<Integer> stockSeleccionados = new ArrayList<>();

            for (Medicamento med : medicamentosSeleccionados) {
                if (sedeStockRepository.getSedeStockByIdSedeAndIdMedicamento(sedeSession, med).isPresent()) {
                    stockSeleccionados.add(sedeStockRepository.getSedeStockByIdMedicamentoAndIdSede(med,sedeSession).getCantidad());
                } else {
                    stockSeleccionados.add(0);
                }
            }

            ArrayList<Doctor> listaDoctores = (ArrayList<Doctor>) doctorRepository.findAll();

            model.addAttribute("listaDoctores", listaDoctores);
            model.addAttribute("stockSeleccionados", stockSeleccionados);
            model.addAttribute("medicamentosSeleccionados", medicamentosSeleccionados);
            model.addAttribute("listaCantidades", listaCantidades);

            return "farmacista/formulario_paciente";
        }
    }

    @PostMapping("/farmacista/form_paciente")
    public String dniApi(@ModelAttribute("usuario") @Validated(DniApiValidationGroup.class) Usuario usuario,
                         BindingResult bindingResult,
                         Model model,
                         @RequestParam(value = "dni") String dni,
                         HttpServletRequest request, HttpServletResponse response, Authentication authentication){

        //Iniciamos la sesión
        HttpSession session = request.getSession();
        Usuario usuarioSession = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuarioSession);

        sedeSession = sedeFarmacistaRepository.buscarFarmacistaSede(usuarioSession.getIdUsuario()).getIdSede();

        if (bindingResult.hasErrors()){

            System.out.println(bindingResult.getAllErrors());

            List<Integer> stockSeleccionados = new ArrayList<>();

            for (Medicamento med : medicamentosSeleccionados) {
                if (sedeStockRepository.getSedeStockByIdSedeAndIdMedicamento(sedeSession, med).isPresent()) {
                    stockSeleccionados.add(sedeStockRepository.getSedeStockByIdMedicamentoAndIdSede(med,sedeSession).getCantidad());
                } else {
                    stockSeleccionados.add(0);
                }
            }

            ArrayList<Doctor> listaDoctores = (ArrayList<Doctor>) doctorRepository.findAll();

            model.addAttribute("listaDoctores", listaDoctores);
            model.addAttribute("stockSeleccionados", stockSeleccionados);
            model.addAttribute("medicamentosSeleccionados", medicamentosSeleccionados);
            model.addAttribute("listaCantidades", listaCantidades);

            model.addAttribute("nombres", "");
            model.addAttribute("apellidos", "");
            model.addAttribute("dni", "");

            return "farmacista/formulario_paciente";

        } else {
            List<Integer> stockSeleccionados = new ArrayList<>();

            for (Medicamento med : medicamentosSeleccionados) {
                if (sedeStockRepository.getSedeStockByIdSedeAndIdMedicamento(sedeSession, med).isPresent()) {
                    stockSeleccionados.add(sedeStockRepository.getSedeStockByIdMedicamentoAndIdSede(med,sedeSession).getCantidad());
                } else {
                    stockSeleccionados.add(0);
                }
            }

            ArrayList<Usuario> listaUsuarios = (ArrayList<Usuario>) usuarioRepository.listarUsuariosSegunRol(4);
            ArrayList<Doctor> listaDoctores = (ArrayList<Doctor>) doctorRepository.findAll();

            model.addAttribute("listaUsuarios", listaUsuarios);
            model.addAttribute("listaDoctores", listaDoctores);
            model.addAttribute("stockSeleccionados", stockSeleccionados);
            model.addAttribute("medicamentosSeleccionados", medicamentosSeleccionados);
            model.addAttribute("listaCantidades", listaCantidades);

            List<String> values = DniAPI.getDni(dni);

            if (!(values.isEmpty())){

                if (!values.get(0).isEmpty()){
                    String apiDni = values.get(3);
                    String apiNombres = values.get(0);
                    String apiApellidos = (values.get(1) + " " + values.get(2));

                    model.addAttribute("dni", apiDni);
                    model.addAttribute("nombres", apiNombres);
                    model.addAttribute("apellidos", apiApellidos);

                    boolean founded = false;

                    Optional<Usuario> usuarioOptional = usuarioRepository.findPacienteByDni(apiDni);
                    Usuario userExisting = new Usuario();
                    if (usuarioOptional.isPresent()){
                        userExisting = usuarioOptional.get();

                        model.addAttribute("usuario", userExisting);
                        founded = true;
                        pacienteOnStore = userExisting;
                    }

                    model.addAttribute("foundedNotification", founded);

                    System.out.println(values);
                    System.out.println("El usuario fue encontrado en DB?" + founded);
                    return "farmacista/formulario_paciente";

                } else {

                    // Caso cuando el dni no existe
                    String dniError = "error";
                    model.addAttribute("dniError", dniError);
                    return "farmacista/formulario_paciente";
                }
            } else {
                // Caso cuando el dni no existe
                String dniError = "error";
                model.addAttribute("dniError", dniError);
                return "farmacista/formulario_paciente";
            }
        }

    }

    @PostMapping("/farmacista/finalizar_compra")
    public String createOrdenVenta(@ModelAttribute("usuario") @Validated(FarmacistaValidationsGroup.class) Usuario usuario,
                                   BindingResult bindingResult,
                                   Model model,
                                   @RequestParam(value = "dni") String dni,
                                   @RequestParam(value = "nombres") String name,
                                   @RequestParam(value = "apellidos") String lastname,
                                   @RequestParam(value = "distrito") String distrito,
                                   @RequestParam(value = "direccion") String direccion,
                                   @RequestParam(value = "doctor", required = false) String doctor,
                                   @RequestParam(value = "seguro", required = false) String seguro,
                                   @RequestParam(value = "correo") String correo,
                                   @RequestParam(value = "celular") String celular,
                                   @RequestParam(value = "listaIds") List<String> listaSelectedIds,
                                   @RequestParam(value = "priceTotal") String priceTotal,
                                   HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        System.out.println(dni);
        System.out.println(name);
        System.out.println(lastname);
        System.out.println(listaSelectedIds);
        //Iniciamos la sesión
        HttpSession session = request.getSession();
        Usuario usuarioSession = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuarioSession);

        sedeSession = sedeFarmacistaRepository.buscarFarmacistaSede(usuarioSession.getIdUsuario()).getIdSede();

        if (bindingResult.hasErrors()){

            System.out.println(bindingResult.getAllErrors());

            List<Integer> stockSeleccionados = new ArrayList<>();

            for (Medicamento med : medicamentosSeleccionados) {
                if (sedeStockRepository.getSedeStockByIdSedeAndIdMedicamento(sedeSession, med).isPresent()) {
                    stockSeleccionados.add(sedeStockRepository.getSedeStockByIdMedicamentoAndIdSede(med,sedeSession).getCantidad());
                } else {
                    stockSeleccionados.add(0);
                }
            }

            ArrayList<Doctor> listaDoctores = (ArrayList<Doctor>) doctorRepository.findAll();

            model.addAttribute("listaDoctores", listaDoctores);
            model.addAttribute("stockSeleccionados", stockSeleccionados);
            model.addAttribute("medicamentosSeleccionados", medicamentosSeleccionados);
            model.addAttribute("listaCantidades", listaCantidades);

            model.addAttribute("nombres", "");
            model.addAttribute("apellidos", "");
            model.addAttribute("dni", "");

            return "farmacista/formulario_paciente";

        } else {

            idVerOrdenCreada = 0;
            medicamentosSeleccionados = getMedicamentosFromLista(listaSelectedIds);
            listaCantidades = getCantidadesFromLista(listaSelectedIds);

            verificationStock verificationStock = new verificationStock(medicamentosSeleccionados, listaCantidades);

            if (usuarioRepository.findPacienteByCorreo(correo).isPresent()){
                Usuario usuarioToComp = usuarioRepository.findPacienteByCorreo(correo).get();

                if (usuarioToComp.getDni().equals(dni)){
                    verificationUser verificationUser = new verificationUser(name,lastname,dni,distrito,direccion,seguro,correo,celular);
                    this.pacienteOnStore = verificationUser.getUser();

                } else {

                    if(medicamentosSeleccionados.isEmpty()){
                        return "redirect:/farmacista";

                    } else {

                        List<Integer> stockSeleccionados = new ArrayList<>();

                        for (Medicamento med : medicamentosSeleccionados) {
                            if (sedeStockRepository.getSedeStockByIdSedeAndIdMedicamento(sedeSession, med).isPresent()) {
                                stockSeleccionados.add(sedeStockRepository.getSedeStockByIdMedicamentoAndIdSede(med,sedeSession).getCantidad());
                            } else {
                                stockSeleccionados.add(0);
                            }
                        }

                        ArrayList<Doctor> listaDoctores = (ArrayList<Doctor>) doctorRepository.findAll();

                        model.addAttribute("listaDoctores", listaDoctores);
                        model.addAttribute("stockSeleccionados", stockSeleccionados);
                        model.addAttribute("medicamentosSeleccionados", medicamentosSeleccionados);
                        model.addAttribute("listaCantidades", listaCantidades);

                        // Caso cuando el correo ya existe en DB
                        String badEmail = "error";
                        model.addAttribute("badEmail", badEmail);

                        // Datos ya jalados de la API
                        model.addAttribute("nombres", name);
                        model.addAttribute("apellidos", lastname);
                        model.addAttribute("dni", dni);

                        return "farmacista/formulario_paciente";
                    }
                }

            } else {
                verificationUser verificationUser = new verificationUser(name,lastname,dni,distrito,direccion,seguro,correo,celular);
                this.pacienteOnStore = verificationUser.getUser();
            }


            System.out.println("Paciente on Store: " + pacienteOnStore);
            System.out.println("ID del paciente: " + pacienteOnStore.getIdUsuario());



            if (verificationStock.getMedicamentosSinStock().isEmpty()) {

                LocalDateTime now = LocalDateTime.now();

                // Convertir la hora actual a la zona horaria de Perú
                ZoneId peruZoneId = ZoneId.of("America/Lima");
                ZonedDateTime peruTime = now.atZone(ZoneId.systemDefault()).withZoneSameInstant(peruZoneId);

                // Si solo necesitas LocalDateTime, puedes convertirlo de nuevo
                LocalDateTime peruLocalDateTime = peruTime.toLocalDateTime();

                Orden newOrden = new Orden();
                newOrden.setFechaIni(peruLocalDateTime);
                priceTotal = priceTotal.replace(",", "");
                newOrden.setPrecioTotal(Float.parseFloat(priceTotal));
                newOrden.setIdFarmacista(usuarioSession.getIdUsuario());
                newOrden.setPaciente(pacienteOnStore);
                newOrden.setTipoOrden(1);
                newOrden.setEstadoOrden(8);
                newOrden.setSede(sedeSession);
                System.out.println(pacienteOnStore);
                newOrden.setSeguroUsado(Objects.requireNonNullElse(seguro, "false"));
                System.out.println(pacienteOnStore);

                if (!(doctor == null)) {
                    if (!doctor.isEmpty() && !doctor.equals("no-doctor")) {
                        newOrden.setDoctor(doctorRepository.getByIdDoctor(Integer.valueOf(doctor)));
                    }
                }

                System.out.println("Orden antes de guardar: " + newOrden);

                System.out.println(pacienteOnStore);
                newOrden.setPaciente(this.pacienteOnStore);


                System.out.println(newOrden);

                ordenRepository.save(newOrden);
                System.out.println("Orden guardada: " + newOrden);

                int i = 0;
                for (Medicamento med : medicamentosSeleccionados){

                    OrdenContenidoId contenidoId = new OrdenContenidoId();
                    contenidoId.setIdOrden(newOrden.getIdOrden());
                    contenidoId.setIdMedicamento(med.getIdMedicamento());

                    OrdenContenido contenido = new OrdenContenido();
                    contenido.setId(contenidoId);
                    contenido.setIdOrden(newOrden);
                    contenido.setIdMedicamento(med);
                    contenido.setCantidad(Integer.parseInt(listaCantidades.get(i)));

                    //----VERIFICAR Y REDUCIR EL STOCK DE UN MEDICAMENTO POR SU ID Y SU SEDE----//
                    if(Integer.parseInt(listaCantidades.get(i)) <= sedeStockRepository.verificarCantidadStockPorSede(sedeSession.getIdSede(), med.getIdMedicamento())){
                        sedeStockRepository.reducirStockPorSede(sedeSession.getIdSede(),med.getIdMedicamento(), Integer.parseInt(listaCantidades.get(i)));
                    }
                    //-------------------------------------------------------------------------//



                    ordenContenidoRepository.save(contenido);
                    i++;
                }

                idVerOrdenCreada = newOrden.getIdOrden();
                return "redirect:/farmacista/ver_orden_tracking?id=" + idVerOrdenCreada;

            } else {

                LocalDateTime now = LocalDateTime.now();

                // Convertir la hora actual a la zona horaria de Perú
                ZoneId peruZoneId = ZoneId.of("America/Lima");
                ZonedDateTime peruTime = now.atZone(ZoneId.systemDefault()).withZoneSameInstant(peruZoneId);

                // Si solo necesitas LocalDateTime, puedes convertirlo de nuevo
                LocalDateTime peruLocalDateTime = peruTime.toLocalDateTime();

                Orden newOrden = new Orden();
                newOrden.setFechaIni(peruLocalDateTime);
                priceTotal = priceTotal.replace(",", "");
                newOrden.setPrecioTotal(Float.parseFloat(priceTotal));
                newOrden.setIdFarmacista(usuarioSession.getIdUsuario());
                newOrden.setPaciente(pacienteOnStore);
                newOrden.setTipoOrden(1);
                newOrden.setEstadoOrden(8);
                newOrden.setSede(sedeSession);
                System.out.println(pacienteOnStore);
                newOrden.setSeguroUsado(Objects.requireNonNullElse(seguro, "false"));
                System.out.println(pacienteOnStore);

                if (!(doctor == null)) {
                    if (!doctor.isEmpty() && !doctor.equals("no-doctor")) {
                        newOrden.setDoctor(doctorRepository.getByIdDoctor(Integer.valueOf(doctor)));
                    }
                }

                System.out.println("Orden antes de guardar: " + newOrden);

                this.ordenSaved = newOrden;
                this.medicamentosSinStock = verificationStock.getMedicamentosSinStock();
                this.medicamentosConStock = verificationStock.getMedicamentosConStock();
                this.cantidadesFaltantes = verificationStock.getCantidadesFaltantes();
                this.cantidadesExistentes = verificationStock.getCantidadesExistentes();

                return "redirect:/farmacista/crear_preorden";
            }
        }
    }

    @GetMapping("/farmacista/crear_preorden")
    public String createPreOrden(Model model) {

        if (medicamentosSinStock.isEmpty()){
            return "redirect:/farmacista";
        }

        System.out.println(cantidadesFaltantes);
        int j = 0;
        for (String cant: cantidadesFaltantes) {
            if (Integer.parseInt(cant) > 30){
                cantidadesFaltantes.set(j,"30");
            }
            j++;
        }

        model.addAttribute("medicamentosSinStock", medicamentosSinStock);
        model.addAttribute("medicamentosConStock", medicamentosConStock);
        model.addAttribute("cantidadesFaltantes", cantidadesFaltantes);
        model.addAttribute("cantidadesExistentes", cantidadesExistentes);

        System.out.println(medicamentosConStock);
        System.out.println(cantidadesExistentes);

        BigDecimal montoTotalOrden = new BigDecimal(0);

        for (int i = 0; i < medicamentosConStock.size(); i++) {
            BigDecimal precio = medicamentosConStock.get(i).getPrecioVenta();
            int cant = Integer.parseInt(cantidadesExistentes.get(i));
            montoTotalOrden = montoTotalOrden.add(precio.multiply(BigDecimal.valueOf(cant)));
        }
        System.out.println(montoTotalOrden);

        model.addAttribute("montoTotalOrden", montoTotalOrden);

        return "farmacista/crear_pre_orden";
    }

    @PostMapping("/farmacista/finalizar_preorden")
    public String finalizarPreOrden(@RequestParam("listaIds") List<String> listaSelectedIds){

        medicamentosSinStock = getMedicamentosFromLista(listaSelectedIds);
        cantidadesFaltantes = getCantidadesFromLista(listaSelectedIds);

        Orden ordenPadre = ordenSaved;

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

        Orden newPreOrden = new Orden();

        newPreOrden.setFechaIni(ordenPadre.getFechaIni());

        BigDecimal montoTotalPreOrden = new BigDecimal(0);

        for (int j = 0; j < medicamentosSinStock.size(); j++) {
            BigDecimal precio = medicamentosSinStock.get(j).getPrecioVenta();
            int cant = Integer.parseInt(cantidadesFaltantes.get(j));
            montoTotalPreOrden = montoTotalPreOrden.add(precio.multiply(BigDecimal.valueOf(cant)));
        }
        System.out.println(montoTotalPreOrden);

        newPreOrden.setPrecioTotal(Float.parseFloat(String.valueOf(montoTotalPreOrden)));
        newPreOrden.setIdFarmacista(usuarioSession.getIdUsuario());
        newPreOrden.setPaciente(pacienteOnStore);
        newPreOrden.setTipoOrden(3);
        newPreOrden.setEstadoOrden(8);
        newPreOrden.setOrdenParent(ordenPadre.getIdOrden());
        newPreOrden.setEstadoPreOrden(3);
        newPreOrden.setSede(sedeSession);
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

        return "redirect:/farmacista/ver_orden_tracking?id=" + ordenPadre.getIdOrden();
    }


    @GetMapping("/farmacista/cambiar_medicamentos")
    public String changeMedicamentos(Model model) {
        if (medicamentosSeleccionados.isEmpty()) {
            return "redirect:/farmacista";
        } else {
            // Obtener todas las categorías de los medicamentos seleccionados
            Set<String> categoriasMedicamentos = new HashSet<>();
            for (Medicamento medicamento : medicamentosSeleccionados) {
                categoriasMedicamentos.add(medicamento.getCategorias());
            }

            // Crear una lista de listas de medicamentos agrupados por categoría
            List<List<Medicamento>> medicamentosAgrupados = new ArrayList<>();
            for (Medicamento medicamento : medicamentosSeleccionados) {
                String categoriaMedicamento = medicamento.getCategorias();
                List<Medicamento> medicamentosPorCategoria = medicamentoRepository.findByCategorias(categoriaMedicamento);
                medicamentosAgrupados.add(medicamentosPorCategoria);
            }

            model.addAttribute("medicamentosSeleccionados", medicamentosSeleccionados);
            model.addAttribute("medicamentosAgrupados", medicamentosAgrupados);
            model.addAttribute("cantidadesFaltantes", cantidadesFaltantes);
            model.addAttribute("cantidadesExistentes", cantidadesExistentes);
        }

        return "farmacista/cambiar_medicamentos";
    }

    @GetMapping("/farmacista/reemplazar_medicamentos")
    public String replace(Model model, @RequestParam(value="id") Integer idOrden,
                                        @RequestParam(value="currentMed", required = false) List<Medicamento> listaMed,
                                        @RequestParam(value="currentCant", required = false) List<String> listaInt, HttpSession session) {

        Usuario userSession = (Usuario) session.getAttribute("usuario");

        if (chatRepository.buscarChatReemplazarMed(userSession.getIdUsuario(), idOrden) != null){

            if(listaMed != null){
                model.addAttribute("currentMed", listaMed);
                model.addAttribute("currentCant", listaInt);
            }

            List<OrdenContenido> lista = ordenContenidoRepository.findMedicamentosByOrdenId(String.valueOf(idOrden));
            List<OrdenContenido> medicamentosEnStock = new ArrayList<>();
            List<OrdenContenido> medicamentosSinStock = new ArrayList<>();
            for(OrdenContenido oc : lista){
                if(oc.getCantidad() > sedeStockRepository.verificarCantidadStockPorSede(  ordenRepository.getOrdenByIdOrden(Integer.parseInt(String.valueOf(idOrden))).getSede().getIdSede(), oc.getIdMedicamento().getIdMedicamento())    ){
                    medicamentosSinStock.add(oc);
                }else{
                    medicamentosEnStock.add(oc);
                }
            }
            model.addAttribute("medicamentosSinStock",medicamentosSinStock);

            List<MedicamentosSedeStockDto> listaMedicamentos = sedeStockRepository.findMedicamentosConStockBySede(ordenRepository.getOrdenByIdOrden(Integer.parseInt(String.valueOf(idOrden))).getSede().getIdSede());
            model.addAttribute("listaMedicamentos",listaMedicamentos);


            model.addAttribute("idOrden",idOrden);

            return "farmacista/chat_reemplazar_medicamentos";
        }else{
            return "redirect:/farmacista/mensajeria";
        }
    }


    @PostMapping("/farmacista/confirmar_cambio")
    public String confirmarCambio(Model model, HttpSession session, @RequestParam("listaIds") List<Integer> lista,
                                  @RequestParam("idOrden") String idOrden, RedirectAttributes redirectAttributes){

        System.out.println("la lista es:" + lista);


        Integer i = new Integer(0);
        Integer cantidad = new Integer(0);

        Integer verificar = null;
        while(i < lista.size()){
            Medicamento medicamento = medicamentoRepository.getById(lista.get(i));
            cantidad = lista.get(i+1);

            //VERIFICAR EL STOCK DE UN MEDICAMENTO POR SU ID Y SU SEDE
            if(cantidad > sedeStockRepository.verificarCantidadStockPorSede(ordenRepository.getOrdenByIdOrden(Integer.parseInt(String.valueOf(idOrden))).getSede().getIdSede(), medicamento.getIdMedicamento())){
                verificar = 1; //No hay stock suficiente
                break;
            }else{
                verificar = 0; //Hay stock
            }
            i = i + 2;
        }

        if(verificar==1){
            List<Medicamento> medicamentosSeleccionados = getMedicamentosFromListaInteger(lista);
            List<String> listaCantidades = getCantidadesFromListaInteger(lista);
            redirectAttributes.addAttribute("currentMed", medicamentosSeleccionados);
            redirectAttributes.addAttribute("currentCant", listaCantidades);
            redirectAttributes.addAttribute("id", idOrden);
            return "redirect:/farmacista/reemplazar_medicamentos";

        }else{

            //eliminar medicamentos de orden contenido antiguos
            List<OrdenContenido> lista2 = ordenContenidoRepository.findMedicamentosByOrdenId(String.valueOf(idOrden));
            List<OrdenContenido> medicamentosEnStock = new ArrayList<>();
            List<OrdenContenido> medicamentosSinStock = new ArrayList<>();
            for(OrdenContenido oc : lista2){
                if(oc.getCantidad() > sedeStockRepository.verificarCantidadStockPorSede(  ordenRepository.getOrdenByIdOrden(Integer.parseInt(String.valueOf(idOrden))).getSede().getIdSede(), oc.getIdMedicamento().getIdMedicamento())    ){
                    ordenContenidoRepository.borrarContenidoOrden(oc.getIdMedicamento().getIdMedicamento(), idOrden);
                }
            }

            //agregar los nuevos
            Integer j = new Integer(0);
            Integer cant = new Integer(0);

            OrdenContenido ordenContenido = new OrdenContenido();
            OrdenContenidoId oid = new OrdenContenidoId();

            ordenContenido.setIdOrden(ordenRepository.getOrdenByIdOrden(Integer.valueOf(idOrden)));
            oid.setIdOrden(ordenRepository.getOrdenByIdOrden(Integer.valueOf(idOrden)).getIdOrden());


            while(j < lista.size()){


                Medicamento medicamento = medicamentoRepository.getById(lista.get(j));
                cant = lista.get(j+1);


                //VERIFICAR Y REDUCIR EL STOCK DE UN MEDICAMENTO POR SU ID Y SU SEDE
                if(cant <= sedeStockRepository.verificarCantidadStockPorSede(ordenRepository.getOrdenByIdOrden(Integer.parseInt(String.valueOf(idOrden))).getSede().getIdSede(), medicamento.getIdMedicamento())){
                    sedeStockRepository.reducirStockPorSede(ordenRepository.getOrdenByIdOrden(Integer.parseInt(String.valueOf(idOrden))).getSede().getIdSede(),medicamento.getIdMedicamento(), cant);
                }


                oid.setIdMedicamento(medicamento.getIdMedicamento());
                ordenContenido.setId(oid);
                ordenContenido.setIdMedicamento(medicamento);
                ordenContenido.setCantidad(cantidad);
                ordenContenidoRepository.save(ordenContenido);



                j = j + 2;


            }

            //cambiar el estado a pago disponible
            ordenRepository.actualizarEstadoOrden(2, Integer.valueOf(idOrden));

            return "redirect:/farmacista/ordenes_web";

        }

    }

    @PostMapping("/farmacista/generar_preorden_chat")
    public String preordenChat(Model model, HttpSession session, @RequestParam("idOrden") String idOrden, RedirectAttributes redirectAttributes){

        System.out.println(idOrden);

        List<OrdenContenido> lista = ordenContenidoRepository.findMedicamentosByOrdenId(idOrden);
        List<OrdenContenido> medicamentosEnStock = new ArrayList<>();
        List<OrdenContenido> medicamentosSinStock = new ArrayList<>();
        for(OrdenContenido oc : lista){
            if(oc.getCantidad() > sedeStockRepository.verificarCantidadStockPorSede(  ordenRepository.getOrdenByIdOrden(Integer.parseInt(idOrden)).getSede().getIdSede(), oc.getIdMedicamento().getIdMedicamento())    ){
                medicamentosSinStock.add(oc);
            }else{
                medicamentosEnStock.add(oc);
            }
        }

        Optional<Orden> ordenAntiguaOptional = ordenRepository.findById(Integer.valueOf(idOrden));

        //Generar Pre Orden:
        if(ordenAntiguaOptional.isPresent()){

            Orden ordenAntigua = ordenAntiguaOptional.get();

            String tracking = new String();
            tracking= ordenRepository.findLastOrdenId()+1 + "-2024";

            LocalDateTime fechaIni = LocalDateTime.now();
            // Convertir la hora actual a la zona horaria de Perú
            ZoneId peruZoneId = ZoneId.of("America/Lima");
            ZonedDateTime peruTime = fechaIni.atZone(ZoneId.systemDefault()).withZoneSameInstant(peruZoneId);

            // Si solo necesitas LocalDateTime, puedes convertirlo de nuevo
            LocalDateTime peruLocalDateTime = peruTime.toLocalDateTime();
            LocalDateTime fechaFin = peruLocalDateTime.plus(5, ChronoUnit.DAYS);
            Integer idFarmacista = new Integer(120); //el id del Farmacista
            Sede s = sedeRepository.getById(ordenAntigua.getSede().getIdSede()); //el id de la Sede
            Doctor doc = doctorRepository.getById(ordenAntigua.getDoctor().getIdDoctor()); //el id del doctor

            Orden orden = new Orden();
            orden.setIdOrden(ordenRepository.findLastOrdenId()+1);
            orden.setTracking(tracking);
            orden.setFechaIni(peruLocalDateTime);
            orden.setFechaFin(fechaFin);

            Float priceTotal = 0.0F;

            for (OrdenContenido ordenContenido : medicamentosSinStock) {
                BigDecimal precioVenta = ordenContenido.getIdMedicamento().getPrecioVenta();
                int cantidad = ordenContenido.getCantidad();

                // Multiplicar BigDecimal por int y convertir el resultado a float
                float precioVentaFloat = precioVenta.floatValue();
                float subtotal = precioVentaFloat * cantidad;

                // Acumular el resultado
                priceTotal += subtotal;
            }

            System.out.println(priceTotal);

            orden.setPrecioTotal(priceTotal);
            orden.setIdFarmacista(idFarmacista);
            orden.setPaciente(ordenAntigua.getPaciente());
            orden.setTipoOrden(3);
            orden.setEstadoOrden(2);
            orden.setSede(s);
            orden.setOrdenParent(ordenAntigua.getIdOrden());
            orden.setDoctor(doc);
            orden.setEstadoPreOrden(1);
            orden.setSeguroUsado(ordenAntigua.getSeguroUsado());
            ordenRepository.save(orden);


            //Contenido de la pre orden:
            Integer cantidad = new Integer(0);
            OrdenContenido ordenContenido = new OrdenContenido();
            OrdenContenidoId oid = new OrdenContenidoId();
            ordenContenido.setIdOrden(orden);
            oid.setIdOrden(orden.getIdOrden());

            for(OrdenContenido med : medicamentosSinStock){
                oid.setIdMedicamento(med.getIdMedicamento().getIdMedicamento());
                ordenContenido.setId(oid);
                ordenContenido.setIdMedicamento(med.getIdMedicamento());
                ordenContenido.setCantidad(med.getCantidad());
                ordenContenidoRepository.save(ordenContenido);
            }


            //Eliminar los medicamentos de la orden antigua:
            List<OrdenContenido> lista2 = ordenContenidoRepository.findMedicamentosByOrdenId(String.valueOf(idOrden));
            for(OrdenContenido oc : lista2){
                if(oc.getCantidad() > sedeStockRepository.verificarCantidadStockPorSede( ordenRepository.getOrdenByIdOrden(Integer.parseInt(String.valueOf(idOrden))).getSede().getIdSede(), oc.getIdMedicamento().getIdMedicamento())    ){
                    ordenContenidoRepository.borrarContenidoOrden(oc.getIdMedicamento().getIdMedicamento(), idOrden);
                }
            }



            if(medicamentosEnStock.size()==0){
                System.out.println("La orden antigua se quedó vacia");
                ordenAntigua.setPrecioTotal(0);
                ordenAntigua.setEstadoOrden(9);
                ordenAntigua.setMotivoAnulado("El contenido de esta orden pasó a una Pre Orden");

            }else{
                //Recalcular el precio total de la ordenAntigua
                Float priceTotal2 = 0.0F;
                for (OrdenContenido ordenContenido2 : medicamentosEnStock) {
                    BigDecimal precioVenta = ordenContenido2.getIdMedicamento().getPrecioVenta();
                    int cantidad2 = ordenContenido2.getCantidad();
                    float precioVentaFloat = precioVenta.floatValue();
                    float subtotal = precioVentaFloat * cantidad2;
                    priceTotal2 += subtotal;
                }
                ordenAntigua.setPrecioTotal(priceTotal2);
                ordenAntigua.setEstadoOrden(2);
            }



            redirectAttributes.addFlashAttribute("msg", "Orden Creada");


            return "redirect:/farmacista/ordenes_web";
        }else{
            return "redirect:/farmacista/ordenes_web";
        }



    }







    @GetMapping("/farmacista/ordenes_venta")
    public String tablaOrdenesVenta(Model model,
                                    HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        HttpSession session = request.getSession();
        usuarioSession = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuarioSession);

        sedeSession = sedeFarmacistaRepository.buscarFarmacistaSede(usuarioSession.getIdUsuario()).getIdSede();

        sedeSession = sedeFarmacistaRepository.buscarFarmacistaSede(usuarioSession.getIdUsuario()).getIdSede();
        List<Orden> listaOrdenesVenta = ordenRepository.findAllOrdenesPorSede(sedeSession.getIdSede());
        model.addAttribute("listaOrdenesVenta", listaOrdenesVenta);
        return "farmacista/ordenes_venta";
    }
    @GetMapping("/farmacista/ordenes_web")
    public String tablaOrdenesWeb(Model model,
                                  HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        HttpSession session = request.getSession();
        usuarioSession = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuarioSession);

        sedeSession = sedeFarmacistaRepository.buscarFarmacistaSede(usuarioSession.getIdUsuario()).getIdSede();

        sedeSession = sedeFarmacistaRepository.buscarFarmacistaSede(usuarioSession.getIdUsuario()).getIdSede();
        List<Orden> listaOrdenesWeb = ordenRepository.findAllOrdenesWebPorSede(sedeSession.getIdSede());
        model.addAttribute("listaOrdenesWeb", listaOrdenesWeb);
        return "farmacista/ordenes_web";
    }
    @GetMapping("/farmacista/pre_ordenes")
    public String tablaPreOrdenes(Model model,
                                  HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        HttpSession session = request.getSession();
        usuarioSession = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuarioSession);

        sedeSession = sedeFarmacistaRepository.buscarFarmacistaSede(usuarioSession.getIdUsuario()).getIdSede();

        sedeSession = sedeFarmacistaRepository.buscarFarmacistaSede(usuarioSession.getIdUsuario()).getIdSede();
        List<Orden> listaPreOrdenes = ordenRepository.findAllPreOrdenesPorSede(sedeSession.getIdSede());
        model.addAttribute("listaPreOrdenes", listaPreOrdenes);
        return "farmacista/pre_ordenes";
    }


    @GetMapping("farmacista/aprobar_orden_web")
    public String aprobarOrdenWeb(Model model, @RequestParam("id") String idOrden,
                                  HttpServletRequest request, HttpServletResponse response, Authentication authentication){

        HttpSession session = request.getSession();
        usuarioSession = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuarioSession);

        sedeSession = sedeFarmacistaRepository.buscarFarmacistaSede(usuarioSession.getIdUsuario()).getIdSede();

        Optional<Orden> ordenWebOptional = ordenRepository.findById(Integer.valueOf(idOrden));
        List<OrdenContenido> contenidoOrdenWeb = ordenContenidoRepository.findMedicamentosByOrdenId(idOrden);

        if (ordenWebOptional.isPresent()){
            Orden ordenWebComprobada = ordenWebOptional.get();

            ordenWebComprobada.setEstadoOrden(2);

            ordenRepository.save(ordenWebComprobada);

            model.addAttribute("idOrden", idOrden);
            model.addAttribute("contenidoOrden", contenidoOrdenWeb);
            model.addAttribute("orden",ordenWebComprobada);
            return "farmacista/tracking-2";
        } else {
            return "farmacista/errorPages/no_existe_orden";
        }
    }

    @GetMapping("farmacista/ver_preorden_tracking")
    public String verPreOrdenTracking(@RequestParam("id") String idPreOrden,
                                      HttpServletRequest request, HttpServletResponse response, Authentication authentication){

        HttpSession session = request.getSession();
        usuarioSession = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuarioSession);

        sedeSession = sedeFarmacistaRepository.buscarFarmacistaSede(usuarioSession.getIdUsuario()).getIdSede();

        Orden preOrden = ordenRepository.findPreordenById(Integer.valueOf(idPreOrden));
        String idOrdenParent = String.valueOf(preOrden.getOrdenParent());

        return "redirect:/farmacista/ver_orden_tracking?id=" + idOrdenParent;
    }

    @GetMapping("farmacista/ver_orden_tracking")
    public String verOrdenTracking(Model model, @RequestParam("id") String idOrden,
                                   HttpServletRequest request, HttpServletResponse response, Authentication authentication,
                                   RedirectAttributes attr){

        HttpSession session = request.getSession();
        usuarioSession = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuarioSession);

        sedeSession = sedeFarmacistaRepository.buscarFarmacistaSede(usuarioSession.getIdUsuario()).getIdSede();

        Optional<Orden> ordenOptional = ordenRepository.findById(Integer.valueOf(idOrden));

        if (ordenOptional.isPresent()){

            Orden ordenComprobada = ordenOptional.get();

            if (ordenComprobada.getSede().getIdSede() == sedeSession.getIdSede()){
                List<OrdenContenido> contenidoOrden = ordenContenidoRepository.findMedicamentosByOrdenId(idOrden);

                boolean containsPreOrden = false;
                Orden preOrdenChild = ordenRepository.findPreordenByOrdenId(Integer.valueOf(idOrden));

                ArrayList<OrdenContenido> contenidoPreOrden = new ArrayList<>();

                if (preOrdenChild != null) {
                    containsPreOrden = true;
                    contenidoPreOrden = (ArrayList<OrdenContenido>) ordenContenidoRepository.findMedicamentosByOrdenId(String.valueOf(preOrdenChild.getIdOrden()));
                }

                System.out.println("La orden tiene preorden?" + containsPreOrden);

                // Falta detectar y enviar si la orden tiene problemas de Stock

                List<OrdenContenido> lista = ordenContenidoRepository.findMedicamentosByOrdenId(idOrden);
                List<OrdenContenido> medsConStock = new ArrayList<>();
                List<OrdenContenido> medsSinStock = new ArrayList<>();
                for(OrdenContenido oc : lista){

                    if (oc.getCantidad() > sedeStockRepository.verificarCantidadStockPorSede(sedeSession.getIdSede(), oc.getIdMedicamento().getIdMedicamento()) ){
                        medsSinStock.add(oc);
                    } else {
                        medsConStock.add(oc);
                    }
                }

                boolean hayProblemasStock = false;

                if (!medsSinStock.isEmpty()){
                    hayProblemasStock = true;
                }

                System.out.println("Hay problemas con Stock?" + hayProblemasStock);

                model.addAttribute("hayProblemasStock", hayProblemasStock);

                model.addAttribute("medsConStock", medsConStock);
                model.addAttribute("medsSinStock", medsSinStock);

                model.addAttribute("containsPreOrden", containsPreOrden);

                model.addAttribute("preOrden", preOrdenChild);
                model.addAttribute("contenidoPreOrden", contenidoPreOrden);
                Usuario userSession = (Usuario) session.getAttribute("usuario");
                model.addAttribute("usuario", userSession);
                model.addAttribute("idOrden", idOrden);
                model.addAttribute("contenidoOrden", contenidoOrden);
                model.addAttribute("orden", ordenComprobada);



                return "farmacista/tracking-2";
            } else {
                attr.addFlashAttribute("msg", "La orden buscada no ha sido encontrada.");
                return "redirect:/farmacista/ordenes_venta";
            }
        } else {
            attr.addFlashAttribute("msg", "La orden buscada no ha sido encontrada.");
            return "redirect:/farmacista/ordenes_venta";
        }
    }


    @PostMapping("/farmacista/anular_orden")
    public String anularBoleta(@RequestParam("id") String idOrden,
                               @RequestParam("motivo") String motivo,
                               HttpServletRequest request, HttpServletResponse response, Authentication authentication,
                               RedirectAttributes attr){

        HttpSession session = request.getSession();
        usuarioSession = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuarioSession);

        sedeSession = sedeFarmacistaRepository.buscarFarmacistaSede(usuarioSession.getIdUsuario()).getIdSede();

        Optional<Orden> ordenOptional = ordenRepository.findById(Integer.valueOf(idOrden));

        if (ordenOptional.isPresent()){
            Orden ordenComprobada = ordenOptional.get();

            ordenComprobada.setEstadoOrden(9);
            ordenComprobada.setMotivoAnulado(motivo);

            Orden preOrdenChild = ordenRepository.findPreordenByOrdenId(Integer.valueOf(idOrden));

            if (preOrdenChild != null) {
                preOrdenChild.setEstadoPreOrden(9);
                preOrdenChild.setMotivoAnulado(motivo);
            }

            //---------RESTABLECER EL STOCK-----------------//
            List<OrdenContenido> medicamentosPedidos = ordenContenidoRepository.findMedicamentosByOrdenId(idOrden);
            Integer idSede = ordenComprobada.getSede().getIdSede();

            for(OrdenContenido oc : medicamentosPedidos){
                sedeStockRepository.aumentarStockPorSede(idSede, oc.getIdMedicamento().getIdMedicamento(),oc.getCantidad());
            }
            //---------------------------------------------//





            attr.addFlashAttribute("msg", "La orden ha sido anulada exitosamente.");
            return "redirect:/farmacista/ver_orden_tracking?id=" + idOrden;

        } else {
            attr.addFlashAttribute("msg", "Ha ocurrido un error al tratar de anular una orden.");
            return "redirect:/farmacista/ordenes_venta";
        }
    }


    @GetMapping("/farmacista/ver_boleta")
    public String verBoleta(Model model, @RequestParam("id") String idOrden,
                            HttpServletRequest request, HttpServletResponse response, Authentication authentication,
                            RedirectAttributes attr) {

        HttpSession session = request.getSession();
        usuarioSession = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuarioSession);

        sedeSession = sedeFarmacistaRepository.buscarFarmacistaSede(usuarioSession.getIdUsuario()).getIdSede();

        Optional<Orden> ordenOptional = ordenRepository.findById(Integer.valueOf(idOrden));
        List<OrdenContenido> contenidoOrden = ordenContenidoRepository.findMedicamentosByOrdenId(idOrden);

        if (ordenOptional.isPresent()){
            Orden ordenComprobada = ordenOptional.get();

            if (ordenComprobada.getSede().getIdSede() == sedeSession.getIdSede()){

                if (ordenComprobada.getEstadoOrden() == 1 || ordenComprobada.getEstadoOrden() == 2){
                    attr.addFlashAttribute("msg", "Esta orden todavía no ha sido pagada o procesada.");
                    return "redirect:/farmacista/ordenes_venta";
                } else {
                    model.addAttribute("orden",ordenComprobada);
                    model.addAttribute("contenidoOrden", contenidoOrden);
                    return "farmacista/boleta";
                }

            } else {
                attr.addFlashAttribute("msg", "La orden buscada no ha sido encontrada.");
                return "redirect:/farmacista/ordenes_venta";
            }

        } else {
            attr.addFlashAttribute("msg", "La orden buscada no ha sido encontrada.");
            return "redirect:/farmacista/ordenes_venta";
        }
    }

    @GetMapping("/farmacista/facturacion")
    public String facturacion(Model model) {
        List<MedicamentosSedeStockDto> medicamentosLowStock = medicamentoRepository.findMedicamentosLowStock(1);
        model.addAttribute("medicamentosLowStock", medicamentosLowStock);
        return "farmacista/facturacion";
    }


    private boolean isValidPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false; // Contraseña es nula o está en blanco
        }
        // Al menos una mayúscula, una minúscula, un caracter especial y mínimo 8 caracteres
        String regex = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    @GetMapping("farmacista/perfil")
    public String cambiarContrasena(Model model,
                                    HttpServletRequest request, HttpServletResponse response, Authentication authentication){

        HttpSession session = request.getSession();
        usuarioSession = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuarioSession);

        String passwordDots = "*************";
        model.addAttribute("farmacista", usuarioSession);
        model.addAttribute("contrasena", passwordDots);
        return "perfil";
    }

    @PostMapping("/farmacista/actualizar_contrasena")
    public String actualizarContrasena(Usuario farmacista, BindingResult bindingResult,
                                     @RequestParam(value = "newContrasena", required = true) String newContrasena,
                                     @RequestParam(value = "confirmContrasena", required = true) String confirmContrasena,
                                     @RequestParam(value = "oldContrasena", required = true) String oldContrasena,
                                     RedirectAttributes attr, Model model,
                                     HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        HttpSession session = request.getSession();
        usuarioSession = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuarioSession);

        if (SHA256.verifyPassword(oldContrasena, usuarioSession.getContrasena())){

            if (newContrasena.equals(confirmContrasena)){

                if (isValidPassword(newContrasena)){
                    usuarioRepository.actualizarContrasenaUsuario(SHA256.cipherPassword(newContrasena), usuarioSession.getIdUsuario());
                    usuarioRepository.actualizarEstadoContra(usuarioSession.getIdUsuario());
                    attr.addFlashAttribute("msgSuccess", "Contraseña actualizada correctamente.");
                } else {
                    attr.addFlashAttribute("msg", "Ingrese una contraseña válida. De más de 8 carácteres, con dígitos y carácteres especiales.");
                }
            } else {
                attr.addFlashAttribute("msg", "Las contraseñas no coinciden.");
            }

        } else {
            attr.addFlashAttribute("msg", "Introduzca su contraseña actual.");
        }
        return "redirect:/farmacista/perfil";

    }


    @PostMapping(value="/farmacista/crear_chat")
    public String crearChat(@RequestParam("idOrden") Integer idOrden, @RequestParam("idPaciente") Integer idPaciente, @RequestParam("idFarmacista") Integer idFarmacista){

        System.out.println("idPaciente:" + idPaciente);
        System.out.println("idFarmacista:" + idFarmacista);
        System.out.println("idOrden:" + idOrden);

        Chat verificarChat = chatRepository.buscarChat(idPaciente, idFarmacista,idOrden);


        if(verificarChat == null){

            Integer lastId = chatRepository.findLastChatId();
            if(lastId != null){
                chatRepository.crearChat(lastId+1, idPaciente, idFarmacista, idOrden);
            }else if (lastId == null){
                chatRepository.crearChat(1, idPaciente, idFarmacista, idOrden);
            }

            return "redirect:/farmacista/chat/" + idOrden + "/" +  idFarmacista+ "/" + idPaciente;

        } else{
            return "redirect:/farmacista/mensajeria";
        }

    }


    @GetMapping(value="/farmacista/mensajeria")
    public String mensajeria(HttpSession session, Model model){

        Usuario userSession = (Usuario) session.getAttribute("usuario");;

        List<Chat> lista = chatRepository.listaChatsParaFarmacista(userSession.getIdUsuario());

        model.addAttribute("listaChats", lista);

        return "farmacista/mensajeria";}



    @GetMapping(value = "/farmacista/chat/{idOrden}/{userId1}/{userId2}")
    public String chat(HttpSession session, @PathVariable String userId1, @PathVariable String userId2, @PathVariable String idOrden, Model model) {
        model.addAttribute("userId1", userId1);
        model.addAttribute("userId2", userId2);
        model.addAttribute("idOrden",idOrden);
        model.addAttribute("idOrdenInteger", Integer.parseInt(idOrden) + 10000);
        model.addAttribute("orden",ordenRepository.getOrdenByIdOrden(Integer.valueOf(idOrden)));



        /*----------------------IP LOCAL-------------------------*/
        try {InetAddress localhost = InetAddress.getLocalHost();model.addAttribute("iplocal", localhost.getHostAddress());} catch (UnknownHostException e) {e.printStackTrace();}
        /*------------------------------------------------------*/



        Chat chatActual  = chatRepository.buscarChat(Integer.parseInt(userId2),Integer.parseInt(userId1),Integer.parseInt(idOrden));

        Usuario userSession = (Usuario) session.getAttribute("usuario");

        if ( chatActual != null && userSession != null && (userSession.getIdUsuario().toString().equals(userId1) || userSession.getIdUsuario().toString().equals(userId2))) {

            System.out.println("El usuario pertenece al chat");

            /*----------------------FIREBASE-------------------------*/
            List <ChatFirebase> chats = null;
            try {chats = chatService.getChatsByIdChat(chatActual.getIdChat());}
            catch (InterruptedException | ExecutionException e) {e.printStackTrace();}
            System.out.println(chats);
            /*------------------------------------------------------*/

            Usuario paciente = usuarioRepository.getById(Integer.parseInt(userId2));

            List<OrdenContenido> lista = ordenContenidoRepository.findMedicamentosByOrdenId(idOrden);
            List<OrdenContenido> medicamentosEnStock = new ArrayList<>();
            List<OrdenContenido> medicamentosSinStock = new ArrayList<>();
            for(OrdenContenido oc : lista){
                if(oc.getCantidad() > sedeStockRepository.verificarCantidadStockPorSede(  ordenRepository.getOrdenByIdOrden(Integer.parseInt(idOrden)).getSede().getIdSede(), oc.getIdMedicamento().getIdMedicamento())    ){
                    medicamentosSinStock.add(oc);
                }else{
                    medicamentosEnStock.add(oc);
                }
            }

            List<Medicamento> listaMedicamentos = medicamentoRepository.findAll();

            model.addAttribute("listaMedicamentos", listaMedicamentos);
            model.addAttribute("medicamentosSinStock", medicamentosSinStock);
            model.addAttribute("medicamentosEnStock", medicamentosEnStock);
            model.addAttribute("idChat", chatActual.getIdChat());
            model.addAttribute("idUser", userSession.getIdUsuario());
            model.addAttribute("paciente", paciente);
            model.addAttribute("mensajes", chats);


            return "farmacista/chat";
        } else {
            System.out.println("El usuario no pertenece al chat");
            return "farmacista/mensajeria";
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



    public List<Medicamento> getMedicamentosFromListaInteger(List<Integer> listaSelectedIds) {
        List<Optional<Medicamento>> optionals = new ArrayList<>();
        List<Medicamento> seleccionados;
        for (int i = 0; i < listaSelectedIds.size(); i += 2) {
            optionals.add(medicamentoRepository.findById(Integer.valueOf(listaSelectedIds.get(i))));
        }
        seleccionados = optionals.stream().flatMap(Optional::stream).collect(Collectors.toList());

        return seleccionados;
    }

    public List<String> getCantidadesFromListaInteger(List<Integer> listaSelectedIds) {
        List<String> cantidades = new ArrayList<>();
        for (int i = 0; i + 1 < listaSelectedIds.size(); i += 2) {
            cantidades.add(String.valueOf(listaSelectedIds.get(i + 1)));
        }
        return cantidades;
    }


    @Getter
    public class verificationUser {

        private final boolean userExist;
        private final Usuario user;

        public verificationUser(String name, String lastname, String dni, String distrito, String direccion, String seguro, String correo, String celular) {

            boolean userExist = false;
            Usuario user = new Usuario();

            Optional<Usuario> userOptional = usuarioRepository.findPacienteByDni(dni);

            if (userOptional.isPresent()) {

                userExist = true;
                user = userOptional.get();

                if (user.getEstadoUsuario().equals(2)){
                    user.setCelular(celular);
                    user.setDireccion(direccion);
                    user.setDistrito(distrito);
                    user.setSeguro(Objects.requireNonNullElse(seguro, "false"));
                    user.setCorreo(correo);
                    usuarioRepository.save(user);
                } else {
                    user.setSeguro(Objects.requireNonNullElse(seguro, "false"));
                    usuarioRepository.save(user);
                }

            } else {
                Usuario newUser = new Usuario();
                newUser.setRol(4);

                newUser.setCorreo(correo);
                newUser.setContrasena("00000000");
                newUser.setNombres(name);
                newUser.setApellidos(lastname);
                newUser.setCelular(celular);
                newUser.setDni(dni);
                newUser.setDireccion(direccion);
                newUser.setDistrito(distrito);
                newUser.setSeguro(Objects.requireNonNullElse(seguro, "false"));
                newUser.setEstadoUsuario(2);

                user = newUser;
                usuarioRepository.save(user);
            }


            System.out.println(user.getIdUsuario());

            this.userExist = userExist;
            this.user = user;
        }
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
            sedeStockId.setIdSede(sedeSession.getIdSede());

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

                    } else {
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

}


