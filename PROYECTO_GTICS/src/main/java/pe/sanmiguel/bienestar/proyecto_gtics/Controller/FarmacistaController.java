package pe.sanmiguel.bienestar.proyecto_gtics.Controller;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
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
                                   HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

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
                return "errorPage"; // O alguna página de error apropiada
            }
        }

        List<Medicamento> listaMedicamentos = medicamentoRepository.findAll();
        int numeroOrdenesPendientes = 0;
        model.addAttribute("sedeSession", sedeSession);
        model.addAttribute("listaMedicamentos", listaMedicamentos);
        model.addAttribute("numeroOrdenesPendientes", numeroOrdenesPendientes);
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

            ArrayList<Usuario> listaUsuarios = (ArrayList<Usuario>) usuarioRepository.listarUsuariosSegunRol(4);
            ArrayList<Doctor> listaDoctores = (ArrayList<Doctor>) doctorRepository.findAll();

            model.addAttribute("listaUsuarios", listaUsuarios);
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

                    System.out.println(values);
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

                Orden newOrden = new Orden();
                newOrden.setFechaIni(now);
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

                    ordenContenidoRepository.save(contenido);
                    i++;
                }

                idVerOrdenCreada = newOrden.getIdOrden();
                return "redirect:/farmacista/ver_orden_tracking?id=" + idVerOrdenCreada;

            } else {

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

        model.addAttribute("medicamentosSinStock", medicamentosSinStock);
        model.addAttribute("medicamentosConStock", medicamentosConStock);
        model.addAttribute("cantidadesFaltantes", cantidadesFaltantes);
        model.addAttribute("cantidadesExistentes", cantidadesExistentes);

        return "farmacista/crear_preorden";
    }

    //BOLETA DE PRE ORDNES//
    @GetMapping("/farmacista/deprecated/ver_pre_orden")
    public String verPreOrden(Model model,
                              HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        /*model.addAttribute("medicamentosSinStock", medicamentosSinStock);
        model.addAttribute("medicamentosConStock", medicamentosConStock);
        model.addAttribute("cantidadesFaltantes", cantidadesFaltantes);
        model.addAttribute("cantidadesExistentes", cantidadesExistentes);*/

        HttpSession session = request.getSession();
        usuarioSession = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuarioSession);

        sedeSession = sedeFarmacistaRepository.buscarFarmacistaSede(usuarioSession.getIdUsuario()).getIdSede();

        sedeSession = sedeFarmacistaRepository.buscarFarmacistaSede(usuarioSession.getIdUsuario()).getIdSede();
        List<Orden> listaOrdenesVenta = ordenRepository.findAllOrdenesPorSede(sedeSession.getIdSede());
        model.addAttribute("listaOrdenesVenta", listaOrdenesVenta);
        return "/farmacista/deprecated/ver_pre_orden";
    }

    //creamos pre orden
    @GetMapping("/farmacista/crear_pre_orden")
    public String crearPreOrden(Model model,
                              HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        model.addAttribute("medicamentosSinStock", medicamentoRepository.findAll());
        model.addAttribute("medicamentosConStock", medicamentoRepository.findAll());
        model.addAttribute("cantidadesFaltantes", cantidadesFaltantes);
        model.addAttribute("cantidadesExistentes", cantidadesExistentes);

        return "/farmacista/crear_pre_orden";
    }

    @GetMapping("/farmacista/cambiar_medicamentos")
    public String changeMedicamentos(Model model) {

        model.addAttribute("medicamentosSinStock", medicamentosSinStock);
        model.addAttribute("medicamentosConStock", medicamentosConStock);
        model.addAttribute("cantidadesFaltantes", cantidadesFaltantes);
        model.addAttribute("cantidadesExistentes", cantidadesExistentes);

        return "farmacista/cambiar_medicamentos";
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
            return "farmacista/tracking";
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

        Orden preOrden = ordenRepository.findPreordenByOrdenId(Integer.valueOf(idPreOrden));
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

                model.addAttribute("containsPreOrden", containsPreOrden);

                model.addAttribute("preOrden", preOrdenChild);
                model.addAttribute("contenidoPreOrden", contenidoPreOrden);
                Usuario userSession = (Usuario) session.getAttribute("usuario");
                model.addAttribute("usuario", userSession);
                model.addAttribute("idOrden", idOrden);
                model.addAttribute("contenidoOrden", contenidoOrden);
                model.addAttribute("orden", ordenComprobada);
                return "farmacista/tracking";
            } else {
                attr.addFlashAttribute("msg", "La orden buscada no ha sido encontrada.");
                return "redirect:/farmacista/ordenes_venta";
            }
        } else {
            attr.addFlashAttribute("msg", "La orden buscada no ha sido encontrada.");
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

                model.addAttribute("orden",ordenComprobada);
                model.addAttribute("contenidoOrden", contenidoOrden);
                return "farmacista/boleta";

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

        Chat verificarChat = chatRepository.buscarChat(idPaciente, idFarmacista);


        if(verificarChat == null){

            Integer lastId = chatRepository.findLastChatId();
            if(lastId != null){
                chatRepository.crearChat(lastId+1, idPaciente, idFarmacista);
            }else if (lastId == null){
                chatRepository.crearChat(1, idPaciente, idFarmacista);
            }

            return "redirect:/farmacista/chat/" + idFarmacista+ "/" + idPaciente;

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



    @GetMapping(value = "/farmacista/chat/{userId1}/{userId2}")
    public String chat(HttpSession session, @PathVariable String userId1, @PathVariable String userId2, Model model) {
        model.addAttribute("userId1", userId1);
        model.addAttribute("userId2", userId2);

        Chat chatActual  = chatRepository.buscarChat(Integer.parseInt(userId2),Integer.parseInt(userId1));



        /*----------------------IP LOCAL-------------------------*/
        try {InetAddress localhost = InetAddress.getLocalHost();
            System.out.println("Mi dirección IP local es: " + localhost.getHostAddress());
            model.addAttribute("iplocal", localhost.getHostAddress());} catch (UnknownHostException e) {
            System.out.println("Error obteniendo la dirección IP local");
            e.printStackTrace();}
        /*------------------------------------------------------*/

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


