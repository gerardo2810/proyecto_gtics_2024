package pe.sanmiguel.bienestar.proyecto_gtics.Controller;


import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import jakarta.websocket.SessionException;
import org.apache.catalina.Session;
import org.apache.poi.ddf.EscherPictBlip;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.sanmiguel.bienestar.proyecto_gtics.ChatFirebase;
import pe.sanmiguel.bienestar.proyecto_gtics.ChatService;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.*;
import pe.sanmiguel.bienestar.proyecto_gtics.Mensaje;
import pe.sanmiguel.bienestar.proyecto_gtics.Repository.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.servlet.http.HttpSession; // Importar HttpSession
import pe.sanmiguel.bienestar.proyecto_gtics.SHA256;


@Controller
@RequestMapping(value="/paciente", method= RequestMethod.GET)
public class PacienteController {


    /*----------------- Repositories -----------------*/
    final MedicamentoRepository medicamentoRepository;
    final OrdenRepository ordenRepository;
    final UsuarioRepository usuarioRepository;
    final TipoOrdenRepository tipoOrdenRepository;
    final EstadoOrdenRepository estadoOrdenRepository;
    final SedeRepository sedeRepository;
    final DoctorRepository doctorRepository;
    final EstadoPreOrdenRepository estadoPreOrdenRepository;
    final OrdenContenidoRepository ordenContenidoRepository;
    final SedeStockRepository sedeStockRepository;

    final ChatRepository chatRepository;

    public PacienteController(ChatRepository chatRepository, SedeStockRepository sedeStockRepository, OrdenContenidoRepository ordenContenidoRepository, EstadoPreOrdenRepository estadoPreOrdenRepository, DoctorRepository doctorRepository,EstadoOrdenRepository estadoOrdenRepository,TipoOrdenRepository tipoOrdenRepository,SedeRepository sedeRepository, MedicamentoRepository medicamentoRepository, UsuarioRepository usuarioRepository, OrdenRepository ordenRepository){
        this.medicamentoRepository = medicamentoRepository;
        this.usuarioRepository = usuarioRepository;
        this.ordenRepository = ordenRepository;
        this.sedeRepository = sedeRepository;
        this.tipoOrdenRepository = tipoOrdenRepository;
        this.estadoOrdenRepository = estadoOrdenRepository;
        this.doctorRepository = doctorRepository;
        this.estadoPreOrdenRepository = estadoPreOrdenRepository;
        this.ordenContenidoRepository = ordenContenidoRepository;
        this.chatRepository = chatRepository;
        this.sedeStockRepository = sedeStockRepository;
    }


    Integer idUsuario = 23;

    @Autowired
    private ChatService chatService;


    /*----------------- Method: GET -----------------*/

    @GetMapping(value="")
    public String preOrdenes(Model model, HttpServletRequest request, HttpServletResponse response, Authentication authentication){

        HttpSession session = request.getSession();
        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName());
        if (usuario == null) {
            usuario = usuarioRepository.findByCorreo(authentication.getName());
            session.setAttribute("usuario", usuario);
        }

        List<Orden> lista =  ordenRepository.listarPreOrdenes(usuario.getIdUsuario());
        model.addAttribute("lista",lista);

