package pe.sanmiguel.bienestar.proyecto_gtics.Controller;

import com.lowagie.text.DocumentException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.sanmiguel.bienestar.proyecto_gtics.DniAPI;
import pe.sanmiguel.bienestar.proyecto_gtics.Dto.OrdenesExporterDto;
import pe.sanmiguel.bienestar.proyecto_gtics.Dto.TopVentasDto;
import pe.sanmiguel.bienestar.proyecto_gtics.Dto.VentasMedicamentosTotalDto;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.*;
import pe.sanmiguel.bienestar.proyecto_gtics.PasswordService;
import pe.sanmiguel.bienestar.proyecto_gtics.Repository.*;
import pe.sanmiguel.bienestar.proyecto_gtics.SHA256;
import pe.sanmiguel.bienestar.proyecto_gtics.ValidationGroup.DniApiValidationGroup;
import pe.sanmiguel.bienestar.proyecto_gtics.ValidationGroup.SuperAdminValidationsGroup;
import pe.sanmiguel.bienestar.proyecto_gtics.util.reportes.ExporterExcel;
import pe.sanmiguel.bienestar.proyecto_gtics.util.reportes.ExporterPDF;


import javax.naming.Context;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.io.IOException;
import java.io.InputStream;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping(value = {"/superadmin"}, method = RequestMethod.GET)
public class SuperAdminController {

    private int idUsuario;
    private UserDetailsService userDetailsService;
    final UsuarioRepository usuarioRepository;
    final DoctorRepository doctorRepository;
    final SedeRepository sedeRepository;
    final SedeFarmacistaRepository sedeFarmacistaRepository;
    final ReposicionRepository reposicionRepository;
    final ReposicionContenidoRepository reposicionContenidoRepository;
    final EstadoUsuarioRepository estadoUsuarioRepository;
    final MedicamentoRepository medicamentoRepository;
    final SedeStockRepository sedeStockRepository;
    final SedeDoctorRepository sedeDoctorRepository;
    final OrdenContenidoRepository ordenContenidoRepository;
    final OrdenRepository ordenRepository;


    public SuperAdminController(UsuarioRepository usuarioRepository, DoctorRepository doctorRepository, SedeRepository sedeRepository, SedeFarmacistaRepository sedeFarmacistaRepository, ReposicionRepository reposicionRepository, ReposicionContenidoRepository reposicionContenidoRepository, EstadoUsuarioRepository estadoUsuarioRepository, MedicamentoRepository medicamentoRepository, SedeStockRepository sedeStockRepository, SedeDoctorRepository sedeDoctorRepository, OrdenContenidoRepository ordenContenidoRepository, OrdenRepository ordenRepository) {
        this.usuarioRepository = usuarioRepository;
        this.doctorRepository = doctorRepository;
        this.sedeRepository = sedeRepository;
        this.sedeFarmacistaRepository = sedeFarmacistaRepository;
        this.reposicionRepository = reposicionRepository;
        this.reposicionContenidoRepository = reposicionContenidoRepository;
        this.estadoUsuarioRepository = estadoUsuarioRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.sedeStockRepository = sedeStockRepository;
        this.sedeDoctorRepository = sedeDoctorRepository;
        this.ordenContenidoRepository = ordenContenidoRepository;
        this.ordenRepository = ordenRepository;
    }

    Usuario usuarioSession;
    Integer idVerReposicionCreada;
    Integer idEstadoFarmacista;

    @GetMapping(value = {""})
    public String showIndexSuperAdmin(Model model,
                                      HttpServletRequest request, HttpServletResponse response, Authentication authentication){

        //SESSION
        //Iniciamos la sesión
        HttpSession session = request.getSession();
        usuarioSession  = usuarioRepository.findByCorreo(authentication.getName());
        if (usuarioSession == null) {
            usuarioSession = usuarioRepository.findByCorreo(authentication.getName());
            session.setAttribute("usuario", usuarioSession);
        }
        List<Sede> adminSedelist = sedeRepository.listarAdministroresSede();
        List<SedeFarmacista> farmacistaList = sedeFarmacistaRepository.listarFarmacistasPorSede();
        List<Usuario> pacientelist = usuarioRepository.listarUsuariosSegunRol(4);
        List<Doctor> doctorList = doctorRepository.findAll();
        List<OrdenesExporterDto> listaOrdenesReportes = ordenRepository.listarOrdenesExporter();

        model.addAttribute("listaOrdenes", listaOrdenesReportes);
        model.addAttribute("adminSedelist", adminSedelist);
        model.addAttribute("farmacistlist", farmacistaList);
        model.addAttribute("pacientelist", pacientelist);
        model.addAttribute("doctorList", doctorList);
        model.addAttribute("usuario",usuarioSession);

        return "superAdmin/paginaInicio";
    }
    @GetMapping(value = {"/hola"})
    public String hola(Model model,
                                      HttpServletRequest request, HttpServletResponse response, Authentication authentication){
        //SESSION
        //Iniciamos la sesión
        HttpSession session = request.getSession();
        usuarioSession  = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuarioSession);

