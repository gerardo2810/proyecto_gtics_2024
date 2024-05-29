package pe.sanmiguel.bienestar.proyecto_gtics.Controller;


import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import jakarta.websocket.SessionException;
import org.apache.catalina.Session;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.*;
import pe.sanmiguel.bienestar.proyecto_gtics.Repository.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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

    public PacienteController(OrdenContenidoRepository ordenContenidoRepository, EstadoPreOrdenRepository estadoPreOrdenRepository, DoctorRepository doctorRepository,EstadoOrdenRepository estadoOrdenRepository,TipoOrdenRepository tipoOrdenRepository,SedeRepository sedeRepository, MedicamentoRepository medicamentoRepository, UsuarioRepository usuarioRepository, OrdenRepository ordenRepository){
        this.medicamentoRepository = medicamentoRepository;
        this.usuarioRepository = usuarioRepository;
        this.ordenRepository = ordenRepository;
        this.sedeRepository = sedeRepository;
        this.tipoOrdenRepository = tipoOrdenRepository;
        this.estadoOrdenRepository = estadoOrdenRepository;
        this.doctorRepository = doctorRepository;
        this.estadoPreOrdenRepository = estadoPreOrdenRepository;
        this.ordenContenidoRepository = ordenContenidoRepository;
    }


    Integer idUsuario = 23;






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
    public String pago_tarjeta(Model model, @RequestParam(value="id") Integer id){

        Orden orden = ordenRepository.getOrdenByIdOrden(id);

        model.addAttribute("orden", orden);

        return "paciente/pago_tarjeta";
    }




    @GetMapping(value="/tracking")
    public String tracking(Model model, @RequestParam("id") String idOrden){

        Integer idInteger = Integer.parseInt(idOrden);
        Orden orden = ordenRepository.getById(idInteger);
        Integer cantProductos = ordenContenidoRepository.cantProductos(idOrden);

        List<OrdenContenido> lista = ordenContenidoRepository.findMedicamentosByOrdenId(idOrden);

        model.addAttribute("cantProductos", cantProductos);
        model.addAttribute("lista", lista);
        model.addAttribute("ordenActual", orden);

        return "paciente/tracking";
    }



    @GetMapping(value="/new_orden")
    public String new_orden(Model model, @ModelAttribute("usuario") Usuario usuario, @ModelAttribute("ordenDto") OrdenDto ordenDto){

        List<Medicamento> listaMedicamentos = medicamentoRepository.findAll();

        List<Doctor> listaDoctores = doctorRepository.findAll();
        model.addAttribute("listaDoctores", listaDoctores);
        model.addAttribute("listaMedicamentos", listaMedicamentos);


        return "paciente/new_orden";}

    @GetMapping(value="/mensajeria")
    public String mensajeria(){return "paciente/mensajeria";}

    @GetMapping(value = "/chat")
    public String chat(Model model){

        try {
            InetAddress localhost = InetAddress.getLocalHost();
            System.out.println("Mi dirección IP local es: " + localhost.getHostAddress());
            model.addAttribute("iplocal", localhost.getHostAddress());
        } catch (UnknownHostException e) {
            System.out.println("Errooooooooooooooooooooooooor");
            e.printStackTrace();
        }


        return "paciente/chat";
    }

    @GetMapping(value = "/orden_paciente_stock")
    public String ordenPacienteStock(){
        return "paciente/orden_paciente_stock";
    }

    @GetMapping(value = "/reemplazar_medicamentos")
    public String ordenReemplazoMedicamentos(){
        return "paciente/reemplazar_medicamentos";
    }

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

    @GetMapping(value = "/confirmar_pago")
    public String confirmarPago(Model model, @RequestParam("id") String idOrden){

        Integer cantProductos = ordenContenidoRepository.cantProductos(idOrden);

        Integer idInteger = Integer.parseInt(idOrden);
        Orden orden = ordenRepository.getById(idInteger);

        List<OrdenContenido> lista = ordenContenidoRepository.findMedicamentosByOrdenId(idOrden);

        model.addAttribute("lista", lista);
        model.addAttribute("cantProductos", cantProductos);
        model.addAttribute("ordenActual", orden);

        return "paciente/confirmar_pago";
    }

    @GetMapping(value="/boleta_pago")
    public String boletaPago(Model model, @RequestParam(value="id") Integer idOrden){

        List<OrdenContenido> lista = ordenContenidoRepository.findMedicamentosByOrdenId(String.valueOf(idOrden));


        model.addAttribute("orden", ordenRepository.getOrdenByIdOrden(idOrden));
        model.addAttribute("medicamentos", lista);

        return "paciente/boleta";
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
                               @RequestParam(value = "priceTotal", required = false) Float total,
                               @RequestParam(value = "seguro", required = false) String seguro,
                               Model model, RedirectAttributes redirectAttributes,
                               HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {


        HttpSession session = request.getSession();
        Usuario usuario1 = (Usuario) session.getAttribute("usuario");


        System.out.println(total);

        if(bindingResult.hasErrors() || bin2.hasErrors()){


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

            return "paciente/new_orden";

        }else{


            InputStream inputStream = file.getInputStream();
            byte[] bytes = inputStream.readAllBytes();


            String tracking = new String();
            tracking= ordenRepository.findLastOrdenId()+1 + "-2024";
            LocalDateTime fechaIni = LocalDateTime.now();
            LocalDateTime fechaFin = LocalDateTime.now();
            Integer idFarmacista = new Integer(1); //el id del Farmacista
            Sede s = sedeRepository.getById(1); //el id de la Sede
            Doctor doc = doctorRepository.getById(idDoctor); //el id del doctor




            Orden orden = new Orden();
            orden.setIdOrden(ordenRepository.findLastOrdenId()+1);
            orden.setTracking(tracking);
            orden.setFechaIni(fechaIni);
            orden.setFechaFin(fechaFin);
            orden.setPrecioTotal(total);
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
    public String pago_recibido(Model model, RedirectAttributes redirectAttributes,  @RequestParam(value="numTarjeta") String tarjeta,
                                                                        @RequestParam(value="mes") String mes,
                                                                        @RequestParam(value="anio") String anio,
                                                                        @RequestParam(value="cvv") String cvv,
                                                                        @RequestParam(value="titular") String titular,
                                                                        @RequestParam(value="idOrden") Integer idOrden){

        System.out.printf(tarjeta);
        System.out.println(mes);
        System.out.println(anio);
        System.out.printf(cvv);
        System.out.println(titular);
        System.out.println(idOrden);
        ordenRepository.actualizarEstadoOrden(3,idOrden);


        redirectAttributes.addFlashAttribute("msg2", "Orden Creada");
        return "redirect:/paciente/boleta_pago?id=" + idOrden;
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
