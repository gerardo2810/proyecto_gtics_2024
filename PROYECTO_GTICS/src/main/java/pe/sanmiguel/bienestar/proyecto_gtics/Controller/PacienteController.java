package pe.sanmiguel.bienestar.proyecto_gtics.Controller;


import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.Part;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
import java.util.Base64;
import java.util.List;


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
    public String preOrdenes(Model model){
        List<Orden> lista =  ordenRepository.listarPreOrdenes();
        model.addAttribute("lista",lista);

        return "paciente/pre_ordenes";
    }



    @GetMapping(value="/ordenes")
    public String ordenes(Model model){
        List<Orden> lista =  ordenRepository.listarOrdenes();

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

        List<OrdenContenido> lista = ordenContenidoRepository.findMedicamentosByOrdenId(idOrden);

        //Como mandar una imagen a una vista:
        //String base64Encoded = Base64.getEncoder().encodeToString(orden.getImagen());
        //model.addAttribute("imagen", base64Encoded);

        model.addAttribute("lista", lista);
        model.addAttribute("ordenActual", orden);

        return "paciente/tracking";
    }

    @GetMapping(value="/tracking_wait")
    public String tracking_wait(){ return "paciente/tracking_pendiente";}

    @GetMapping(value="/tracking_end")
    public String tracking_end(){ return "paciente/tracking_finalizado";}

    @GetMapping(value="/new_orden")
    public String new_orden(Model model){

        List<Medicamento> listaMedicamentos = medicamentoRepository.findAll();
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
        return "paciente/perfil";
    }

    @GetMapping(value = "/cambio_contrasena")
    public String cambioContrasena(){
        return "paciente/cambioContraseña";
    }


    @GetMapping(value = "/confirmar_pago")
    public String confirmarPago(Model model, @RequestParam("id") String idOrden){

        Integer idInteger = Integer.parseInt(idOrden);
        Orden orden = ordenRepository.getById(idInteger);

        List<OrdenContenido> lista = ordenContenidoRepository.findMedicamentosByOrdenId(idOrden);

        model.addAttribute("lista", lista);
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




    /*----------------- Method: POST -----------------*/

    @PostMapping(value = "/guardarOrden")
    public String guardarOrden(Usuario usuario,
                               @RequestParam(value = "doctor", required = false) String doctor,
                               @RequestParam(value = "fecha", required = false) String fecha,
                               @RequestParam(value = "imagen", required = false) Part imagen,
                               @RequestParam(value = "listaIds", required = false) List<Integer> lista,
                               @RequestParam(value = "priceTotal", required = false) Float total,
                               Model model, RedirectAttributes redirectAttributes) throws IOException {


        InputStream inputStream = imagen.getInputStream();
        byte[] bytes = inputStream.readAllBytes();







        String tracking = new String();
        tracking= ordenRepository.findLastOrdenId()+1 + "-2024";
        LocalDateTime fechaIni = LocalDateTime.now();
        LocalDateTime fechaFin = LocalDateTime.now();
        Integer idFarmacista = new Integer(1); //el id del Farmacista
        Usuario udb = usuarioRepository.getById(1); // el id del usuario Paciente que realiza la orden
        Sede s = sedeRepository.getById(1); //el id de la Sede
        Doctor doc = doctorRepository.getById(1); //el id del doctor




        Orden orden = new Orden();
        orden.setIdOrden(ordenRepository.findLastOrdenId()+1);
        orden.setTracking(tracking);
        orden.setFechaIni(fechaIni);
        orden.setFechaFin(fechaFin);
        orden.setPrecioTotal(total);
        orden.setIdFarmacista(idFarmacista);
        orden.setPaciente(udb);
        orden.setTipoOrden(2);
        orden.setEstadoOrden(1);
        orden.setSede(s);
        orden.setDoctor(doc);
        orden.setEstadoPreOrden(1);

        //Imagen de receta proxima a usar
        //orden.setImagen(bytes);



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
}
