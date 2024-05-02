package pe.sanmiguel.bienestar.proyecto_gtics.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.sanmiguel.bienestar.proyecto_gtics.Dto.MedicamentosSedeStockDto;
import pe.sanmiguel.bienestar.proyecto_gtics.Dto.ReposicionContenidoMedicamentoDto;
import pe.sanmiguel.bienestar.proyecto_gtics.Dto.UsuarioSedeFarmacistaDto;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.*;
import pe.sanmiguel.bienestar.proyecto_gtics.Repository.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/adminsede", method = RequestMethod.GET)
public class AdminSedeController {

    /* Repositorios */
    final UsuarioRepository usuarioRepository;
    final SedeRepository sedeRepository;
    final SedeStockRepository sedeStockRepository;
    final MedicamentoRepository medicamentoRepository;
    final OrdenRepository ordenRepository;
    final OrdenContenidoRepository ordenContenidoRepository;
    final ReposicionRepository reposicionRepository;
    final EstadoPreOrdenRepository estadoPreOrdenRepository;
    final DoctorRepository doctorRepository;
    final SedeFarmacistaRepository sedeFarmacistaRepository;
    final ReposicionContenidoRepository reposicionContenidoRepository;

    public AdminSedeController(UsuarioRepository usuarioRepository, SedeRepository sedeRepository, SedeStockRepository sedeStockRepository, MedicamentoRepository medicamentoRepository, OrdenRepository ordenRepository, OrdenContenidoRepository ordenContenidoRepository, ReposicionRepository reposicionRepository, EstadoPreOrdenRepository estadoPreOrdenRepository, DoctorRepository doctorRepository, SedeFarmacistaRepository sedeFarmacistaRepository, ReposicionContenidoRepository reposicionContenidoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.sedeRepository = sedeRepository;
        this.sedeStockRepository = sedeStockRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.ordenRepository = ordenRepository;
        this.ordenContenidoRepository = ordenContenidoRepository;
        this.reposicionRepository  =reposicionRepository;
        this.estadoPreOrdenRepository = estadoPreOrdenRepository;
        this.doctorRepository = doctorRepository;
        this.sedeFarmacistaRepository = sedeFarmacistaRepository;
        this.reposicionContenidoRepository = reposicionContenidoRepository;
    }
    /*Variables locales*/
    Sede sedeVistaReposicion = new Sede();
    Usuario administradorVistaReposicion = new Usuario();
    Reposicion reposicionMostrar = new Reposicion();
    List<ReposicionContenidoMedicamentoDto> listaMostrarNuevaCompra;


