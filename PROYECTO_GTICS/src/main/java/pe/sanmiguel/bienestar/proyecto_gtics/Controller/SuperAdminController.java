package pe.sanmiguel.bienestar.proyecto_gtics.Controller;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.*;
import pe.sanmiguel.bienestar.proyecto_gtics.PasswordService;
import pe.sanmiguel.bienestar.proyecto_gtics.Repository.*;
import pe.sanmiguel.bienestar.proyecto_gtics.SHA256;


import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLOutput;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
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


    public SuperAdminController(UsuarioRepository usuarioRepository, DoctorRepository doctorRepository, SedeRepository sedeRepository, SedeFarmacistaRepository sedeFarmacistaRepository, ReposicionRepository reposicionRepository, ReposicionContenidoRepository reposicionContenidoRepository, EstadoUsuarioRepository estadoUsuarioRepository, MedicamentoRepository medicamentoRepository, SedeStockRepository sedeStockRepository, SedeDoctorRepository sedeDoctorRepository) {
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
        session.setAttribute("usuario", usuarioSession);

        List<Sede> adminSedelist = sedeRepository.listarAdministroresSede();
        List<SedeFarmacista> farmacistaList = sedeFarmacistaRepository.listarFarmacistasPorSede();
        List<Usuario> pacientelist = usuarioRepository.listarUsuariosSegunRol(4);
        List<Doctor> doctorList = doctorRepository.findAll();
        model.addAttribute("adminSedelist", adminSedelist);
        model.addAttribute("farmacistlist", farmacistaList);
        model.addAttribute("pacientelist", pacientelist);
        model.addAttribute("doctorList", doctorList);
        return "superAdmin/paginaInicio";
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
        List<Usuario> adminSinSede = usuarioRepository.listarAdministradoresSinSede();
        List<Usuario> adminBaneados = usuarioRepository.listarAdministradoresBaneados();
        List<Sede> sedeList = sedeRepository.findAll();
        model.addAttribute("adminSedelist", adminSedelist);
        model.addAttribute("sedeList", sedeList);
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
            sedeFarmacistaRepository.aprobarSolicitud(idUsuario);
            attr.addFlashAttribute("msg", "Solicitud aceptada exitosamente");
            try {
                sendTemporaryPasswordEmailFarmacista(correoSolicitud, contrasena);
            } catch (MessagingException e) {
                System.err.println("Error al enviar o recibir correo: " + e.getMessage());
                e.printStackTrace();
            }
            String hashedPassword = SHA256.cipherPassword(contrasena);
            usuarioRepository.actualizarContrasenaFarmacista(hashedPassword, idUsuario);
            usuarioRepository.actualizarEstadoFarmacista(idUsuario);
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
    public String showMedicamentos(Model model,
                                   HttpServletRequest request, HttpServletResponse response, Authentication authentication){
        //SESSION
        //Iniciamos la sesión
        HttpSession session = request.getSession();
        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuario);

        List<Medicamento> listaMedicamentos = medicamentoRepository.listarMedicamentosActivos();
        model.addAttribute("listaMedicamentos", listaMedicamentos);
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

    int sedeId;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @PostMapping("/guardarAdministrador")
    public String agregarNuevoAdministrador(@ModelAttribute("administrador") @Valid Usuario administrador, BindingResult bindingResult,
                                            @RequestParam(value = "sedeid", required = false) String idSede,
                                            @RequestParam(value = "dni", required = false) String dni,
                                            @RequestParam(value = "correo", required = false) String correo,
                                            RedirectAttributes attr, Model model) throws IOException, MessagingException {
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

            // Generar contraseña temporal
            String temporaryPassword = passwordService.generateTemporaryPassword();
            System.out.println("Contraseña temporal." + temporaryPassword);
            String hashedPassword = SHA256.cipherPassword(temporaryPassword);
            administrador.setContrasena(hashedPassword);
             // Indicar que el usuario debe cambiar la contraseña en el primer inicio de sesión


            if(idSede == null || idSede.isEmpty()){
                System.out.println("ID ADMIN: " + administrador.getIdUsuario());

                administrador.setRol(2);
                administrador.setEstadoUsuario(5);
                usuarioRepository.save(administrador);
                sendTemporaryPasswordEmail(administrador.getCorreo(), temporaryPassword);
                attr.addFlashAttribute("msg", "Nuevo administrador creado exitosamente");
                return "redirect:/superadmin/administradoresSede";
            }else{
                if(administrador.getIdUsuario() != 0){
                    System.out.println("ID ADMIN si no es vacio: " + administrador.getIdUsuario());
                    int idsede = Integer.parseInt(idSede);

                    administrador.setRol(2);
                    administrador.setEstadoUsuario(1);
                    usuarioRepository.save(administrador);
                    sedeRepository.asignarAdministradorSede(administrador.getIdUsuario(), idsede);
                    sendTemporaryPasswordEmail(administrador.getCorreo(), temporaryPassword);
                    attr.addFlashAttribute("msg", "Nuevo administrador creado exitosamente");
                    return "redirect:/superadmin/administradoresSede";
                }else{
                    System.out.println("ID ADMIN si es vacio: " + administrador.getIdUsuario());

                    administrador.setRol(2);
                    administrador.setEstadoUsuario(5);
                    usuarioRepository.save(administrador);
                    sendTemporaryPasswordEmail(administrador.getCorreo(), temporaryPassword);
                    attr.addFlashAttribute("msg", "Nuevo administrador creado exitosamente");
                    return "redirect:/superadmin/administradoresSede";
                }
            }
        }
    }

    private void sendTemporaryPasswordEmail(String to, String temporaryPassword) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom("bienestar.sanmiguel1@outlook.com");
        helper.setTo(to);
        helper.setSubject("Contraseña Temporal para Nuevo Administrador");


        String emailContent = "<html>" +
                "<body style='font-family: Arial, sans-serif; margin: 0; padding: 0;'>" +
                "<div style='background-color: #007BFF; padding: 20px; text-align: center;'>" +
                "<h1 style='color: #1B4F72; margin: 0;'>Bienestar <span style='color: #FFFFFF;'>San Miguel</span></h1>" +
                "</div>" +
                "<div style='border: 2px solid #007BFF; border-radius: 10px; padding: 20px; margin: 20px;'>" +
                "<h2 style='color: #007BFF;'>Hola,</h2>" +
                "<p>Tu cuenta de administrador ha sido creada. Por favor, usa la siguiente contraseña temporal para iniciar sesión:</p>" +
                "<p style='font-size: 20px; color: #007BFF; font-weight: bold; text-align: center;'>" + temporaryPassword + "</p>" +
                "<p>Debes cambiar esta contraseña inmediatamente después de tu primer inicio de sesión. La contraseña temporal es válida por 24 horas. Si no cambias tu contraseña en este tiempo, tendrás que solicitar una nueva.</p>" +
                "<p>Gracias.</p>" +
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
    public String asignarNuevaSedeAdministrador(Usuario administrador,
                                                Model model, @RequestParam("id") String id, RedirectAttributes attr,
                                                HttpServletRequest request, HttpServletResponse response, Authentication authentication){
        int idAdmin = Integer.parseInt(id);
        Optional<Usuario> optionalUsuario = usuarioRepository.findById(idAdmin);
        System.out.println("Valor Dentro de editar de optional usuario: " + optionalUsuario.get().getIdUsuario());
        if(optionalUsuario.isPresent()){
            administrador = optionalUsuario.get();
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
    public String asignaraNuevoAdministrador(@ModelAttribute("administrador") Usuario administrador, @RequestParam(value = "sedeid", required = false) String idSede, RedirectAttributes attr, Model model) throws IOException {

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
                sedeRepository.asignarAdministradorSede(administrador.getIdUsuario(), idsede);
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

            InputStream inputStream = file.getInputStream();
            byte[] bytes = inputStream.readAllBytes();

            System.out.println("NO HAY ERROR");
            medicamento.setCategorias("NNUL");
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

            InputStream inputStream = file.getInputStream();
            byte[] bytes = inputStream.readAllBytes();

            medicamento.setCategorias("NNUL");
            medicamento.setEstado(1);
            medicamento.setImagen(bytes);

            if (idSedesSeleccionadas == null || idSedesSeleccionadas.isEmpty()) {
                int idMedicamento = medicamento.getIdMedicamento();
                System.out.println("Estoy en si el parámetros es NULL");
                medicamento.setImagen(null);
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
                    medicamento.setImagen(null);
                    medicamentoRepository.save(medicamento);
                    attr.addFlashAttribute("msg", "Medicamento actualizado exitosamente");
                    return "redirect:/superadmin/medicamentos";
                }else{
                    sedeStockRepository.borrarSedesAnteriores(idMedicamento);
                    for (Integer idSede : idSedesSeleccionadas) {
                        medicamentoRepository.asignarMedicamentoPorSede(idSede,idMedicamento,0);
                    }
                    medicamento.setImagen(null);
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


}