        List<Sede> adminSedelist = sedeRepository.listarAdministroresSede();
        List<SedeFarmacista> farmacistaList = sedeFarmacistaRepository.listarFarmacistasPorSede();
        List<Usuario> pacientelist = usuarioRepository.listarUsuariosSegunRol(4);
        List<Doctor> doctorList = doctorRepository.findAll();
        model.addAttribute("adminSedelist", adminSedelist);
        model.addAttribute("farmacistlist", farmacistaList);
        model.addAttribute("pacientelist", pacientelist);
        model.addAttribute("doctorList", doctorList);
        model.addAttribute("usuario",usuarioSession);
        return "superAdmin/hola";
    }

    @GetMapping(value = {"/administradoresSede"})
    public String showAdministradoresSede(Model model,
                                          HttpServletRequest request, HttpServletResponse response, Authentication authentication){
        //SESSION
        //Iniciamos la sesión
        HttpSession session = request.getSession();
        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuario);

        List<Sede> adminSedelist = sedeRepository.listarAdministroresSede();
        List<Sede> sedesDisp = sedeRepository.listarSedesDisponibles();
        List<Usuario> adminSinSede = usuarioRepository.listarAdministradoresSinSede();
        List<Usuario> adminBaneados = usuarioRepository.listarAdministradoresBaneados();
        List<Sede> sedeList = sedeRepository.findAll();
        model.addAttribute("adminSedelist", adminSedelist);
        model.addAttribute("sedeList", sedeList);
        model.addAttribute("sedesDisp", sedesDisp);
        model.addAttribute("adminSinSede", adminSinSede);
        model.addAttribute("adminBaneados", adminBaneados);
        return "superAdmin/listaAdministSede";
    }
    @GetMapping(value = {"/farmacistas"})
    public String showFarmacistas(Model model,
                                  HttpServletRequest request, HttpServletResponse response, Authentication authentication){
        //SESSION
        //Iniciamos la sesión
        HttpSession session = request.getSession();
        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuario);

        List<SedeFarmacista> farmacistaList = sedeFarmacistaRepository.listarFarmacistasActivosInactivos();
        List<Sede> sedeList = sedeRepository.findAll();
        model.addAttribute("farmacistlist", farmacistaList);
        model.addAttribute("sedeList", sedeList);
        return "superAdmin/listaFarmacistas";
    }

    @GetMapping(value = {"/pacientes"})
    public String showPacientes(Model model,
                                HttpServletRequest request, HttpServletResponse response, Authentication authentication){
        //SESSION
        //Iniciamos la sesión
        HttpSession session = request.getSession();
        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuario);

        List<Usuario> pacientelist = usuarioRepository.listarUsuariosSegunRol(4);
        model.addAttribute("pacientelist", pacientelist);
        return "superAdmin/listaPacientes";
    }

    @GetMapping(value = {"/doctores"})
    public String showDoctores(Model model,
                               HttpServletRequest request, HttpServletResponse response, Authentication authentication){
        //SESSION
        //Iniciamos la sesión
        HttpSession session = request.getSession();
        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuario);

        List<Doctor> doctorList = doctorRepository.findAll();
        model.addAttribute("doctorList", doctorList);
        return "superAdmin/listaDoctores";
    }

    @GetMapping(value = {"/pedidos"})
    public String showPedidos(Model model,
                              HttpServletRequest request, HttpServletResponse response, Authentication authentication){
        //SESSION
        //Iniciamos la sesión
        HttpSession session = request.getSession();
        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuario);

        List<Reposicion> reposicionList = reposicionRepository.listarOrdenesReposicion();
        model.addAttribute("reposicionList", reposicionList);
        return "superAdmin/pedidos";
    }

    @PostMapping("/verOrdenReposicionId")
    public String verOrdenesReposicionID(@RequestParam(value = "idReposicion") String idReposicionID){
        idVerReposicionCreada = Integer.valueOf(idReposicionID);
        return "redirect:/superadmin/masdetallesPedidos";
    }


    @PostMapping("/estadoSolicitudFarmacista")
    public String verEstadoSolicitud(@RequestParam(value = "estadoSolicitud") int idEstadoSolicitud,
                                     @RequestParam(value = "correo") String correoSolicitud,
                                     @RequestParam(value = "idUsuario") int idUsuario, RedirectAttributes attr){
        System.out.println("Valor de Estado solicitud: " + idEstadoSolicitud);
        System.out.println("Valor de id farmacista: " + idUsuario);
        System.out.println("correo del farmacista: " + correoSolicitud);
        String contrasena = usuarioRepository.contraAdmin(idUsuario);

        if(idEstadoSolicitud==1){

            try {
                sendTemporaryPasswordEmailFarmacista(correoSolicitud, contrasena);
            } catch (MailSendException e) {
                sedeFarmacistaRepository.denegarSolicitud(idUsuario);
                attr.addFlashAttribute("msg1", "Correo no enviado, solicitar al Administrador de Sede que verifique la validez del correo. Farmacista rechazado");
                return "redirect:/superadmin/solicitudes";
            } catch (MessagingException e) {
                System.err.println("Error al enviar o recibir correo: " + e.getMessage());
                e.printStackTrace();
            }

            sedeFarmacistaRepository.aprobarSolicitud(idUsuario);
            attr.addFlashAttribute("msg", "Solicitud aceptada exitosamente");
            String hashedPassword = SHA256.cipherPassword(contrasena);
            usuarioRepository.actualizarContrasenaFarmacista(hashedPassword, idUsuario);
            usuarioRepository.actualizarEstadoFarmacista(idUsuario);

            //Se le envía el correo con la contraseña, luego de eso necesita de un timer de 3 minutos para cambiarla
            long tiempoEnvio = System.currentTimeMillis(); //tiempo donde se envía

            //Por temas de presentación lo pongo en 3 minuto nomás

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Usuario usuario = usuarioRepository.encontrarFarmacistaporId(idUsuario);
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - tiempoEnvio >= 3 * 60 * 1000 && usuario.getEstadoContra() == 0) {
                        // Si han pasado 3 minutos y la contraseña no ha sido cambiada
                        String expiredPassword = "noRegistrado";
                        String passwordHashNoRegistrado = SHA256.cipherPassword(expiredPassword);
                        usuarioRepository.actualizarContrasenaFarmacista(passwordHashNoRegistrado, idUsuario);
                        System.out.println("La contraseña ha expirado para " + usuario.getCorreo());
                    }
                }
            }, 3 * 60 * 1000); //Se ejecuta esta funcion luego de 1 minuto



            return "redirect:/superadmin/solicitudes";
        }else {
            sedeFarmacistaRepository.denegarSolicitud(idUsuario);
            attr.addFlashAttribute("msg", "Solicitud rechazada exitosamente");
            return "redirect:/superadmin/solicitudes";
        }
    }

    @GetMapping(value = {"/masdetallesPedidos"})
    public String masDetallesPedidos(Model model,
                                     HttpServletRequest request, HttpServletResponse response, Authentication authentication){
        //SESSION
        //Iniciamos la sesión
        HttpSession session = request.getSession();
        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuario);

        Optional<Reposicion> optionalReposicion = reposicionRepository.findById(idVerReposicionCreada);
        List<ReposicionContenido> contenidoReposicion = reposicionContenidoRepository.buscarMedicamentosByReposicionId(String.valueOf(idVerReposicionCreada));

        if (optionalReposicion.isPresent()){
            Reposicion reposicionComprobada = optionalReposicion.get();
            model.addAttribute("reposicion",reposicionComprobada);
            model.addAttribute("contenidoReposicion", contenidoReposicion);
            return "superAdmin/masdetallesPedidos";
        } else {
            return "superAdmin/pedidos";
        }
    }


    @GetMapping(value = {"/solicitudes"})
    public String showSolicitudes(Model model,
                                  HttpServletRequest request, HttpServletResponse response, Authentication authentication){
        //SESSION
        //Iniciamos la sesión
        HttpSession session = request.getSession();
        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuario);

        List<SedeFarmacista> solicitudesList = sedeFarmacistaRepository.listarSolicitudesFarmacistas();
        List<Sede> sedeList = sedeRepository.findAll();
        model.addAttribute("solicitudesList", solicitudesList);
        model.addAttribute("sedeList", sedeList);
        return "superAdmin/solicitudes";
    }

    @GetMapping(value = {"/medicamentos"})
    public String showMedicamentos(@RequestParam(name = "state", required = false) String state, Model model,
                                   HttpServletRequest request, HttpServletResponse response, Authentication authentication){
        //SESSION
        //Iniciamos la sesión
        HttpSession session = request.getSession();
        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuario);

        List<Sede> sedeList = sedeRepository.findAll();

        if(state==null){
            System.out.println("Estoy en stado null");
            List<Medicamento> listaMedicamentos = medicamentoRepository.listarMedicamentosActivos();
            model.addAttribute("listaMedicamentos", listaMedicamentos);
            model.addAttribute("sedes", sedeList);
            return "superAdmin/medicamentos";
        }else{
            System.out.println("Stado es " +state);
            for(Sede sede:sedeList){
                String nombreSede = sede.getNombre();
                if(state.equals(nombreSede)){
                    List<Medicamento> listaMedicamentosSede = medicamentoRepository.listarMedicamentoSede(state);
                    model.addAttribute("listaMedicamentos", listaMedicamentosSede);
                    model.addAttribute("sedes", sedeList);
                    model.addAttribute("estado", state);
                    return "superAdmin/medicamentos";
                }
            }
        }
        return "superAdmin/medicamentos";
    }



    @GetMapping(value = {"/perfil"})
    public String cambiarContraseña(Model model,
                                    HttpServletRequest request, HttpServletResponse response, Authentication authentication){
        //SESSION
        //Iniciamos la sesión
        HttpSession session = request.getSession();
        usuarioSession  = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuarioSession);

        String passwordDots = "*************";
        model.addAttribute("superadmin", usuarioSession);
        model.addAttribute("contrasenia", passwordDots);
        return "perfil";
    }

    @PostMapping("/actualizar_contrasena")
    public String cambiarContrasenia( Usuario superadmin, BindingResult bindingResult,
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
        return "redirect:/superadmin/perfil";
    }

    @GetMapping(value = {"/historialSolicitudes"})
    public String verHistorialSolicitudes(Model model,
                                          HttpServletRequest request, HttpServletResponse response, Authentication authentication){
        //SESSION
        //Iniciamos la sesión
        HttpSession session = request.getSession();
        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuario);

        List<SedeFarmacista> solicitudesAcepRech = sedeFarmacistaRepository.listarSolicitudesAceptadasyRechazadas();
        model.addAttribute("solicitudesAcepRech", solicitudesAcepRech);
        return "superAdmin/historialSolicitudes";
    }

    @GetMapping(value = {"/aprobarSolicitudes"})
    public String aprobarSolicitudes(Model model, @RequestParam("id") int id,
                                     HttpServletRequest request, HttpServletResponse response, Authentication authentication){
        //SESSION
        //Iniciamos la sesión
        HttpSession session = request.getSession();
        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuario);

        Usuario usuarioFarmacista = usuarioRepository.encontrarFarmacistaporIdActivosInactivos(id);
        Optional<SedeFarmacista> optionalSedeFarmacista = sedeFarmacistaRepository.buscarCodigoFarmacista(id);
        if(optionalSedeFarmacista.isPresent()){
            SedeFarmacista sedeFarmacista = optionalSedeFarmacista.get();
            List<EstadoUsuario> estadoUsuarioList = estadoUsuarioRepository.findAll();
            model.addAttribute("farmacista", usuarioFarmacista);
            model.addAttribute("sedeFarmacista", sedeFarmacista);
            model.addAttribute("estadoUsuarioList", estadoUsuarioList);
            return "superAdmin/verificarSolicitudes";
        }else {
            return "redirect:/superadmin/solicitudes";
        }
    }

    public class EmailValidator {
        private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

        public static boolean isValidEmail(String email) {
            return email != null && Pattern.matches(EMAIL_REGEX, email);
        }
    }

    @GetMapping(value = {"/crearAdministrador"})
    public String crearAdminitrador(@ModelAttribute("administrador") Usuario administrador, Model model,
                                    HttpServletRequest request, HttpServletResponse response, Authentication authentication){
        //SESSION
        //Iniciamos la sesión
        HttpSession session = request.getSession();
        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuario);

        List<Sede> sedeDisponibleList = sedeRepository.listarSedesDisponibles();
        model.addAttribute("sedeDisponibleList", sedeDisponibleList);
        return "superAdmin/crearAdministrador";
    }

    private static final String EMAIL_REGEX = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
    private static final List<String> RESERVED_DOMAINS = Arrays.asList("example.com", "example.net", "example.org", "invalid", "localhost", "test");

    public static boolean isValidEmail(String email) {
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        Matcher matcher = pattern.matcher(email);

        if (!matcher.matches()) {
            return false;
        }

        String domain = email.substring(email.indexOf("@") + 1);
        return !RESERVED_DOMAINS.contains(domain);
    }

    public static boolean isDomainValid(String email) {
        String domain = email.substring(email.indexOf("@") + 1);

        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
            DirContext ictx = new InitialDirContext(env);

            Attributes attrs = ictx.getAttributes(domain, new String[] {"MX"});
            return attrs.get("MX") != null;
        } catch (Exception e) {
            return false;
        }
    }


    @PostMapping("/form_administrador")
    public String dniApi(@ModelAttribute("administrador") @Validated(DniApiValidationGroup.class) Usuario administrador,
                         BindingResult bindingResult,
                         Model model,
                         @RequestParam(value = "dni") String dni,
                         HttpServletRequest request, HttpServletResponse response, Authentication authentication){

        //Iniciamos la sesión
        HttpSession session = request.getSession();
        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuario);

        if (bindingResult.hasErrors()){
            System.out.println("HAY ERRORES DE VALIDACIÓN:");
            for (ObjectError error : bindingResult.getAllErrors()) {
                System.out.println("- " + error.getDefaultMessage());
            }
            List<Sede> sedeDisponibleList = sedeRepository.listarSedesDisponibles();
            model.addAttribute("sedeDisponibleList", sedeDisponibleList);

            model.addAttribute("nombres", "");
            model.addAttribute("apellidos", "");
            model.addAttribute("dni", "");

            return "superAdmin/crearAdministrador";

        } else {

            List<String> dnisUsados = usuarioRepository.listarDNIsUsados();

            System.out.println("DNI usado " + dni);
            if (dnisUsados.contains(dni)) {
                System.out.println("El DNI está en la lista.");
                bindingResult.rejectValue("dni", "error.dni", "El DNI ya se encuentra registrado.");
                model.addAttribute("nombres", "");
                model.addAttribute("apellidos", "");
                model.addAttribute("dni", "");
                return "superAdmin/crearAdministrador";
            }

            List<Sede> sedeDisponibleList = sedeRepository.listarSedesDisponibles();
            model.addAttribute("sedeDisponibleList", sedeDisponibleList);

            List<String> values = DniAPI.getDni(dni);

            if (!(values.isEmpty())){
                if(!(values.get(0).isEmpty())){
                    String apiDni = values.get(3);
                    String apiNombres = values.get(0);
                    String apiApellidos = (values.get(1) + " " + values.get(2));
                    model.addAttribute("dni", apiDni);
                    model.addAttribute("nombres", apiNombres);
                    model.addAttribute("apellidos", apiApellidos);

                    System.out.println("DNI: " + administrador.getDni());
                    System.out.println("Nombre: " + administrador.getNombres());
                    System.out.println("Apellidos: " + administrador.getApellidos());
                    System.out.println(values);
                    return "superAdmin/crearAdministrador";
                }else{
                    // Caso cuando el dni no existe
                    String dniError = "error";
                    bindingResult.rejectValue("dni", "error.dni", "El DNI no existe.");
                    return "superAdmin/crearAdministrador";
                }
            }else{
                // Caso cuando el dni no existe
                String dniError = "error";
                bindingResult.rejectValue("dni", "error.dni", "El DNI no existe.");
                return "superAdmin/crearAdministrador";
            }
        }
    }


    int sedeId;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @PostMapping("/guardarAdministrador")
    public String agregarNuevoAdministrador(@ModelAttribute("administrador") @Validated(SuperAdminValidationsGroup.class) Usuario administrador, BindingResult bindingResult,
                                            Model model,
                                            @RequestParam(value = "sedeid", required = false) String idSede,
                                            @RequestParam(value = "nombres") String name,
                                            @RequestParam(value = "apellidos") String lastname,
                                            @RequestParam(value = "dni", required = false) String dni,
                                            @RequestParam(value = "correo", required = false) String correo,
                                            HttpServletRequest request, HttpServletResponse response, Authentication authentication,
                                            RedirectAttributes attr) throws IOException, MessagingException {
        System.out.println(dni);
        System.out.println(name);
        System.out.println(lastname);

        HttpSession session = request.getSession();
        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuario);

        if(bindingResult.hasErrors()){
            System.out.println("HAY ERRORES DE VALIDACIÓN:");
            for (ObjectError error : bindingResult.getAllErrors()) {
                System.out.println("- " + error.getDefaultMessage());
            }
            List<Sede> sedeDisponibleList = sedeRepository.listarSedesDisponibles();
            model.addAttribute("sedeDisponibleList", sedeDisponibleList);

            return "superAdmin/crearAdministrador";
        }else{
            List<String> dnisUsados = usuarioRepository.listarDNIsUsados();
            List<String> correosUsados = usuarioRepository.listarCorreosUsados();
            System.out.println("DNI usado " + dni);
            if (dnisUsados.contains(dni)) {
                System.out.println("El DNI está en la lista.");
                bindingResult.rejectValue("dni", "error.dni", "El DNI ya se encuentra registrado.");
                return "superAdmin/crearAdministrador";
            }
            if (correosUsados.contains(correo)) {
                System.out.println("El correo está en la lista.");
                bindingResult.rejectValue("correo", "error.correo", "El correo ya se encuentra registrado.");
                return "superAdmin/crearAdministrador";
            }

            if (!(isValidEmail(correo) || isDomainValid(correo))) {
                System.out.println("Email is valid and domain is valid.");
                bindingResult.rejectValue("correo", "error.correo", "El correo electrónico ingresado no es válido.");
                System.out.println("HAY ERRORES DE VALIDACIÓN:");
                for (ObjectError error : bindingResult.getAllErrors()) {
                    System.out.println("- " + error.getDefaultMessage());
                }
                List<Sede> sedeDisponibleList = sedeRepository.listarSedesDisponibles();
                model.addAttribute("sedeDisponibleList", sedeDisponibleList);
                return "superAdmin/crearAdministrador";
            }

            if (!EmailValidator.isValidEmail(correo)) {
                bindingResult.rejectValue("correo", "error.correo", "Debe ingresar un correo electrónico válido.");
                System.out.println("HAY ERRORES DE VALIDACIÓN:");
                for (ObjectError error : bindingResult.getAllErrors()) {
                    System.out.println("- " + error.getDefaultMessage());
                }
                List<Sede> sedeDisponibleList = sedeRepository.listarSedesDisponibles();
                model.addAttribute("sedeDisponibleList", sedeDisponibleList);
                return "superAdmin/crearAdministrador";
            }

            // Generar contraseña temporal
            String temporaryPassword = passwordService.generateTemporaryPassword();
            System.out.println("Contraseña temporal." + temporaryPassword);
            String hashedPassword1 = SHA256.cipherPassword(temporaryPassword);
            administrador.setContrasena(hashedPassword1);
             // Indicar que el usuario debe cambiar la contraseña en el primer inicio de sesión


            if(idSede == null || idSede.isEmpty()) {
                System.out.println("ID ADMIN: " + administrador.getIdUsuario());

                administrador.setRol(2);
                administrador.setEstadoUsuario(5);

                usuarioRepository.save(administrador);
                String hashedPassword = SHA256.cipherPassword(temporaryPassword);
                usuarioRepository.actualizarContrasenaFarmacista(hashedPassword, idUsuario);
                usuarioRepository.actualizarEstadoFarmacista(idUsuario);

                attr.addFlashAttribute("msg", "Nuevo administrador creado exitosamente");
                return "redirect:/superadmin/administradoresSede";
            }else{
                if(administrador.getIdUsuario() != 0){
                    System.out.println("ID ADMIN si no es vacio: " + administrador.getIdUsuario());
                    int idsede = Integer.parseInt(idSede);

                    administrador.setRol(2);
                    administrador.setEstadoUsuario(1);

                    usuarioRepository.save(administrador);
                    String hashedPassword = SHA256.cipherPassword(temporaryPassword);
                    usuarioRepository.actualizarContrasenaFarmacista(hashedPassword, idUsuario);
                    usuarioRepository.actualizarEstadoFarmacista(idUsuario);

                    attr.addFlashAttribute("msg", "Nuevo administrador creado exitosamente");
                    return "redirect:/superadmin/administradoresSede";
                }else{
                    System.out.println("ID ADMIN si es vacio: " + administrador.getIdUsuario());

                    administrador.setRol(2);
                    administrador.setEstadoUsuario(5);

                    usuarioRepository.save(administrador);
                    String hashedPassword = SHA256.cipherPassword(temporaryPassword);
                    usuarioRepository.actualizarContrasenaFarmacista(hashedPassword, idUsuario);
                    usuarioRepository.actualizarEstadoFarmacista(idUsuario);

                    attr.addFlashAttribute("msg", "Nuevo administrador creado exitosamente");
                    return "redirect:/superadmin/administradoresSede";
                }
            }
        }
    }



    private void sendTemporaryPasswordEmail(String to, String temporaryPassword, String sedeName) throws MessagingException {
        Usuario datosAdmin = usuarioRepository.findByCorreo(to);
        String nombreAdmin = datosAdmin.getNombres();
        String apellidoAdmin = datosAdmin.getApellidos();
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom("bienestar.sanmiguel1@outlook.com");
        helper.setTo(to);
        helper.setSubject("Bienvenido a Bienestar San Miguel - Credenciales de Administrador");

        String emailContent = "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: 'Trebuchet MS', sans-serif; margin: 0; padding: 0; }" +
                ".header { background-color: #007BFF; padding: 20px; text-align: center; }" +
                ".header h1 { color: #1B4F72; margin: 0; }" +
                ".header h1 span { color: #FFFFFF; }" +
                ".content { border: 2px solid #007BFF; border-radius: 10px; padding: 20px; margin: 20px; }" +
                ".content h2 { color: #007BFF; }" +
                ".content p { margin: 10px 0; }" +
                ".credentials { background-color: #f9f9f9; padding: 10px; border-radius: 5px; }" +
                ".credentials p { margin: 5px 0; }" +
                "a { color: #007BFF; text-decoration: none; }" +
                "a:hover { text-decoration: underline; }" +
                ".center { text-align: center; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='header'>" +
                "<h1>Bienestar <span>San Miguel</span></h1>" +
                "</div>" +
                "<div class='content'>" +
                "<h3 style='font-size: 18px; color: #1B4F72'>¡Bienvenido a la familia <strong>Bienestar San Miguel</strong>!</h3>" +
                "<h4 style='font-size: 16px; color: #007BFF'>Hola, " + nombreAdmin + " " + apellidoAdmin + ": </h4>" +
                "<p style='font-size: 14px;'>Nos complace informarte que has sido asignado como administrador de la <strong>" + sedeName + "</strong>.</p>" +
                "<p style='font-size: 14px;'>Por favor, utiliza las siguientes credenciales para iniciar sesión:</p>" +
                "<div class='credentials'>" +
                "<p style='font-size: 16px;color: #1B4F72'><strong>Usuario:</strong><strong style='font-size: 16px;color: #007BFF'> " + to + "</strong></p>" +
                "<p style='font-size: 16px;color: #1B4F72'><strong>Contraseña:</strong><strong style='font-size: 16px;color: #007BFF'> " + temporaryPassword + "</strong></p>" +
                "</div>" +
                "<p style='font-size: 14px;'>Debes cambiar esta contraseña inmediatamente después de tu primer inicio de sesión. La contraseña temporal es válida por 24 horas. Si no cambias tu contraseña en este tiempo, tendrás que solicitar una nueva.</p>" +
                "<p style='font-size: 14px;'>Puedes acceder a la plataforma a través del siguiente enlace:</p>" +
                "<p class='center' style='font-size: 16px'><a href='https://bienestarsanmiguel.xyz'>https://bienestarsanmiguel.xyz</a></p>" +
                "<p style='font-size: 14px;'>Gracias.</p>" +
                "</div>" +
                "</body>" +
                "</html>";

        helper.setText(emailContent, true);
        mailSender.send(message);
    }


    private void sendTemporaryPasswordEmailFarmacista(String to, String temporaryPassword) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom("bienestar.sanmiguel1@outlook.com");
        helper.setTo(to);
        helper.setSubject("Contraseña Temporal para Nuevo Farmacista");


        String emailContent = "<html>" +
                "<body style='font-family: Arial, sans-serif; margin: 0; padding: 0;'>" +
                "<div style='background-color: #007BFF; padding: 20px; text-align: center;'>" +
                "<h1 style='color: #1B4F72; margin: 0;'>Bienestar <span style='color: #FFFFFF;'>San Miguel</span></h1>" +
                "</div>" +
                "<div style='border: 2px solid #007BFF; border-radius: 10px; padding: 20px; margin: 20px;'>" +
                "<h2 style='color: #007BFF;'>Hola,</h2>" +
                "<p>Tu cuenta de farmacista ha sido creada. Por favor, usa la siguiente contraseña temporal para iniciar sesión:</p>" +
                "<p style='font-size: 20px; color: #007BFF; font-weight: bold; text-align: center;'>" + temporaryPassword + "</p>" +
                "<p>Debes cambiar esta contraseña inmediatamente después de tu primer inicio de sesión. La contraseña temporal es válida por 5 minutos. Si no cambias tu contraseña en este tiempo, tendrás que solicitar una nueva.</p>" +
                "<p>Gracias.</p>" +
                "</div>" +
                "</body>" +
                "</html>";

        helper.setText(emailContent, true);
        mailSender.send(message);
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

    private String hashPasswordSHA256(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/asignarSedeAdministradorSede")
    public String asignarSedeAdministrador(@ModelAttribute("administrador") @Valid Usuario administrador, BindingResult bindingResult, @RequestParam("datosAdministrador") Integer datosAdministrador, @RequestParam("sedeid") int sedeId, Model model, RedirectAttributes attr,
                                           HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        //SESSION
        //Iniciamos la sesión
        HttpSession session = request.getSession();
        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuario);

        System.out.println("Valor Admin del administrador: " + administrador.getIdUsuario());
        System.out.println("Valor Admin final: " + datosAdministrador);
        System.out.println("Valor Sede final: " + sedeId);
        sedeRepository.asignarAdministradorSede(datosAdministrador, sedeId);
        return "redirect:/superadmin/administradoresSede";
    }



    @GetMapping(value = {"/editarAdministrador"})
    public String editarAdministrador(Usuario administrador,
                                      Model model, @RequestParam("id") String id,
                                      HttpServletRequest request, HttpServletResponse response, Authentication authentication){
        //SESSION
        //Iniciamos la sesión
        HttpSession session = request.getSession();
        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuario);

        int idAdmin = Integer.parseInt(id);
        Optional<Usuario> optionalUsuario = usuarioRepository.findById(idAdmin);
        Optional<Sede> optionalAdministradorSede = sedeRepository.buscarAdminID(idAdmin);
        System.out.println("Valor Dentro de editar de optional usuario: " + optionalUsuario.get().getIdUsuario());
        if(optionalUsuario.isPresent()){
            administrador = optionalUsuario.get();
            Sede sedeAdministrador = optionalAdministradorSede.get();
            List<Sede> sedeList = sedeRepository.findAll();

            String passwordHash = administrador.getContrasena(); // Obtener el hash de la contraseña desde la base de datos
            String passwordDots = "*************";

            List<EstadoUsuario> estadoUsuarioList = estadoUsuarioRepository.listarEstadosUsuarios();
            model.addAttribute("sedeAdministrador", sedeAdministrador);
            model.addAttribute("sedeList", sedeList);
            model.addAttribute("administrador", administrador);
            model.addAttribute("estadoUsuarioList", estadoUsuarioList);
            return "superAdmin/editarAdministrador";
        }else {
            return "redirect:/superadmin/administradoresSede";
        }
    }

    @PostMapping("/actualizarAdministrador")
    public String actualizarAdministrador(@ModelAttribute("administrador") @Valid Usuario administrador, BindingResult bindingResult, @RequestParam("sedeid") Integer idSede,
                                          @RequestParam(value = "contrasena", required = false) String contrasenia,
                                          @RequestParam(value = "dni", required = false) String dni,
                                          @RequestParam(value = "correo", required = false) String correo,
                                          RedirectAttributes attr, Model model){
        if(bindingResult.hasErrors()){
            Sede sedeListAdminID = sedeRepository.sedeAdminID(administrador.getIdUsuario());
            List<Sede> sedeList = sedeRepository.findAll();
            String passwordHash = administrador.getContrasena(); // Obtener el hash de la contraseña desde la base de datos
            String passwordDots = "*************";

            List<EstadoUsuario> estadoUsuarioList = estadoUsuarioRepository.listarEstadosUsuarios();
            model.addAttribute("sedeAdministrador", sedeListAdminID);
            model.addAttribute("sedeList", sedeList);
            model.addAttribute("estadoUsuarioList", estadoUsuarioList);

            return "superAdmin/editarAdministrador";
        }else{

            System.out.println("Contra antigua: " + administrador.getContrasena());
            System.out.println("Contra nueva: " + contrasenia);

            System.out.println("Imprimir Sede ID: " + idSede);
            int idAdministradorNuevo = administrador.getIdUsuario();

            List<String> correosUsados = usuarioRepository.listarCorreosUsadosMenosUserID(administrador.getIdUsuario());
            if (correosUsados.contains(correo)) {
                System.out.println("El correo está en la lista.");
                Sede sedeListAdminID = sedeRepository.sedeAdminID(administrador.getIdUsuario());
                List<Sede> sedeList = sedeRepository.findAll();

                String passwordHash = administrador.getContrasena(); // Obtener el hash de la contraseña desde la base de datos
                String passwordDots = "*************";

                List<EstadoUsuario> estadoUsuarioList = estadoUsuarioRepository.listarEstadosUsuarios();
                model.addAttribute("sedeAdministrador", sedeListAdminID);
                model.addAttribute("sedeList", sedeList);
                model.addAttribute("estadoUsuarioList", estadoUsuarioList);
                bindingResult.rejectValue("correo", "error.correo", "El correo ya se encuentra registrado.");
                return "superAdmin/editarAdministrador";
            }

            List<String> dnisUsados = usuarioRepository.listarDNIsUsadosMenosUserID(administrador.getIdUsuario());
            if (dnisUsados.contains(dni)) {
                System.out.println("El DNi está en la lista.");
                Sede sedeListAdminID = sedeRepository.sedeAdminID(administrador.getIdUsuario());
                List<Sede> sedeList = sedeRepository.findAll();

                String passwordHash = administrador.getContrasena(); // Obtener el hash de la contraseña desde la base de datos
                String passwordDots = "*************";

                List<EstadoUsuario> estadoUsuarioList = estadoUsuarioRepository.listarEstadosUsuarios();
                model.addAttribute("sedeAdministrador", sedeListAdminID);
                model.addAttribute("sedeList", sedeList);
                model.addAttribute("estadoUsuarioList", estadoUsuarioList);
                bindingResult.rejectValue("dni", "error.dni", "El DNI ya se encuentra registrado.");
                return "superAdmin/editarAdministrador";
            }

            if (!EmailValidator.isValidEmail(correo)) {
                bindingResult.rejectValue("correo", "error.correo", "Debe ingresar un correo electrónico válido.");
                System.out.println("HAY ERRORES DE VALIDACIÓN:");
                Sede sedeListAdminID = sedeRepository.sedeAdminID(administrador.getIdUsuario());
                List<Sede> sedeList = sedeRepository.findAll();
                String passwordHash = administrador.getContrasena(); // Obtener el hash de la contraseña desde la base de datos
                String passwordDots = "*************";

                List<EstadoUsuario> estadoUsuarioList = estadoUsuarioRepository.listarEstadosUsuarios();
                model.addAttribute("sedeAdministrador", sedeListAdminID);
                model.addAttribute("sedeList", sedeList);
                model.addAttribute("estadoUsuarioList", estadoUsuarioList);

                return "superAdmin/editarAdministrador";
            }

            Usuario adminDatos = usuarioRepository.administradorSede(idAdministradorNuevo);
            String passwordOld = adminDatos.getContrasena();

            String passwordNew = SHA256.cipherPassword(contrasenia);

            System.out.println("Contra antigua: " + passwordOld);
            System.out.println("Contra nueva: " + passwordNew);

            if(contrasenia.isEmpty()){
                System.out.println("Contra vacia: " + administrador.getContrasena());
                String contradeAdmin = usuarioRepository.contraAdmin(administrador.getIdUsuario());
                administrador.setContrasena(contradeAdmin);
            } else if (!isValidPassword(contrasenia)) {
                System.out.println("O AQUII:");
                String errorMsg = "Debe escribir una contraseña. Esta debe tener al menos 8 caracteres, una mayúscula, un número y un carácter especial.";
                bindingResult.rejectValue("contrasena", "error.contrasena", errorMsg);
                Sede sedeListAdminID = sedeRepository.sedeAdminID(administrador.getIdUsuario());
                List<Sede> sedeList = sedeRepository.findAll();

                String passwordHash = administrador.getContrasena(); // Obtener el hash de la contraseña desde la base de datos
                String passwordDots = "*************";

                List<EstadoUsuario> estadoUsuarioList = estadoUsuarioRepository.listarEstadosUsuarios();
                model.addAttribute("sedeAdministrador", sedeListAdminID);
                model.addAttribute("sedeList", sedeList);
                model.addAttribute("estadoUsuarioList", estadoUsuarioList);
                model.addAttribute("error", errorMsg); // Añade el error al modelo
                return "superAdmin/editarAdministrador";
            }

            Sede AdministradorTieneSede = sedeRepository.buscarSedeTieneAdministrador(idAdministradorNuevo);

            /*Se ubica el administrador de la sede nueva elegida*/
            Sede sedeTieneAdministrador = sedeRepository.getSedeByIdSede(idSede);
            /*Se obtiene el id del administrador de la sede nueva elegida*/
            System.out.println("Contra vacia: " + administrador.getContrasena());
            if(idSede == AdministradorTieneSede.getIdSede()){
                if(contrasenia.isEmpty()) {
                    System.out.println("Contra vacia: " + administrador.getContrasena());
                    String contradeAdmin = usuarioRepository.contraAdmin(administrador.getIdUsuario());
                    administrador.setContrasena(contradeAdmin);
                }
                else{
                    String hashedPassword = SHA256.cipherPassword(contrasenia);
                    administrador.setContrasena(hashedPassword);
                }

                System.out.println("Sedes iguales: " );
                usuarioRepository.save(administrador);
                attr.addFlashAttribute("msg", "Datos del administrador actualizados exitosamente");
                return "redirect:/superadmin/administradoresSede";
            }
            else{
                /*Se obtiene el id de la sede actual del admin*/
                int idSedeAntigua = AdministradorTieneSede.getIdSede();

                if(contrasenia.isEmpty()) {
                    System.out.println("Contra vacia: " + administrador.getContrasena());
                    String contradeAdmin = usuarioRepository.contraAdmin(administrador.getIdUsuario());
                    administrador.setContrasena(contradeAdmin);
                }
                else{
                    String hashedPassword = SHA256.cipherPassword(contrasenia);
                    administrador.setContrasena(hashedPassword);
                }

                if (sedeTieneAdministrador.getAdmin()==null){
                    sedeRepository.eliminarAdminAntiguo(idSedeAntigua);
                    sedeRepository.asignarAdministradorSede(idAdministradorNuevo, idSede);
                    usuarioRepository.save(administrador);
                }else{
                    int idAdministradorAntiguo = sedeTieneAdministrador.getAdmin().getIdUsuario();
                    usuarioRepository.administradorSinSede(idAdministradorAntiguo);
                    sedeRepository.eliminarAdminAntiguo(idSedeAntigua);
                    sedeRepository.asignarAdministradorSede(idAdministradorNuevo, idSede);
                    usuarioRepository.save(administrador);
                }
                attr.addFlashAttribute("msg", "Datos del administrador actualizados exitosamente");
                return "redirect:/superadmin/administradoresSede";
            }
        }
    }

    @PostMapping("/eliminarAdministrador")
    public String eliminarAdministrador(@RequestParam(value = "idAdministrador") String idAdministrador, @RequestParam(value = "idSede") int idSede, RedirectAttributes attr){
        int idAdministradorID = Integer.parseInt(idAdministrador);
        sedeRepository.eliminarAdminAntiguo(idSede);
        usuarioRepository.eliminarFarmacista(idAdministradorID);
        attr.addFlashAttribute("msg", "Administrador eliminado exitosamente");
        return "redirect:/superadmin/administradoresSede";

    }

    @PostMapping("/banearAdministrador")
    public String banearAdministrador(@RequestParam(value = "idAdministrador") String idAdministrador, @RequestParam(value = "idSede") int idSede, RedirectAttributes attr){
        int idAdministradorID = Integer.parseInt(idAdministrador);
        sedeRepository.eliminarAdminAntiguo(idSede);
        usuarioRepository.banearAdministrador(idAdministradorID);
        attr.addFlashAttribute("msg", "Administrador baneado exitosamente");
        return "redirect:/superadmin/administradoresSede";

    }

    @PostMapping("/eliminarAdministradorSinSede")
    public String eliminarAdminSinSede(@RequestParam(value = "idAdministrador") String idAdministrador, RedirectAttributes attr){
        int idAdministradorID = Integer.parseInt(idAdministrador);
        usuarioRepository.eliminarFarmacista(idAdministradorID);
        attr.addFlashAttribute("msg", "Administrador eliminado exitosamente");
        return "redirect:/superadmin/administradoresSede";

    }

    @PostMapping("/desbanearAdministrador")
    public String desbanearAdministrador(@RequestParam(value = "idAdministrador") String idAdministrador, RedirectAttributes attr){
        int idAdministradorID = Integer.parseInt(idAdministrador);
        usuarioRepository.administradorSinSede(idAdministradorID);
        attr.addFlashAttribute("msg", "Administrador desbaneado exitosamente");
        return "redirect:/superadmin/administradoresSede";

    }

    @GetMapping(value = {"/editarAsignarAdministrador"})
    public String asignarNuevaSedeAdministrador(Model model, @RequestParam("id") String id, RedirectAttributes attr,
                                                HttpServletRequest request, HttpServletResponse response, Authentication authentication){
        //SESSION
        //Iniciamos la sesión
        HttpSession session = request.getSession();
        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuario);

        int idAdmin = Integer.parseInt(id);
        Optional<Usuario> optionalUsuario = usuarioRepository.findById(idAdmin);
        System.out.println("Valor Dentro de editar de optional usuario: " + optionalUsuario.get().getIdUsuario());
        if(optionalUsuario.isPresent()){
            Usuario administrador = optionalUsuario.get();
            List<Sede> sedeList = sedeRepository.listarSedesDisponibles();

            String passwordHash = administrador.getContrasena(); // Obtener el hash de la contraseña desde la base de datos
            String passwordDots = hashToDots(passwordHash);

            model.addAttribute("sedeList", sedeList);
            model.addAttribute("contrasenia", passwordDots);
            model.addAttribute("administrador", administrador);
            return "superAdmin/asignarAdministrador";
        }else {
            return "redirect:/superadmin/administradoresSede";
        }
    }

    private String hashToDots(String passwordHash) {
        return "*".repeat(passwordHash.length()); // Repite el carácter '*' según la longitud del hash
    }


    @PostMapping("/asignandoAdministrador")
    public String asignaraNuevoAdministrador(@RequestParam(value = "idUsuario", required = false) String idUsuario,
                                             @RequestParam(value = "correo", required = false) String correo,
                                             @RequestParam(value = "sedeid", required = false) String idSede, RedirectAttributes attr, Model model) throws IOException {


        int idAdmin = Integer.parseInt(idUsuario);
        Usuario administrador = usuarioRepository.administradorSede(idAdmin);
        if(idSede == null || idSede.isEmpty()){
            System.out.println("ID ADMIN: " + administrador.getIdUsuario());
            usuarioRepository.administradorSinSede(administrador.getIdUsuario());
            attr.addFlashAttribute("msg", "El administrador no fue asignado a ninguna sede");
            return "redirect:/superadmin/administradoresSede";
        }else{
            if(administrador.getIdUsuario() != 0){
                System.out.println("ID ADMIN si no es vacio: " + administrador.getIdUsuario());
                int idsede = Integer.parseInt(idSede);
                usuarioRepository.activarAdministrador(administrador.getIdUsuario());

                // Generar contraseña temporal
                String temporaryPassword = passwordService.generateTemporaryPassword();
                System.out.println("Contraseña temporal." + temporaryPassword);
                String hashedPassword1 = SHA256.cipherPassword(temporaryPassword);
                administrador.setContrasena(hashedPassword1);
                // Indicar que el usuario debe cambiar la contraseña en el primer inicio de sesión

                List<String> correosUsados = usuarioRepository.listarCorreosUsadosMenosUserID(administrador.getIdUsuario());
                if (correosUsados.contains(correo)) {
                    System.out.println("El correo está en la lista.");
                    usuarioRepository.administradorSinSede(idAdmin);
                    attr.addFlashAttribute("msg1", "Correo ya registrado. Ingrese uno nuevo.");
                    return "redirect:/superadmin/administradoresSede";
                }

                String correoUser = usuarioRepository.encontrarCorreoAdministrador(administrador.getIdUsuario());

                if(!(correo.equals(correoUser))){
                    administrador.setCorreo(correo);
                    usuarioRepository.save(administrador);
                    correoUser = administrador.getCorreo();
                    System.out.println("Correo modificado: " + correoUser);
                }

                System.out.println("Correo: " + usuarioRepository.encontrarCorreoAdministrador(administrador.getIdUsuario()));
                int sedeID = Integer.parseInt(idSede);
                String nameSede = sedeRepository.nombreSede(sedeID);
                try {

                    sendTemporaryPasswordEmail(correoUser, temporaryPassword, nameSede);
                } catch (MailSendException e) {
                    usuarioRepository.administradorSinSede(idAdmin);
                    attr.addFlashAttribute("msg1", "Correo no enviado, verifique la validez del correo. El Administrador no fue asignado a ninguna sede");
                    return "redirect:/superadmin/administradoresSede";
                } catch (MessagingException e) {
                    System.err.println("Error al enviar o recibir correo: " + e.getMessage());
                    e.printStackTrace();
                }

                sedeRepository.asignarAdministradorSede(administrador.getIdUsuario(), idsede);
                usuarioRepository.save(administrador);
                String hashedPassword = SHA256.cipherPassword(temporaryPassword);
                usuarioRepository.actualizarContrasenaFarmacista(hashedPassword, idAdmin);
                usuarioRepository.actualizarEstadoFarmacista(idAdmin);

                //Se le envía el correo con la contraseña, luego de eso necesita de un timer de 5 minutos para cambiarla
                long tiempoEnvio = System.currentTimeMillis(); //tiempo donde se envía

                //Por temas de presentación lo pongo en 3 minuto nomás

                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Usuario usuario = usuarioRepository.encontrarAdministradorId(administrador.getIdUsuario());
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - tiempoEnvio >= 1 * 60 * 1000 && usuario.getEstadoContra() == 0) {
                            // Si han pasado 3 minutos y la contraseña no ha sido cambiada
                            String expiredPassword = "noRegistrado";
                            String passwordHashNoRegistrado = SHA256.cipherPassword(expiredPassword);
                            usuarioRepository.actualizarContrasenaFarmacista(passwordHashNoRegistrado, idAdmin);
                            System.out.println("La contraseña ha expirado para " + usuario.getCorreo());
                        }
                    }
                }, 1 * 60 * 1000); //Se ejecuta esta funcion luego de 1 minuto


                attr.addFlashAttribute("msg", "El administrador fue asignado a una sede correctamente");
                return "redirect:/superadmin/administradoresSede";
            }else{
                System.out.println("ID ADMIN si es vacio: " + administrador.getIdUsuario());
                attr.addFlashAttribute("msg", "El administrador no fue asignado a ninguna sede");
                return "redirect:/superadmin/administradoresSede";
            }
        }

    }


    @GetMapping(value = {"/crearDoctor"})
    public String crearDoctor(@ModelAttribute("doctor") Doctor doctor, RedirectAttributes attr, Model model,
                              HttpServletRequest request, HttpServletResponse response, Authentication authentication){
        //SESSION
        //Iniciamos la sesión
        HttpSession session = request.getSession();
        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuario);

        List<Sede> sedeDisponibleList = sedeRepository.findAll();
        model.addAttribute("sedeDisponibleList", sedeDisponibleList);
        return "superAdmin/crearDoctor";
    }

    @PostMapping("/form_doctor")
    public String dniApiDoctor(@ModelAttribute("doctor") @Validated(DniApiValidationGroup.class) Doctor doctor,
                         BindingResult bindingResult,
                         Model model,
                         @RequestParam(value = "dni") String dni,
                         HttpServletRequest request, HttpServletResponse response, Authentication authentication){

        //Iniciamos la sesión
        HttpSession session = request.getSession();
        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuario);


        if (bindingResult.hasErrors()){
            System.out.println("HAY ERRORES DE VALIDACIÓN:");
            for (ObjectError error : bindingResult.getAllErrors()) {
                System.out.println("- " + error.getDefaultMessage());
            }
            List<Sede> sedeDisponibleList = sedeRepository.findAll();
            model.addAttribute("sedeDisponibleList", sedeDisponibleList);

            model.addAttribute("nombres", "");
            model.addAttribute("apellidos", "");
            model.addAttribute("dni", "");

            return "superAdmin/crearDoctor";

        } else {

            List<String> dnisUsados = usuarioRepository.listarDNIsUsados();

            if (dnisUsados.contains(dni)) {
                System.out.println("El DNI está en la lista.");
                List<Sede> sedeDisponibleList = sedeRepository.findAll();
                model.addAttribute("sedeDisponibleList", sedeDisponibleList);
                bindingResult.rejectValue("dni", "error.dni", "El DNI ya se encuentra registrado.");
                return "superAdmin/crearDoctor";
            }

            List<Sede> sedeDisponibleList = sedeRepository.findAll();
            model.addAttribute("sedeDisponibleList", sedeDisponibleList);

            List<String> values = DniAPI.getDni(dni);
            if (!(values.isEmpty())){
                if(!(values.get(0).isEmpty())) {
                    String apiDni = values.get(3);
                    String apiNombres = values.get(0);
                    String apiApellidos = (values.get(1) + " " + values.get(2));

                    model.addAttribute("dni", apiDni);
                    model.addAttribute("nombres", apiNombres);
                    model.addAttribute("apellidos", apiApellidos);

                    System.out.println(values);
                    return "superAdmin/crearDoctor";
                }else{
                    String dniError = "error";
                    bindingResult.rejectValue("dni", "error.dni", "El DNI no existe.");
                    return "superAdmin/crearDoctor";
                }
            }else{
                String dniError = "error";
                bindingResult.rejectValue("dni", "error.dni", "El DNI no existe.");
                return "superAdmin/crearDoctor";
            }
        }
    }

    @PostMapping("/guardarDoctor")
    public String crearNuevoDoctor(@ModelAttribute("doctor") @Valid Doctor doctor, BindingResult bindingResul,
                                   @RequestParam(value = "dni", required = false) String dni,
                                   @RequestParam(value = "correo", required = false) String correo,
                                   RedirectAttributes attr, Model model) {
        if(bindingResul.hasErrors()){
            List<Sede> sedeDisponibleList = sedeRepository.findAll();
            model.addAttribute("sedeDisponibleList", sedeDisponibleList);
            return "superAdmin/crearDoctor";
        }else{

            List<String> dnisUsados = usuarioRepository.listarDNIsUsados();
            List<String> correosUsados = usuarioRepository.listarCorreosUsados();

            if (dnisUsados.contains(dni)) {
                System.out.println("El DNI está en la lista.");
                List<Sede> sedeDisponibleList = sedeRepository.findAll();
                model.addAttribute("sedeDisponibleList", sedeDisponibleList);
                bindingResul.rejectValue("dni", "error.dni", "El DNI ya se encuentra registrado.");
                return "superAdmin/crearDoctor";
            }
            if (correosUsados.contains(correo)) {
                System.out.println("El correo está en la lista.");
                List<Sede> sedeDisponibleList = sedeRepository.findAll();
                model.addAttribute("sedeDisponibleList", sedeDisponibleList);
                bindingResul.rejectValue("correo", "error.correo", "El correo ya se encuentra registrado.");
                return "superAdmin/crearDoctor";
            }

            if (!EmailValidator.isValidEmail(correo)) {
                bindingResul.rejectValue("correo", "error.correo", "Debe ingresar un correo electrónico válido.");
                System.out.println("HAY ERRORES DE VALIDACIÓN:");
                List<Sede> sedeDisponibleList = sedeRepository.findAll();
                model.addAttribute("sedeDisponibleList", sedeDisponibleList);
                return "superAdmin/crearDoctor";
            }

            doctorRepository.save(doctor);
            attr.addFlashAttribute("msg", "Nuevo doctor creado exitosamente");
            return "redirect:/superadmin/doctores";
        }
    }

    @GetMapping(value = {"/editarDoctor"})
    public String editarDoctor(@ModelAttribute("doctor") Doctor doctor, Model model, @RequestParam("id") int id,
                               HttpServletRequest request, HttpServletResponse response, Authentication authentication){
        //SESSION
        //Iniciamos la sesión
        HttpSession session = request.getSession();
        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuario);

        Optional<Doctor> optionalDoctor = doctorRepository.findById(id);

        if (optionalDoctor.isPresent()) {
            doctor = optionalDoctor.get();
            List<Sede> sedeDisponibleList = sedeRepository.findAll();
            List<SedeDoctor> sedeDoctorList = sedeDoctorRepository.listarSedesDondeEstaDoctor(id);
            List<Integer> idsSedes = sedeDoctorRepository.listarSedesPorIdDoctor(id);
            model.addAttribute("idsSede", idsSedes);
            model.addAttribute("sedeDisponibleList", sedeDisponibleList);
            model.addAttribute("doctor", doctor);
            model.addAttribute("doctoresVisiblesSede", sedeDoctorList);
            return "superAdmin/editarDoctor";
        } else {
            return "redirect:/superadmin/doctores";
        }
    }


    @PostMapping("/actualizarDoctor")
    public String actualizarDoctor(@ModelAttribute("doctor") @Valid Doctor doctor, BindingResult bindingResult,
                                   @RequestParam(value = "sedeid", required = false) List<Integer> idSedesSeleccionadas, RedirectAttributes attr,
                                   @RequestParam(value = "dni", required = false) String dni,
                                   @RequestParam(value = "correo", required = false) String correo,Model model){
        if(bindingResult.hasErrors()){
            List<Sede> sedeDisponibleList = sedeRepository.findAll();
            List<SedeDoctor> sedeDoctorList = sedeDoctorRepository.listarSedesDondeEstaDoctor(doctor.getIdDoctor());
            List<Integer> idsSedes = sedeDoctorRepository.listarSedesPorIdDoctor(doctor.getIdDoctor());
            model.addAttribute("idsSede", idsSedes);
            model.addAttribute("sedeDisponibleList", sedeDisponibleList);
            model.addAttribute("doctoresVisiblesSede", sedeDoctorList);
            return "superAdmin/editarDoctor";
        }else{

            List<String> correosUsados = doctorRepository.listarCorreosUsadosMenosUserID(doctor.getIdDoctor());
            List<String> dnisUsados = doctorRepository.listarDNIsUsadosMenosUserID(doctor.getIdDoctor());

            if (correosUsados.contains(correo)) {
                System.out.println("El correo está en la lista.");
                List<Sede> sedeDisponibleList = sedeRepository.findAll();
                List<SedeDoctor> sedeDoctorList = sedeDoctorRepository.listarSedesDondeEstaDoctor(doctor.getIdDoctor());
                List<Integer> idsSedes = sedeDoctorRepository.listarSedesPorIdDoctor(doctor.getIdDoctor());
                model.addAttribute("idsSede", idsSedes);
                model.addAttribute("sedeDisponibleList", sedeDisponibleList);
                model.addAttribute("doctor", doctor);
                model.addAttribute("doctoresVisiblesSede", sedeDoctorList);
                bindingResult.rejectValue("correo", "error.correo", "El correo ya se encuentra registrado.");
                return "superAdmin/editarDoctor";
            }

            if (dnisUsados.contains(dni)) {
                System.out.println("El DNi está en la lista.");

                List<Sede> sedeDisponibleList = sedeRepository.findAll();
                List<SedeDoctor> sedeDoctorList = sedeDoctorRepository.listarSedesDondeEstaDoctor(doctor.getIdDoctor());
                List<Integer> idsSedes = sedeDoctorRepository.listarSedesPorIdDoctor(doctor.getIdDoctor());
                model.addAttribute("idsSede", idsSedes);
                model.addAttribute("sedeDisponibleList", sedeDisponibleList);
                model.addAttribute("doctor", doctor);
                model.addAttribute("doctoresVisiblesSede", sedeDoctorList);
                bindingResult.rejectValue("dni", "error.dni", "El DNI ya se encuentra registrado.");
                return "superAdmin/editarDoctor";
            }

            if (!EmailValidator.isValidEmail(correo)) {
                bindingResult.rejectValue("correo", "error.correo", "Debe ingresar un correo electrónico válido.");
                System.out.println("HAY ERRORES DE VALIDACIÓN:");
                List<Sede> sedeDisponibleList = sedeRepository.findAll();
                List<SedeDoctor> sedeDoctorList = sedeDoctorRepository.listarSedesDondeEstaDoctor(doctor.getIdDoctor());
                List<Integer> idsSedes = sedeDoctorRepository.listarSedesPorIdDoctor(doctor.getIdDoctor());
                model.addAttribute("idsSede", idsSedes);
                model.addAttribute("sedeDisponibleList", sedeDisponibleList);
                model.addAttribute("doctoresVisiblesSede", sedeDoctorList);
                return "superAdmin/editarDoctor";
            }

            if (idSedesSeleccionadas == null || idSedesSeleccionadas.isEmpty()) {
                int idDoctor = doctor.getIdDoctor();
                System.out.println("Estoy en si el parámetros es NULL");
                sedeDoctorRepository.borrarSedesAnterioresDoctor(idDoctor);
                doctorRepository.save(doctor);
                attr.addFlashAttribute("msg", "Datos del doctor actualizados exitosamente");
                return "redirect:/superadmin/doctores";
            } else {
                System.out.println("Estoy en si NO el parámetros es NULL");
                int idDoctor = doctor.getIdDoctor();
                List<Integer> sedeDoctorList = sedeDoctorRepository.listarDoctoresEnSedePorId(idDoctor);
                for (Integer idSede: sedeDoctorList){
                    System.out.println("Valor de idSede: " + idSede);
                }
                if(sedeDoctorList.isEmpty()){
                    for (Integer idSede : idSedesSeleccionadas) {
                        sedeDoctorRepository.asignarDoctorPorSede(idDoctor,idSede);
                    }
                    doctorRepository.save(doctor);
                    attr.addFlashAttribute("msg", "Datos del doctor actualizados exitosamente");
                    return "redirect:/superadmin/doctores";
                }else{
                    sedeDoctorRepository.borrarSedesAnterioresDoctor(idDoctor);
                    for (Integer idSede : idSedesSeleccionadas) {
                        sedeDoctorRepository.asignarDoctorPorSede(idDoctor,idSede);
                    }
                    doctorRepository.save(doctor);
                    attr.addFlashAttribute("msg", "Datos del doctor actualizados exitosamente");
                    return "redirect:/superadmin/doctores";
                }
            }
        }
    }

    @PostMapping("/eliminarDoctor")
    public String eliminarDoctor(@RequestParam(value = "idDoctor") String idDoctor, RedirectAttributes attr){
        int idDoctorID = Integer.parseInt(idDoctor);
        sedeDoctorRepository.borrarSedesAnterioresDoctor(idDoctorID);
        doctorRepository.eliminarDoctor(idDoctorID);
        attr.addFlashAttribute("msg", "Doctor eliminado exitosamente");
        return "redirect:/superadmin/doctores";
    }

    @GetMapping(value = {"/editarFarmacista"})
    public String editarFarmacista(@ModelAttribute("farmacista") @Valid Usuario farmacista, BindingResult bindingResult, Model model, @RequestParam("id") int id,
                                   HttpServletRequest request, HttpServletResponse response, Authentication authentication){
        //SESSION
        //Iniciamos la sesión
        HttpSession session = request.getSession();
        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuario);

        Optional<Usuario> optionalFarmacista = usuarioRepository.findById(id);

        if (optionalFarmacista.isPresent()) {
            farmacista = optionalFarmacista.get();
            SedeFarmacista datosFarmacistaSede = sedeFarmacistaRepository.buscarFarmacistaSede(id);
            List<EstadoUsuario> estadoUsuarioList = estadoUsuarioRepository.listarEstadosUsuarios();

            String passwordHash = farmacista.getContrasena(); // Obtener el hash de la contraseña desde la base de datos
            String passwordDots = hashToDots(passwordHash);

            model.addAttribute("farmacista", farmacista);
            model.addAttribute("datosFarmacistaSede", datosFarmacistaSede);
            model.addAttribute("estadoUsuarioList", estadoUsuarioList);
            return "superAdmin/editarFarmacista";
        } else {
            return "redirect:/superadmin/farmacistas";
        }
    }

    @PostMapping("/actualizarDatosFarmacista")
    public String actualizarDatosFarmacista(@ModelAttribute("farmacista") @Valid Usuario farmacista, BindingResult bindingResult,
                                            @RequestParam(value = "contrasena", required = false) String contrasenia,
                                            @RequestParam(value = "correo", required = false) String correo,
                                            RedirectAttributes attr, Model model){

        if(bindingResult.hasErrors()){
            System.out.println("Hay error: " );
            System.out.println("HAY ERRORES DE VALIDACIÓN:");
            for (ObjectError error : bindingResult.getAllErrors()) {
                System.out.println("- " + error.getDefaultMessage());
            }
            SedeFarmacista datosFarmacistaSede = sedeFarmacistaRepository.buscarFarmacistaSede(farmacista.getIdUsuario());
            List<EstadoUsuario> estadoUsuarioList = estadoUsuarioRepository.listarEstadosUsuarios();
            String passwordHash = farmacista.getContrasena(); // Obtener el hash de la contraseña desde la base de datos
            String passwordDots = hashToDots(passwordHash);
            model.addAttribute("farmacista", farmacista);
            model.addAttribute("datosFarmacistaSede", datosFarmacistaSede);
            model.addAttribute("estadoUsuarioList", estadoUsuarioList);
            return "superAdmin/editarFarmacista";
        }else{

            List<String> correosUsados = usuarioRepository.listarCorreosUsadosMenosUserID(farmacista.getIdUsuario());
            if (correosUsados.contains(correo)) {
                SedeFarmacista datosFarmacistaSede = sedeFarmacistaRepository.buscarFarmacistaSede(farmacista.getIdUsuario());
                List<EstadoUsuario> estadoUsuarioList = estadoUsuarioRepository.listarEstadosUsuarios();
                String passwordHash = farmacista.getContrasena(); // Obtener el hash de la contraseña desde la base de datos
                String passwordDots = hashToDots(passwordHash);
                model.addAttribute("farmacista", farmacista);
                model.addAttribute("datosFarmacistaSede", datosFarmacistaSede);
                model.addAttribute("estadoUsuarioList", estadoUsuarioList);
                bindingResult.rejectValue("correo", "error.correo", "El correo ya se encuentra registrado.");
                return "superAdmin/editarFarmacista";
            }

            if (!EmailValidator.isValidEmail(correo)) {
                bindingResult.rejectValue("correo", "error.correo", "Debe ingresar un correo electrónico válido.");
                System.out.println("HAY ERRORES DE VALIDACIÓN:");
                for (ObjectError error : bindingResult.getAllErrors()) {
                    System.out.println("- " + error.getDefaultMessage());
                }
                SedeFarmacista datosFarmacistaSede = sedeFarmacistaRepository.buscarFarmacistaSede(farmacista.getIdUsuario());
                List<EstadoUsuario> estadoUsuarioList = estadoUsuarioRepository.listarEstadosUsuarios();
                String passwordHash = farmacista.getContrasena(); // Obtener el hash de la contraseña desde la base de datos
                String passwordDots = hashToDots(passwordHash);
                model.addAttribute("farmacista", farmacista);
                model.addAttribute("datosFarmacistaSede", datosFarmacistaSede);
                model.addAttribute("estadoUsuarioList", estadoUsuarioList);
                return "superAdmin/editarFarmacista";
            }

            int idFarmacistaNuevo = farmacista.getIdUsuario();
            Usuario adminDatos = usuarioRepository.farmacista(idFarmacistaNuevo);
            String passwordOld = adminDatos.getContrasena();

            if(contrasenia.isEmpty()){
                System.out.println("Contra vacia: " + farmacista.getContrasena());
                String contradeAdmin = usuarioRepository.contraAdmin(farmacista.getIdUsuario());
                farmacista.setContrasena(contradeAdmin);
            } else if (!isValidPassword(contrasenia)) {
                System.out.println("O AQUII:");
                String errorMsg = "Debe escribir una contraseña. Esta debe tener al menos 8 caracteres, una mayúscula, un número y un carácter especial.";
                bindingResult.rejectValue("contrasena", "error.contrasena", errorMsg);
                SedeFarmacista datosFarmacistaSede = sedeFarmacistaRepository.buscarFarmacistaSede(farmacista.getIdUsuario());
                List<EstadoUsuario> estadoUsuarioList = estadoUsuarioRepository.listarEstadosUsuarios();
                String passwordHash = farmacista.getContrasena(); // Obtener el hash de la contraseña desde la base de datos
                model.addAttribute("farmacista", farmacista);
                model.addAttribute("datosFarmacistaSede", datosFarmacistaSede);
                model.addAttribute("estadoUsuarioList", estadoUsuarioList);
                model.addAttribute("error", errorMsg); // Añade el error al modelo
                return "superAdmin/editarFarmacista";
            }

            if(contrasenia.isEmpty()) {
                System.out.println("Contra vacia: " + farmacista.getContrasena());
                String contradeAdmin = usuarioRepository.contraAdmin(farmacista.getIdUsuario());
                farmacista.setContrasena(contradeAdmin);
            }
            else{
                String hashedPassword = SHA256.cipherPassword(contrasenia);
                farmacista.setContrasena(hashedPassword);
            }

            attr.addFlashAttribute("msg", "Datos del farmacista actualizados exitosamente");
            usuarioRepository.save(farmacista);
            return "redirect:/superadmin/farmacistas";
        }
    }

    @PostMapping("/eliminarFarmacista")
    public String eliminarFarmacista(@RequestParam(value = "idFarmacista") String idFarmacista, RedirectAttributes attr){
        int idFarmacistaID = Integer.parseInt(idFarmacista);
        sedeFarmacistaRepository.eliminarFarmacistadeSedeFarmacista(idFarmacistaID);
        usuarioRepository.eliminarFarmacista(idFarmacistaID);
        attr.addFlashAttribute("msg", "Farmacista eliminado exitosamente");
        return "redirect:/superadmin/farmacistas";

    }


    @GetMapping(value = {"/crearMedicamento"})
    public String crearMedicamento(@ModelAttribute("medicamento") Medicamento medicamento, @ModelAttribute("validateImage") ValidateImage validateImage, Model model,
                                   HttpServletRequest request, HttpServletResponse response, Authentication authentication){
        //SESSION
        //Iniciamos la sesión
        HttpSession session = request.getSession();
        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuario);

        List<Sede> sedeDisponibleList = sedeRepository.findAll();
        model.addAttribute("sedeDisponibleList", sedeDisponibleList);
        return "superAdmin/crearMedicamento";
    }


    @PostMapping("/guardarMedicamento")
    public String agregarNuevoMedicamento(@ModelAttribute("medicamento") @Valid Medicamento medicamento, BindingResult bindingResul,
                                          @ModelAttribute("validateImage") @Valid ValidateImage validateImage, BindingResult binding2,
                                          @RequestParam(value = "presentacion", required = false)  String presentacion,
                                          @RequestParam(value = "tipounidad", required = false) String tipounidad,
                                          @RequestParam(value = "numunidad", required = false) String numunidad,
                                          @RequestParam(value = "nombre", required = false) String nombre,
                                          @RequestParam(value = "precioCompra", required = false) BigDecimal precioCompra,
                                          @RequestParam(value = "precioVenta", required = false) BigDecimal precioVenta,
                                          @RequestParam(value = "categorias", required = false) String categorias,
                                          @RequestParam(value = "file", required = false) Part file,
                                          RedirectAttributes attr, Model model) throws IOException {

        if(bindingResul.hasErrors() || binding2.hasErrors()){
            System.out.println("HAY ERRORES DE VALIDACIÓN:");
            for (ObjectError error : bindingResul.getAllErrors()) {
                System.out.println("- " + error.getDefaultMessage());
            }
            List<Sede> sedeDisponibleList = sedeRepository.findAll();
            model.addAttribute("sedeDisponibleList", sedeDisponibleList);
            return "superAdmin/crearMedicamento";
        }else{

            List<String> nombresUsados = medicamentoRepository.listarNombresMedicamento();

            if (nombresUsados.contains(nombre)) {
                System.out.println("El nombre está en la lista.");
                List<Sede> sedeDisponibleList = sedeRepository.findAll();
                model.addAttribute("sedeDisponibleList", sedeDisponibleList);
                bindingResul.rejectValue("nombre", "error.nombre", "El nombre del medicamento ya se encuentra registrado.");
                return "superAdmin/crearMedicamento";
            }

            if (Pattern.matches("\\d+", nombre)) {
                bindingResul.rejectValue("nombre", "error.nombre", "El nombre no puede contener solo números.");
            }

            String contentType = file.getContentType();
            if (!contentType.equals("image/png") && !contentType.equals("image/jpeg")) {
                binding2.rejectValue("file", "error.file", "Solo se aceptan archivos PNG o JPEG.");
                List<Sede> sedeDisponibleList = sedeRepository.findAll();
                model.addAttribute("sedeDisponibleList", sedeDisponibleList);
                return "superAdmin/crearMedicamento";
            }

            if(precioVenta.compareTo(precioCompra) < 0){
                bindingResul.rejectValue("precioCompra", "error.precioCompra", "El precio de compra no puede ser mayor al precio de venta.");
                List<Sede> sedeDisponibleList = sedeRepository.findAll();
                model.addAttribute("sedeDisponibleList", sedeDisponibleList);
                return "superAdmin/crearMedicamento";
            }

            InputStream inputStream = file.getInputStream();
            byte[] bytes = inputStream.readAllBytes();

            System.out.println("NO HAY ERROR");
            medicamento.setCategorias(categorias);
            medicamento.setEstado(1);
            medicamento.setImagen(bytes);
            String unidad = presentacion + ' ' + numunidad + ' ' + tipounidad;
            medicamento.setUnidad(unidad);
            System.out.println("- Unidad final " + unidad);
            medicamentoRepository.save(medicamento);
            attr.addFlashAttribute("msg", "Medicamento creado exitosamente");
            return "redirect:/superadmin/medicamentos";
        }
    }

    @GetMapping(value = {"/editarMedicamento"})
    public String editarMedicamento(@ModelAttribute("product") Medicamento medicamento,
                                    @ModelAttribute("validateImage") ValidateImage validateImage,
                                    Model model, @RequestParam("id") int id,
                                    HttpServletRequest request, HttpServletResponse response, Authentication authentication){
        //SESSION
        //Iniciamos la sesión
        HttpSession session = request.getSession();
        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuario);

        Optional<Medicamento> optionalMedicamento = medicamentoRepository.findById(id);


        if (optionalMedicamento.isPresent()) {
            medicamento = optionalMedicamento.get();
            List<Sede> sedeDisponibleList = sedeRepository.findAll();
            List<SedeStock> sedeStockList = sedeStockRepository.medicamentoPresenteSedes(id);
            List<Integer> idsSede = sedeStockRepository.listarMedicamentosEnSedePorId(id);

            model.addAttribute("idsSede", idsSede);
            model.addAttribute("sedeDisponibleList", sedeDisponibleList);
            model.addAttribute("medicamento", medicamento);
            model.addAttribute("medicamentosVisiblesSede", sedeStockList);
            return "superAdmin/editarMedicamento";
        } else {
            return "redirect:/superadmin/medicamentos";
        }
    }

    @PostMapping("/actualizarMedicamento")
    public String actualizarMedicamento(@ModelAttribute("medicamento") @Valid Medicamento medicamento, BindingResult bindingResult,
                                        @ModelAttribute("validateImage") ValidateImage validateImage, BindingResult binding2,
                                        @RequestParam(value = "nombre", required = false) String nombre,
                                        @RequestParam(value = "sedeid", required = false) List<Integer> idSedesSeleccionadas,
                                        @RequestParam(value = "file", required = false) Part file,
                                        @RequestParam(value = "precioCompra", required = false) BigDecimal precioCompra,
                                        @RequestParam(value = "precioVenta", required = false) BigDecimal precioVenta,
                                        RedirectAttributes attr, Model model) throws IOException {
        if(bindingResult.hasErrors() || binding2.hasErrors()){
            System.out.println("HAY ERRORES DE VALIDACIÓN:");
            for (ObjectError error : bindingResult.getAllErrors()) {
                System.out.println("- " + error.getDefaultMessage());
            }
            List<SedeStock> sedeStockList = sedeStockRepository.medicamentoPresenteSedes(medicamento.getIdMedicamento());
            List<Sede> sedeDisponibleList = sedeRepository.findAll();
            List<Integer> idsSede = sedeStockRepository.listarMedicamentosEnSedePorId(medicamento.getIdMedicamento());
            model.addAttribute("idsSede", idsSede);
            model.addAttribute("sedeDisponibleList", sedeDisponibleList);
            model.addAttribute("medicamentosVisiblesSede", sedeStockList);
            return "superAdmin/editarMedicamento";
        }else{

            List<String> nombresUsados = medicamentoRepository.listarNombresMedicamentoID(medicamento.getIdMedicamento());

            if (nombresUsados.contains(nombre)) {
                System.out.println("El nombre está en la lista.");
                List<SedeStock> sedeStockList = sedeStockRepository.medicamentoPresenteSedes(medicamento.getIdMedicamento());
                List<Sede> sedeDisponibleList = sedeRepository.findAll();
                List<Integer> idsSede = sedeStockRepository.listarMedicamentosEnSedePorId(medicamento.getIdMedicamento());
                model.addAttribute("idsSede", idsSede);
                model.addAttribute("sedeDisponibleList", sedeDisponibleList);
                model.addAttribute("medicamentosVisiblesSede", sedeStockList);

                bindingResult.rejectValue("nombre", "error.nombre", "El nombre del medicamento ya se encuentra registrado.");
                return "superAdmin/editarMedicamento";
            }

            if (Pattern.matches("\\d+", nombre)) {
                bindingResult.rejectValue("nombre", "error.nombre", "El nombre no puede contener solo números.");
                List<SedeStock> sedeStockList = sedeStockRepository.medicamentoPresenteSedes(medicamento.getIdMedicamento());
                List<Sede> sedeDisponibleList = sedeRepository.findAll();
                List<Integer> idsSede = sedeStockRepository.listarMedicamentosEnSedePorId(medicamento.getIdMedicamento());
                model.addAttribute("idsSede", idsSede);
                model.addAttribute("sedeDisponibleList", sedeDisponibleList);
                model.addAttribute("medicamentosVisiblesSede", sedeStockList);
            }

            if(precioVenta.compareTo(precioCompra) < 0){
                bindingResult.rejectValue("precioCompra", "error.precioCompra", "El precio de compra no puede ser mayor al precio de venta.");
                List<SedeStock> sedeStockList = sedeStockRepository.medicamentoPresenteSedes(medicamento.getIdMedicamento());
                List<Sede> sedeDisponibleList = sedeRepository.findAll();
                List<Integer> idsSede = sedeStockRepository.listarMedicamentosEnSedePorId(medicamento.getIdMedicamento());
                model.addAttribute("idsSede", idsSede);
                model.addAttribute("sedeDisponibleList", sedeDisponibleList);
                model.addAttribute("medicamentosVisiblesSede", sedeStockList);
                return "superAdmin/editarMedicamento";
            }

            if (file == null || file.getSize() == 0) {
                System.out.println("La imagen es nula o vacía.");
                Medicamento imaMedicamento = medicamentoRepository.imagenMedicamento(medicamento.getIdMedicamento());
                medicamento.setImagen(imaMedicamento.getImagen());
            } else {
                System.out.println("Se recibe una imagen.");

                String contentType = file.getContentType();
                if (!contentType.equals("image/png") && !contentType.equals("image/jpeg")) {
                    binding2.rejectValue("file", "error.file", "Solo se aceptan archivos PNG o JPEG.");
                    System.out.println("HAY ERRORES DE VALIDACIÓN:");
                    for (ObjectError error : bindingResult.getAllErrors()) {
                        System.out.println("- " + error.getDefaultMessage());
                    }
                    List<SedeStock> sedeStockList = sedeStockRepository.medicamentoPresenteSedes(medicamento.getIdMedicamento());
                    List<Sede> sedeDisponibleList = sedeRepository.findAll();
                    List<Integer> idsSede = sedeStockRepository.listarMedicamentosEnSedePorId(medicamento.getIdMedicamento());
                    model.addAttribute("idsSede", idsSede);
                    model.addAttribute("sedeDisponibleList", sedeDisponibleList);
                    model.addAttribute("medicamentosVisiblesSede", sedeStockList);
                    return "superAdmin/editarMedicamento";
                }

                InputStream inputStream = file.getInputStream();
                byte[] bytes = inputStream.readAllBytes();
                medicamento.setImagen(bytes);
            }

            medicamento.setCategorias("NNUL");
            medicamento.setEstado(1);


            if (idSedesSeleccionadas == null || idSedesSeleccionadas.isEmpty()) {
                int idMedicamento = medicamento.getIdMedicamento();
                System.out.println("Estoy en si el parámetros es NULL");
                if (file == null || file.getSize() == 0) {
                    System.out.println("La imagen es nula o vacía.");
                    Medicamento imaMedicamento = medicamentoRepository.imagenMedicamento(medicamento.getIdMedicamento());
                    medicamento.setImagen(imaMedicamento.getImagen());
                } else {
                    System.out.println("Se recibe una imagen.");

                    String contentType = file.getContentType();
                    if (!contentType.equals("image/png") && !contentType.equals("image/jpeg")) {
                        binding2.rejectValue("file", "error.file", "Solo se aceptan archivos PNG o JPEG.");
                        System.out.println("HAY ERRORES DE VALIDACIÓN:");
                        for (ObjectError error : bindingResult.getAllErrors()) {
                            System.out.println("- " + error.getDefaultMessage());
                        }
                        List<SedeStock> sedeStockList = sedeStockRepository.medicamentoPresenteSedes(medicamento.getIdMedicamento());
                        List<Sede> sedeDisponibleList = sedeRepository.findAll();
                        List<Integer> idsSede = sedeStockRepository.listarMedicamentosEnSedePorId(medicamento.getIdMedicamento());
                        model.addAttribute("idsSede", idsSede);
                        model.addAttribute("sedeDisponibleList", sedeDisponibleList);
                        model.addAttribute("medicamentosVisiblesSede", sedeStockList);
                        return "superAdmin/editarMedicamento";
                    }

                    InputStream inputStream = file.getInputStream();
                    byte[] bytes = inputStream.readAllBytes();
                    medicamento.setImagen(bytes);
                }

                medicamento.setCategorias("NNUL");
                medicamento.setEstado(1);

                sedeStockRepository.borrarSedesAnteriores(idMedicamento);
                medicamentoRepository.save(medicamento);
                attr.addFlashAttribute("msg", "Medicamento actualizado exitosamente");
                return "redirect:/superadmin/medicamentos";
            } else {
                System.out.println("Estoy en si NO el parámetros es NULL");
                int idMedicamento = medicamento.getIdMedicamento();
                List<Integer> sedeStockList = sedeStockRepository.listarMedicamentosEnSedePorId(idMedicamento);
                for (Integer idSede: sedeStockList){
                    System.out.println("Valor de idSede: " + idSede);
                }
                if(sedeStockList.isEmpty()){
                    for (Integer idSede : idSedesSeleccionadas) {
                        medicamentoRepository.asignarMedicamentoPorSede(idSede,idMedicamento,0);
                    }
                    if (file == null || file.getSize() == 0) {
                        System.out.println("La imagen es nula o vacía.");
                        Medicamento imaMedicamento = medicamentoRepository.imagenMedicamento(medicamento.getIdMedicamento());
                        medicamento.setImagen(imaMedicamento.getImagen());
                    } else {
                        System.out.println("Se recibe una imagen.");

                        String contentType = file.getContentType();
                        if (!contentType.equals("image/png") && !contentType.equals("image/jpeg")) {
                            binding2.rejectValue("file", "error.file", "Solo se aceptan archivos PNG o JPEG.");
                            System.out.println("HAY ERRORES DE VALIDACIÓN:");
                            for (ObjectError error : bindingResult.getAllErrors()) {
                                System.out.println("- " + error.getDefaultMessage());
                            }
                            List<SedeStock> sedeStockList1 = sedeStockRepository.medicamentoPresenteSedes(medicamento.getIdMedicamento());
                            List<Sede> sedeDisponibleList = sedeRepository.findAll();
                            List<Integer> idsSede = sedeStockRepository.listarMedicamentosEnSedePorId(medicamento.getIdMedicamento());
                            model.addAttribute("idsSede", idsSede);
                            model.addAttribute("sedeDisponibleList", sedeDisponibleList);
                            model.addAttribute("medicamentosVisiblesSede", sedeStockList1);
                            return "superAdmin/editarMedicamento";
                        }

                        InputStream inputStream = file.getInputStream();
                        byte[] bytes = inputStream.readAllBytes();
                        medicamento.setImagen(bytes);
                    }

                    medicamento.setCategorias("NNUL");
                    medicamento.setEstado(1);

                    medicamentoRepository.save(medicamento);
                    attr.addFlashAttribute("msg", "Medicamento actualizado exitosamente");
                    return "redirect:/superadmin/medicamentos";
                }else{
                    sedeStockRepository.borrarSedesAnteriores(idMedicamento);
                    for (Integer idSede : idSedesSeleccionadas) {
                        medicamentoRepository.asignarMedicamentoPorSede(idSede,idMedicamento,0);
                    }
                    if (file == null || file.getSize() == 0) {
                        System.out.println("La imagen es nula o vacía.");
                        Medicamento imaMedicamento = medicamentoRepository.imagenMedicamento(medicamento.getIdMedicamento());
                        medicamento.setImagen(imaMedicamento.getImagen());
                    } else {
                        System.out.println("Se recibe una imagen.");

                        String contentType = file.getContentType();
                        if (!contentType.equals("image/png") && !contentType.equals("image/jpeg")) {
                            binding2.rejectValue("file", "error.file", "Solo se aceptan archivos PNG o JPEG.");
                            System.out.println("HAY ERRORES DE VALIDACIÓN:");
                            for (ObjectError error : bindingResult.getAllErrors()) {
                                System.out.println("- " + error.getDefaultMessage());
                            }
                            List<SedeStock> sedeStockList1 = sedeStockRepository.medicamentoPresenteSedes(medicamento.getIdMedicamento());
                            List<Sede> sedeDisponibleList = sedeRepository.findAll();
                            List<Integer> idsSede = sedeStockRepository.listarMedicamentosEnSedePorId(medicamento.getIdMedicamento());
                            model.addAttribute("idsSede", idsSede);
                            model.addAttribute("sedeDisponibleList", sedeDisponibleList);
                            model.addAttribute("medicamentosVisiblesSede", sedeStockList1);
                            return "superAdmin/editarMedicamento";
                        }


                        InputStream inputStream = file.getInputStream();
                        byte[] bytes = inputStream.readAllBytes();
                        medicamento.setImagen(bytes);
                    }

                    medicamento.setCategorias("NNUL");
                    medicamento.setEstado(1);

                    medicamentoRepository.save(medicamento);
                    attr.addFlashAttribute("msg", "Medicamento actualizado exitosamente");
                    return "redirect:/superadmin/medicamentos";
                }
            }
        }
    }

    @PostMapping("/eliminarMedicamento")
    public String eliminarMedicamento(@RequestParam(value = "idMedicamento") String idMedicamento, RedirectAttributes attr){
        int idMedicamentoID = Integer.parseInt(idMedicamento);
        sedeStockRepository.borrarSedesAnteriores(idMedicamentoID);
        medicamentoRepository.eliminarMedicamento(idMedicamentoID);
        attr.addFlashAttribute("msg", "Medicamento borrado exitosamente");
        return "redirect:/superadmin/medicamentos";
    }

    //Exportar PDF para reporte
    @GetMapping("/exportarPDF")
    public void exportarListadoMedicamentosPDF(HttpServletResponse response) throws DocumentException, IOException {
        response.setContentType("application/pdf");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String fechaActual = dateFormatter.format(new Date());
        String cabecera = "Content-Disposition";
        String valor = "attachment; filename=Medicamentos_" + fechaActual + ".pdf";
        response.setHeader(cabecera, valor);

        List<VentasMedicamentosTotalDto> medicamentos = medicamentoRepository.listaMedicamentosVentas();
        List<TopVentasDto> listaVentasSede1 = ordenContenidoRepository.listartopVentarporSede(1);
        List<TopVentasDto> listaVentasSede2 = ordenContenidoRepository.listartopVentarporSede(2);
        List<TopVentasDto> listaVentasSede3 = ordenContenidoRepository.listartopVentarporSede(3);
        List<TopVentasDto> listaVentasSede4 = ordenContenidoRepository.listartopVentarporSede(4);
        List<TopVentasDto> listaVentasSede5 = ordenContenidoRepository.listartopVentarporSede(5);
        ExporterPDF exporter = new ExporterPDF(medicamentos, listaVentasSede1, listaVentasSede2, listaVentasSede3, listaVentasSede4, listaVentasSede5, medicamentoRepository);
        exporter.exportar(response);

    }

    @GetMapping("/exportarExcel")
    public void exportarListadoMedicamentosExcel(HttpServletResponse response) throws DocumentException, IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String fechaActual = dateFormatter.format(new Date());
        String cabecera = "Content-Disposition";
        String valor = "attachment; filename=OrdenesWeb_" + fechaActual + ".xlsx";
        response.setHeader(cabecera, valor);

        List<OrdenesExporterDto> listaExporter = ordenRepository.listarOrdenesExporter();
        ExporterExcel exporter = new ExporterExcel(listaExporter);
        exporter.exportar(response);

    }

    @GetMapping("/reportes")
    public String verReportes(Model model){

        List<OrdenesExporterDto> listaOrdenesReportes = ordenRepository.listarOrdenesExporter();

        model.addAttribute("listaOrdenes", listaOrdenesReportes);
        return "superAdmin/reportes";
    }



}
