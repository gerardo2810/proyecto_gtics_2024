package pe.sanmiguel.bienestar.proyecto_gtics.Controller;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.sanmiguel.bienestar.proyecto_gtics.Dto.MedicamentosSedeStockDto;
import pe.sanmiguel.bienestar.proyecto_gtics.EmailService;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.*;
import pe.sanmiguel.bienestar.proyecto_gtics.Repository.*;
import pe.sanmiguel.bienestar.proyecto_gtics.SHA256;
import pe.sanmiguel.bienestar.proyecto_gtics.ValidationGroup.FarmacistaValidationsGroup;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
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

    public FarmacistaController(UsuarioRepository usuarioRepository, SedeRepository sedeRepository, SedeStockRepository sedeStockRepository, SedeFarmacistaRepository sedeFarmacistaRepository, MedicamentoRepository medicamentoRepository, OrdenRepository ordenRepository, OrdenContenidoRepository ordenContenidoRepository, ReposicionRepository reposicionRepository, EstadoPreOrdenRepository estadoPreOrdenRepository, DoctorRepository doctorRepository) {
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
    }

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

            ArrayList<Usuario> listaUsuarios = (ArrayList<Usuario>) usuarioRepository.listarUsuariosSegunRol(4);
            ArrayList<Doctor> listaDoctores = (ArrayList<Doctor>) doctorRepository.findAll();

            model.addAttribute("listaUsuarios", listaUsuarios);
            model.addAttribute("listaDoctores", listaDoctores);
            model.addAttribute("stockSeleccionados", stockSeleccionados);
            model.addAttribute("medicamentosSeleccionados", medicamentosSeleccionados);
            model.addAttribute("listaCantidades", listaCantidades);

            return "farmacista/formulario_paciente";
        }
    }

    @PostMapping("/farmacista/finalizar_compra")
    public String createOrdenVenta(@ModelAttribute("usuario") @Validated(FarmacistaValidationsGroup.class) Usuario usuario,
                                   BindingResult bindingResult,
                                   Model model,
                                   @RequestParam(value = "nombres") String name,
                                   @RequestParam(value = "apellidos") String lastname,
                                   @RequestParam(value = "dni") String dni,
                                   @RequestParam(value = "distrito") String distrito,
                                   @RequestParam(value = "direccion") String direccion,
                                   @RequestParam(value = "doctor", required = false) String doctor,
                                   @RequestParam(value = "seguro", required = false) String seguro,
                                   @RequestParam(value = "correo") String correo,
                                   @RequestParam(value = "celular") String celular,
                                   @RequestParam(value = "listaIds") List<String> listaSelectedIds,
                                   @RequestParam(value = "priceTotal") String priceTotal,
                                   HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

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

            return "farmacista/formulario_paciente";

        } else {

            idVerOrdenCreada = 0;

            medicamentosSeleccionados = getMedicamentosFromLista(listaSelectedIds);
            listaCantidades = getCantidadesFromLista(listaSelectedIds);

            verificationStock verificationStock = new verificationStock(medicamentosSeleccionados, listaCantidades);
            verificationUser verificationUser = new verificationUser(name,lastname,dni,distrito,direccion,seguro,correo,celular);

            this.pacienteOnStore = verificationUser.getUser();

            if (verificationStock.getMedicamentosSinStock().isEmpty()) {

                LocalDateTime now = LocalDateTime.now();

                Orden newOrden = new Orden();
                newOrden.setFechaIni(now);
                newOrden.setPrecioTotal(Float.parseFloat(priceTotal));
                newOrden.setIdFarmacista(usuarioSession.getIdUsuario());
                newOrden.setTipoOrden(1);
                newOrden.setEstadoOrden(8);
                newOrden.setSede(sedeSession);
                newOrden.setSeguroUsado(Objects.requireNonNullElse(seguro, "false"));


                if (!(doctor == null)) {
                    if (!doctor.isEmpty() && !doctor.equals("no-doctor")) {
                        newOrden.setDoctor(doctorRepository.getByIdDoctor(Integer.valueOf(doctor)));
                    }
                }

                newOrden.setPaciente(this.pacienteOnStore);

                ordenRepository.save(newOrden);

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



    @PostMapping("/farmacista/finalizar_compra_preorden")
    public String createOrdenVenta() {

        idVerOrdenCreada = 6; // Ejemplo fijo, falta realizar comprobaciones y llenado a base de datos para hacerlo dinámico

        return "redirect:/farmacista/ver_pre_orden";

    }




    @GetMapping("/farmacista/crear_preorden")
    public String createPreOrden(Model model) {

        model.addAttribute("medicamentosSinStock", medicamentosSinStock);
        model.addAttribute("medicamentosConStock", medicamentosConStock);
        model.addAttribute("cantidadesFaltantes", cantidadesFaltantes);
        model.addAttribute("cantidadesExistentes", cantidadesExistentes);

        return "farmacista/crear_preorden";
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

            Optional<Usuario> userOptional = usuarioRepository.findPacienteByCorreoAndDni(correo, dni);

            if (userOptional.isPresent()) {
                userExist = true;
                user = userOptional.get();

                user.setNombres(name);
                user.setApellidos(lastname);
                user.setCelular(celular);
                user.setDireccion(direccion);
                user.setDistrito(distrito);
                user.setSeguro(Objects.requireNonNullElse(seguro, "false"));
                usuarioRepository.save(user);

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
            }

            usuarioRepository.save(user);

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