        return "paciente/pre_ordenes";
    }



    @GetMapping(value="/ordenes")
    public String ordenes(Model model, HttpServletRequest request, HttpServletResponse response, Authentication authentication){


        HttpSession session = request.getSession();
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        List<Orden> lista =  ordenRepository.listarOrdenes(usuario.getIdUsuario());

        if (usuario != null) {
            // Imprimir el nombre del usuario autenticado
            System.out.println("Nombre del usuario autenticado: " + usuario.getNombres());
        } else {
            System.out.println("No hay usuario autenticado en la sesión.");
        }



        model.addAttribute("lista",lista);

        return "paciente/ordenes";
    }



    @GetMapping(value="/pago_tarjeta")
    public String pago_tarjeta(HttpSession session, Model model, @RequestParam(value="id") Integer id,  @ModelAttribute("tarjeta") Tarjeta tarjeta){
        Usuario userSession = (Usuario) session.getAttribute("usuario");

        Optional<Orden> orden = Optional.ofNullable(ordenRepository.ordenFaltaPago(id, userSession.getIdUsuario()));

        if(orden.isPresent()){
            model.addAttribute("idOrden", orden.get().getIdOrden());
            return "paciente/pago_tarjeta";
        }else{
            return "redirect:/paciente/ordenes";
        }

    }


    @GetMapping(value = "/chat/{idOrden}/{userId1}/{userId2}")
    public String chat(HttpSession session, @PathVariable String userId1, @PathVariable String userId2,@PathVariable String idOrden, Model model) {
        model.addAttribute("userId1", userId1);
        model.addAttribute("userId2", userId2);
        model.addAttribute("idOrden", idOrden);
        model.addAttribute("idOrdenInteger", Integer.parseInt(idOrden) + 10000);

        Chat chatActual  = chatRepository.buscarChat(Integer.parseInt(userId1),Integer.parseInt(userId2), Integer.parseInt(idOrden));

        /*----------------------IP LOCAL-------------------------*/
        try {InetAddress localhost = InetAddress.getLocalHost();
            System.out.println("Mi dirección IP local es: " + localhost.getHostAddress());
            model.addAttribute("iplocal", localhost.getHostAddress());} catch (UnknownHostException e) {
            System.out.println("Error obteniendo la dirección IP local");
            e.printStackTrace();}
        /*------------------------------------------------------*/


        Usuario userSession = (Usuario) session.getAttribute("usuario");
        if (chatActual != null && userSession != null && (userSession.getIdUsuario().toString().equals(userId1) || userSession.getIdUsuario().toString().equals(userId2))) {

            System.out.println("El usuario pertenece al chat");

            /*----------------------FIREBASE-------------------------*/
            List <ChatFirebase> chats = null;
            try {chats = chatService.getChatsByIdChat(chatActual.getIdChat());}
            catch (InterruptedException | ExecutionException e) {e.printStackTrace();}
            System.out.println(chats);
            /*------------------------------------------------------*/


            Usuario farmacista = usuarioRepository.getById(Integer.parseInt(userId2));

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

            model.addAttribute("medicamentosSinStock", medicamentosSinStock);
            model.addAttribute("medicamentosEnStock", medicamentosEnStock);
            model.addAttribute("idChat", chatActual.getIdChat());
            model.addAttribute("idUser", userSession.getIdUsuario());
            model.addAttribute("farmacista", farmacista);
            model.addAttribute("mensajes", chats);


            return "paciente/chat";
        } else {
            System.out.println("El usuario no pertenece al chat");
            return "paciente/mensajeria";
        }
    }


    @GetMapping(value="/tracking")
    public String tracking(Model model, @RequestParam("id") String idOrden, HttpSession session){
        Usuario userSession = (Usuario) session.getAttribute("usuario");

        Integer idInteger = Integer.parseInt(idOrden);
        Optional<Orden> orden = Optional.ofNullable(ordenRepository.ordenXusuario(idInteger, userSession.getIdUsuario()));

        if(orden.isPresent()){
            Integer cantProductos = ordenContenidoRepository.cantProductos(idOrden);
            List<OrdenContenido> lista = ordenContenidoRepository.findMedicamentosByOrdenId(idOrden);

            model.addAttribute("cantProductos", cantProductos);
            model.addAttribute("lista", lista);
            model.addAttribute("ordenActual", orden.get());

            return "paciente/tracking";
        }else{
            return "redirect:/paciente";
        }

    }



    @GetMapping(value="/new_orden")
    public String new_orden(HttpSession session, Model model, @ModelAttribute("usuario") Usuario usuario, @ModelAttribute("ordenDto") OrdenDto ordenDto, HttpServletRequest request, HttpServletResponse response, Authentication authentication){

        Usuario userSession = (Usuario) session.getAttribute("usuario");
        model.addAttribute("usuario", userSession);


        List<Medicamento> listaMedicamentos = medicamentoRepository.findAll();
        List<Doctor> listaDoctores = doctorRepository.findAll();
        model.addAttribute("listaDoctores", listaDoctores);
        model.addAttribute("listaMedicamentos", listaMedicamentos);
        model.addAttribute("emptyInput", 1);

        return "paciente/new_orden";}

    @GetMapping(value="/mensajeria")
    public String mensajeria(HttpSession session, Model model){

        Usuario userSession = (Usuario) session.getAttribute("usuario");;

        List<Chat> lista = chatRepository.listaChatsParaPaciente(userSession.getIdUsuario());

        model.addAttribute("listaChats", lista);


        return "paciente/mensajeria";}


    @GetMapping(value = "/perfil")
    public String perfil(){
        return "perfil";
    }

    @GetMapping(value = "/cambio_contrasena")
    public String cambioContrasena(){
        return "paciente/cambioContraseña";
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

    @PostMapping("/actualizar_contrasena")
    public String actualizarContrasena(Usuario paciente, BindingResult bindingResult,
                                       @RequestParam(value = "newContrasena", required = true) String newContrasena,
                                       @RequestParam(value = "confirmContrasena", required = true) String confirmContrasena,
                                       @RequestParam(value = "oldContrasena", required = true) String oldContrasena,
                                       RedirectAttributes attr, Model model,
                                       HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        HttpSession session = request.getSession();
        Usuario usuarioSession = usuarioRepository.findByCorreo(authentication.getName());
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
        return "redirect:/paciente/perfil";
    }

    @GetMapping(value = "/confirmar_pago")
    public String confirmarPago(Model model, HttpSession session, @RequestParam("id") String idOrden){
        Usuario userSession = (Usuario) session.getAttribute("usuario");

        Integer idInteger = Integer.parseInt(idOrden);
        Optional<Orden> orden = Optional.ofNullable(ordenRepository.ordenFaltaPago(idInteger, userSession.getIdUsuario()));

        if(orden.isPresent()){

            Integer cantProductos = ordenContenidoRepository.cantProductos(idOrden);
            List<OrdenContenido> lista = ordenContenidoRepository.findMedicamentosByOrdenId(idOrden);
            model.addAttribute("lista", lista);
            model.addAttribute("cantProductos", cantProductos);
            model.addAttribute("ordenActual", orden.get());

            return "paciente/confirmar_pago";

        }else{
            return "redirect:/paciente/ordenes";
        }
    }

    @GetMapping(value="/boleta_pago")
    public String boletaPago(Model model, @RequestParam(value="id") Integer idOrden, HttpSession session){
        Usuario userSession = (Usuario) session.getAttribute("usuario");
        Optional<Orden> orden = Optional.ofNullable(ordenRepository.ordenesPagadas(idOrden, userSession.getIdUsuario()));

        if(orden.isPresent()){
            List<OrdenContenido> lista = ordenContenidoRepository.findMedicamentosByOrdenId(String.valueOf(idOrden));
            model.addAttribute("orden", ordenRepository.getOrdenByIdOrden(idOrden));
            model.addAttribute("medicamentos", lista);

            return "paciente/boleta";
        }else{
            return "redirect:/paciente/ordenes";
        }


    }

    /*----------------- Foto de medicamentos-----------------*/


    @GetMapping(value="/foto_medicamento")
    public String fotoMedicamento(){
        return "farmacista/deprecated/subir_foto_medicamento";
    }

    @PostMapping(value="/guardarFotoMedicamento")
    public String subirFotoMedicamento(@RequestParam(value="idMedicamento", required = false) Integer id,
                                       @RequestParam(value="file", required = false) Part file,
                                       RedirectAttributes attr) throws IOException {

        InputStream inputStream = file.getInputStream();
        byte[] bytes = inputStream.readAllBytes();

        Optional <Medicamento> med = medicamentoRepository.findById(id);

        if (med.isPresent()){
            System.out.println(bytes.length);
            System.out.println(id);

            medicamentoRepository.imagenMedicamento(bytes, id);

        }



        return"redirect:/paciente/foto_medicamento";

    }






    /*----------------- Method: POST -----------------*/

    @PostMapping(value = "/guardarOrden")
    public String guardarOrden(@ModelAttribute("usuario") @Valid Usuario usuario, BindingResult bindingResult,
                               @ModelAttribute("ordenDto") @Valid OrdenDto ordenDto, BindingResult bin2,
                               @RequestParam(value = "file", required = false) Part file,
                               @RequestParam(value = "listaIds", required = false) List<Integer> lista,
                               @RequestParam(value = "idDoctor", required = false) Integer idDoctor,
                               @RequestParam(value = "priceTotal", required = false) String total,
                               @RequestParam(value = "seguro", required = false) String seguro,
                               Model model, RedirectAttributes redirectAttributes,
                               HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        HttpSession session = request.getSession();
        Usuario usuario1 = (Usuario) session.getAttribute("usuario");

        String fileName = file.getSubmittedFileName();
        String[] partes = fileName.split("\\.");
        String extension = null;
        if (partes.length > 1) {
            extension = partes[partes.length - 1];  // Obtener la última parte como extensión
            System.out.println("Extensión: " + extension);

        } else {
            System.out.println("No se pudo determinar la extensión del archivo.");
        }



        if(bindingResult.hasErrors() || bin2.hasErrors() || (extension != null && (!extension.equals("png") || !extension.equals("jpg")) )){
            System.out.println(bindingResult.getAllErrors());
            System.out.printf(String.valueOf(file));





            List<Medicamento> listaMedicamentos = medicamentoRepository.findAll();
            List<Doctor> listaDoctores = doctorRepository.findAll();
            model.addAttribute("listaDoctores", listaDoctores);
            model.addAttribute("listaMedicamentos", listaMedicamentos);
            if(!lista.isEmpty()){
                List<Medicamento> medicamentosSeleccionados = getMedicamentosFromLista(lista);
                List<String> listaCantidades = getCantidadesFromLista(lista);
                model.addAttribute("currentMed", medicamentosSeleccionados);
                model.addAttribute("currentCant", listaCantidades);
            }
            if(extension != null && (!extension.equals("png") || !extension.equals("jpg") || !extension.equals("jpeg")) ){
                model.addAttribute("extensionIncorrecta", 0);
                System.out.println(extension);
            }



            return "paciente/new_orden";

        }else{


            InputStream inputStream = file.getInputStream();
            byte[] bytes = inputStream.readAllBytes();


            String tracking = new String();
            tracking= ordenRepository.findLastOrdenId()+1 + "-2024";
            LocalDateTime fechaIni = LocalDateTime.now();
            LocalDateTime fechaFin = LocalDateTime.now();
            Integer idFarmacista = new Integer(120); //el id del Farmacista
            Sede s = sedeRepository.getById(5); //el id de la Sede
            Doctor doc = doctorRepository.getById(idDoctor); //el id del doctor




            Orden orden = new Orden();
            orden.setIdOrden(ordenRepository.findLastOrdenId()+1);
            System.out.println("Ultimo id en db de orden: " + ordenRepository.findLastOrdenId());
            orden.setTracking(tracking);
            orden.setFechaIni(fechaIni);
            orden.setFechaFin(fechaFin);
            total = total.replace(",", "");
            orden.setPrecioTotal(Float.parseFloat(total));
            orden.setIdFarmacista(idFarmacista);
            orden.setPaciente(usuario1);
            orden.setTipoOrden(2);
            orden.setEstadoOrden(1);
            orden.setSede(s);
            orden.setDoctor(doc);
            orden.setEstadoPreOrden(1);
            orden.setSeguroUsado(seguro);
            orden.setImagen(bytes);



            ordenRepository.save(orden);



            Integer i = new Integer(0);
            Integer cantidad = new Integer(0);

            OrdenContenido ordenContenido = new OrdenContenido();
            OrdenContenidoId oid = new OrdenContenidoId();

            ordenContenido.setIdOrden(orden);
            oid.setIdOrden(orden.getIdOrden());

            while(i < lista.size()){

                Medicamento medicamento = medicamentoRepository.getById(lista.get(i));
                cantidad = lista.get(i+1);

                oid.setIdMedicamento(medicamento.getIdMedicamento());
                ordenContenido.setId(oid);
                ordenContenido.setIdMedicamento(medicamento);
                ordenContenido.setCantidad(cantidad);
                ordenContenidoRepository.save(ordenContenido);

                i = i + 2;


            }

            redirectAttributes.addFlashAttribute("msg", "Orden Creada");


            return "redirect:/paciente/ordenes";


        }


    }






    @PostMapping(value="/procesar_pago")
    public String pago_recibido(Model model, @ModelAttribute("tarjeta") @Valid Tarjeta tarjeta, BindingResult bindingResult,
                                                                        RedirectAttributes redirectAttributes,
                                                                        @RequestParam(value="numero_tarjeta") String tarjeta2,
                                                                        @RequestParam(value="mes") String mes,
                                                                        @RequestParam(value="anio") String anio,
                                                                        @RequestParam(value="cvv") String cvv,
                                                                        @RequestParam(value="titular") String titular,
                                                                        @RequestParam(value="idOrden") Integer idOrden){

        System.out.println(idOrden);

        if(bindingResult.hasErrors()){


            System.out.println(bindingResult.getAllErrors());
            model.addAttribute("idOrden",idOrden);

            return "paciente/pago_tarjeta";

        }else{

            ordenRepository.actualizarEstadoOrden(3,idOrden);
            redirectAttributes.addFlashAttribute("msg2", "Orden Creada");
            return "redirect:/paciente/boleta_pago?id=" + idOrden;
        }






    }



    public List<Medicamento> getMedicamentosFromLista(List<Integer> listaSelectedIds) {
        List<Optional<Medicamento>> optionals = new ArrayList<>();
        List<Medicamento> seleccionados;
        for (int i = 0; i < listaSelectedIds.size(); i += 2) {
            optionals.add(medicamentoRepository.findById(Integer.valueOf(listaSelectedIds.get(i))));
        }
        seleccionados = optionals.stream().flatMap(Optional::stream).collect(Collectors.toList());

        return seleccionados;
    }

    public List<String> getCantidadesFromLista(List<Integer> listaSelectedIds) {
        List<String> cantidades = new ArrayList<>();
        for (int i = 0; i + 1 < listaSelectedIds.size(); i += 2) {
            cantidades.add(String.valueOf(listaSelectedIds.get(i + 1)));
        }
        return cantidades;
    }
}