    public List<String> getCantidadesFromLista(List<String> listaSelectedIds) {
        List<String> cantidades = new ArrayList<>();
        for (int i = 0; i + 1 < listaSelectedIds.size(); i += 2) {
            cantidades.add(listaSelectedIds.get(i + 1));
        }
        return cantidades;
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

    Sede sedeSession;
    List<Medicamento> medicamentosSeleccionados = new ArrayList<>();
    List<String> listaCantidades = new ArrayList<>();

    @GetMapping("")
    public String showIndexAdminSede(Model model){
        //SESSION
        int idSede = 1;
        int finalIdReposicion = reposicionRepository.findLastReposicionIdNoEntregado();
        int preFinalIdReposicion = finalIdReposicion - 1;
        List<Reposicion> listaReposicionNoEntregadasUltimas = reposicionRepository.listarOrdenesReposicionNoEntregadasUltimas(idSede, finalIdReposicion, preFinalIdReposicion);
        if(listaReposicionNoEntregadasUltimas.size() == 0){
            return "adminsede/inicio";
        }
        model.addAttribute("listaReposicionNoEntregadasUltimas", listaReposicionNoEntregadasUltimas);
        return "adminsede/inicio";
    }

    @GetMapping("/doctores")
    public String showDoctors(Model model){
        List<Doctor> listaDoctores = doctorRepository.listarDoctores();
        model.addAttribute("listaDoctoresD", listaDoctores);
        return "adminsede/doctores";
    }

    @GetMapping("/farmacista")
    public String showFarmacistas(Model model){
        //List<Usuario> listaFarmacistas = usuarioRepository.listarFarmacistas();
        //SESSION
        int idSede = 1;
        List<UsuarioSedeFarmacistaDto> listaFarmacistasNew = usuarioRepository.listarSedeFarmacista(idSede);
        model.addAttribute("listaFarmacistasNew", listaFarmacistasNew);
        //model.addAttribute("listaFarmacistas", listaFarmacistas);
        return "adminsede/farmacistas";
    }

    @GetMapping("/ordenes")
    public String showOrders(Model model){
        //SESSION
        int idSede = 1;
        List<Reposicion> listaReposicionNoEntregadas = reposicionRepository.listarOrdenesReposicionNoEntregadas(idSede) ;
        model.addAttribute("listaReposicionNoEntregadas", listaReposicionNoEntregadas);
        return "adminsede/ordenes_reposicion";
    }

    @GetMapping("/editar_farmacista")
    public String editFarmacista(@RequestParam("id") int id,
                                 Model model){
        Usuario usuarioFarmacista = usuarioRepository.encontrarFarmacistaporId(id);
        Optional<SedeFarmacista> optionalSedeFarmacista = sedeFarmacistaRepository.buscarCodigoFarmacista(id);
        if(optionalSedeFarmacista.isPresent()){
            SedeFarmacista sedeFarmacista = optionalSedeFarmacista.get();
            model.addAttribute("farmacista", usuarioFarmacista);
            model.addAttribute("sedeFarmacista", sedeFarmacista);
            return "adminsede/editar_farmacista";
        }else {
            return "redirect:/adminsede/farmacista";

        }

    }

    @GetMapping("/editar_orden_reposicion")
    public String editOrden(@RequestParam(name = "state", required = false) String state,
                            @RequestParam("id") int id,
                            Model model){

        int idSede = 1;
        List<ReposicionContenidoMedicamentoDto> listaMedicamentosSeleccionados = reposicionContenidoRepository.listaMostrarMedicamentosSeleccionados(id);

        if("agotado".equals(state)){
            List<MedicamentosSedeStockDto> listaMedicamentosAgotados = medicamentoRepository.listarMedicamentosStockAgotados(idSede);
            model.addAttribute("idOrden", id);
            model.addAttribute("listaMedicamentosSedeStock", listaMedicamentosAgotados);
            model.addAttribute("listaMedicamentosSeleccionados", listaMedicamentosSeleccionados);
            return "adminsede/editar_orden_reposicion";
        } else if ("poragotar".equals(state)) {
            List<MedicamentosSedeStockDto> listaMedicamentosPorAgotar = medicamentoRepository.listarMedicamentosStockPorAgotar(idSede);
            model.addAttribute("idOrden", id);
            model.addAttribute("listaMedicamentosSedeStock", listaMedicamentosPorAgotar);
            model.addAttribute("listaMedicamentosSeleccionados", listaMedicamentosSeleccionados);
            return "adminsede/editar_orden_reposicion";
        }else {
            List<MedicamentosSedeStockDto> listaMedicamentosporAgotaroAgotados = medicamentoRepository.listarMedicamentosStockPorAgotaroAgotados(idSede);
            model.addAttribute("idOrden", id);
            model.addAttribute("listaMedicamentosSedeStock", listaMedicamentosporAgotaroAgotados);
            model.addAttribute("listaMedicamentosSeleccionados", listaMedicamentosSeleccionados);
            return "adminsede/editar_orden_reposicion";
        }
    }

    @GetMapping("/medicamentos")
    public String showMedicamentos(@RequestParam(name = "state", required = false) String state,
                                   Model model) {
        int idSession = 1; //Sede 1
        sedeSession = sedeRepository.getSedeByIdSede(idSession);
        model.addAttribute("sedeSession", sedeSession);

        if ("disponible".equals(state)) {
            List<MedicamentosSedeStockDto> listaMedicamentosStockDisponible = medicamentoRepository.listarMedicamentosStockDisponible(idSession);
            model.addAttribute("listaMedicamentosSedeStock", listaMedicamentosStockDisponible);
            return "adminsede/medicamentos_sede";

        } else if ("agotado".equals(state)) {
            List<MedicamentosSedeStockDto> listaMedicamentosStockAgotado = medicamentoRepository.listarMedicamentosStockAgotados(idSession);
            model.addAttribute("listaMedicamentosSedeStock", listaMedicamentosStockAgotado);
            return "adminsede/medicamentos_sede";

        } else if ("poragotar".equals(state)) {
            List<MedicamentosSedeStockDto> listaMedicamentosPorAgotar = medicamentoRepository.listarMedicamentosStockPorAgotar(idSession);
            model.addAttribute("listaMedicamentosSedeStock", listaMedicamentosPorAgotar);
            return "adminsede/medicamentos_sede";

        } else {
            List<MedicamentosSedeStockDto> listaMedicamentosSedeStock = medicamentoRepository.listarMedicamentosSedeStock(idSession); //seteamos por default
            model.addAttribute("listaMedicamentosSedeStock", listaMedicamentosSedeStock);
            return "adminsede/medicamentos_sede";
        }

    }

    @GetMapping("/solicitud_farmacista")
    public String solicitudFarmacista(){
        return "adminsede/solicitud_agregar_farmacista";
    }

    @GetMapping("/ver_ordenes_entregadas")
    public String verOrdenesEntregadas(Model model){
        int idSede = 1; //CORREGIRRRRRRRRRRRR CON SESIONNNNNNNNNNNNNNN
        List<Reposicion> listaOrdenesEntregadas = reposicionRepository.listarOrdenesReposicionEntregadas(idSede);
        model.addAttribute("listaOrdenesEntregadas", listaOrdenesEntregadas);
        return "adminsede/ver_ordenes_entregadas";
    }

    @GetMapping("/cambiar_contrasena")
    public String vistaCambiarContra(){
        return "adminsede/cambiar_contrasena_adminsede";
    }

    @GetMapping("/perfil_adminsede")
    public String vistaPerfil(){
        return "adminsede/perfil_adminsede";
    }

    @GetMapping("/verDetalles") //VER DETALLES DE NUEVA COMPRA
    public String verDetalles(Model model){

        model.addAttribute("sede", sedeVistaReposicion);
        model.addAttribute("administradorMostrar", administradorVistaReposicion);
        model.addAttribute("reposicion", reposicionMostrar);
        model.addAttribute("listaMostrarNuevaCompra", listaMostrarNuevaCompra);
        return "adminsede/verDetalles";
    }

    @GetMapping("/eliminar_farmacista")
    public String eliminarFarmacista(@RequestParam("id") int id){

        sedeFarmacistaRepository.eliminarFarmacistadeSedeFarmacista(id);
        usuarioRepository.eliminarFarmacistadeUsuario(id);
        return "redirect:/adminsede/farmacista";

    }

    @GetMapping("/generar_orden_forms")
    public String generarOrden(Model model){

        List<Integer> stockSeleccionados = new ArrayList<>();

        for (Medicamento med : medicamentosSeleccionados) {
            if (sedeStockRepository.getSedeStockByIdSedeAndIdMedicamento(sedeSession, med).isPresent()) {
                stockSeleccionados.add(sedeStockRepository.getSedeStockByIdMedicamentoAndIdSede(med,sedeSession).getCantidad());
            } else {
                stockSeleccionados.add(0);
            }
        }

        model.addAttribute("stockSeleccionados", stockSeleccionados);
        model.addAttribute("medicamentosSeleccionados", medicamentosSeleccionados);
        model.addAttribute("listaCantidades", listaCantidades);

        return "adminsede/generar_orden";
    }

    /*
    @PostMapping(value = {"/editarFarmacista"})
    @ResponseBody
    public String editarFarmacista(@RequestParam("idUsuario") int id,
                                   @RequestParam("nombres") String nombre,
                                   @RequestParam("apellidos") String apellido,
                                   @RequestParam("dni") String dni,
                                   @RequestParam("distrito") String distrito,
                                   @RequestParam("correo") String correo){

        usuarioRepository.editarFarmacista(id,nombre,apellido,dni,distrito,correo);
        return "redirect: /adminsede/farmacista";
    }*/


    /*---------------------------------------------------------POST---------------------------------------------------------*/

    @PostMapping("/editarFarmacista")
    public String editarFarmacista(Usuario usuario, RedirectAttributes attr,
                                   SedeFarmacista sedeFarmacista){
        usuarioRepository.save(usuario);
        Optional<SedeFarmacista> optionalSedeFarmacista = sedeFarmacistaRepository.buscarCodigoFarmacista(usuario.getIdUsuario());
        if(optionalSedeFarmacista.isPresent()){
            SedeFarmacista sedeFarmacistaOld = optionalSedeFarmacista.get();
            sedeFarmacista.setId(sedeFarmacistaOld.getId());
            sedeFarmacista.setIdFarmacista(sedeFarmacistaOld.getIdFarmacista());
            sedeFarmacista.setAprobado(sedeFarmacistaOld.getAprobado());
            sedeFarmacistaRepository.save(sedeFarmacista);
            return "redirect:/adminsede/farmacista";

        }else {
            return "redirect:/adminsede/farmacista";
        }


    }
    @PostMapping("/solicitud_farmacista_post")
    public String solicitudAgregarFarmacista(@RequestParam("nombres") String nombres,
                                             @RequestParam("apellidos") String apellidos,
                                             @RequestParam("dni") String dni,
                                             @RequestParam("distrito") String distrito,
                                             @RequestParam("correo") String correo,
                                             @RequestParam("contrasena") String contrasena,
                                             @RequestParam("celular") String celular,
                                             @RequestParam("codigoMed") String codigoMed,
                                             @RequestParam("direccion") String direccion,
                                             @RequestParam("seguro") String seguro){

        int estadoUsuario = 2;
        int idRol = 3;
        int idUsuario = usuarioRepository.findLastUsuarioId() + 1;
        int aprobado = 2; //El farmacista no está aprobado
        int idSede = 1; //Cambiar
        usuarioRepository.crearFarmacista(idUsuario, idRol, correo, contrasena, nombres, apellidos, celular, dni, direccion, distrito, seguro, estadoUsuario);
        sedeFarmacistaRepository.crearSedeFarmacista(idSede, idUsuario, codigoMed, aprobado);
        return "redirect:/adminsede/farmacista";
    }

    @PostMapping("/generar_orden")
    public String fillContentOrder(@RequestParam("listaIds") List<String> listaSelectedIds){
        if (!listaSelectedIds.isEmpty()){
            medicamentosSeleccionados = getMedicamentosFromLista(listaSelectedIds);
            listaCantidades = getCantidadesFromLista(listaSelectedIds);
            return "redirect:/adminsede/generar_orden_forms";
        } else {
            return "redirect:/adminsede/medicamentos";
        }
    }

    @PostMapping("/detalles_orden")
    public String detallesOrdenPost(@RequestParam("nombres") String nombres,
                                    @RequestParam("apellidos") String apellidos,
                                    @RequestParam("dni") String dni,
                                    @RequestParam("direccionSede") String direccionSede,
                                    @RequestParam("correo") String correo,
                                    @RequestParam("nombreSede") String nombreSede,
                                    @RequestParam("priceTotal") Float priceTotal,
                                    @RequestParam("listaIds") List<String> listaIds){

        // Crear orden de reposición
        Integer idReposicion = reposicionRepository.findLastReposicionId() + 1;
        String tracking = "RECIBIDO"; //Por Default
        Integer idEstado = 1; //Por Default
        Integer idSede = 1; // CORREGIR CON SESSION--------------------------------------------------------
        reposicionRepository.crearOrdenReposicion(idReposicion, tracking, priceTotal, idEstado, idSede);
        List<Medicamento> listaMedicamentosReposicion = new ArrayList<>();

        // Guardar lista de medicamentos
        int m = 0;
        for (int i = 1; i <= (listaIds.size()/2); i++){

            Integer idMedicamento = Integer.parseInt(listaIds.get(m));
            Integer cantidadMedicamento = Integer.parseInt(listaIds.get(m+1));
            reposicionContenidoRepository.guardarContenidoReposicion(idMedicamento, idReposicion, cantidadMedicamento);


            m = m + 2;
        }

        sedeVistaReposicion.setNombre(nombreSede);
        sedeVistaReposicion.setDireccion(direccionSede);
        administradorVistaReposicion.setDni(dni);
        administradorVistaReposicion.setCorreo(correo);
        administradorVistaReposicion.setNombres(nombres);
        administradorVistaReposicion.setApellidos(apellidos);
        reposicionMostrar = reposicionRepository.encontrarReposicionporId(idReposicion);
        listaMostrarNuevaCompra = reposicionContenidoRepository.listaMostrarDetalleNuevaCompra(idReposicion); //lista a mostrar

        return "redirect:/adminsede/verDetalles";
    }

    @PostMapping("/verDetallesOrdenEntregado")
    public String verDetallesOrdenEntregado(@RequestParam("idOrden") int idOrden){

        listaMostrarNuevaCompra = reposicionContenidoRepository.listaMostrarDetalleNuevaCompra(idOrden);
        reposicionMostrar = reposicionRepository.encontrarReposicionporId(idOrden);

        return "redirect:/adminsede/verDetallesOrdenEntregada";
    }

    @GetMapping("/verDetallesOrdenEntregada")
    public String verDetallesOrdenEntregado(Model model){
        model.addAttribute("listaMostrarNuevaCompra", listaMostrarNuevaCompra);
        model.addAttribute("reposicionMostrar", reposicionMostrar);

        //Datos que se corregirán con sesion para la Sede --------------------------------------------
        sedeVistaReposicion.setNombre("Administrador 1");
        sedeVistaReposicion.setDireccion("Av. Los Pinos 656");

        //Datos que se corregirán con sesion para el Administrador --------------------------------------------
        administradorVistaReposicion.setDni("12345678");
        administradorVistaReposicion.setCorreo("admin.sede1@gmail.com");
        administradorVistaReposicion.setNombres("Admin");
        administradorVistaReposicion.setApellidos("Sede1");

        model.addAttribute("sede", sedeVistaReposicion);
        model.addAttribute("administradorMostrar", administradorVistaReposicion);
        model.addAttribute("reposicion", reposicionMostrar);
        model.addAttribute("listaMostrarNuevaCompra", listaMostrarNuevaCompra);
        return "adminsede/verDetallesOrdenEntregada";
    }

    @GetMapping("/eliminar_orden_reposicion")
    public String eliminar_orden_reposicion(@RequestParam("id") int idReposicion){

        reposicionContenidoRepository.eliminarContenidoReposicion(idReposicion);
        reposicionRepository.eliminarReposicionporId(idReposicion);
        return "redirect:/adminsede/ordenes";
    }






}
