package pe.sanmiguel.bienestar.proyecto_gtics.Controller;

import org.springframework.boot.Banner;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.sanmiguel.bienestar.proyecto_gtics.Dto.MedicamentosSedeStockDto;
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

    public AdminSedeController(UsuarioRepository usuarioRepository, SedeRepository sedeRepository, SedeStockRepository sedeStockRepository, MedicamentoRepository medicamentoRepository, OrdenRepository ordenRepository, OrdenContenidoRepository ordenContenidoRepository, ReposicionRepository reposicionRepository, EstadoPreOrdenRepository estadoPreOrdenRepository, DoctorRepository doctorRepository, SedeFarmacistaRepository sedeFarmacistaRepository) {
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
    }

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

    @GetMapping(value = "")
    public String showIndexAdminSede(){
        return "adminsede/inicio";
    }

    @GetMapping(value = {"/doctores"})
    public String showDoctors(Model model){
        List<Doctor> listaDoctores = doctorRepository.listarDoctores();
        model.addAttribute("listaDoctoresD", listaDoctores);
        return "adminsede/doctores";
    }

    @GetMapping(value = "/farmacista")
    public String showFarmacistas(Model model){
        List<Usuario> listaFarmacistas = usuarioRepository.listarFarmacistas();
        model.addAttribute("listaFarmacistas", listaFarmacistas);
        return "adminsede/farmacistas";
    }

    @GetMapping(value = "/ordenes")
    public String showOrders(){
        return "adminsede/ordenes_reposicion";
    }

    @GetMapping(value = "/editar_farmacista")
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

    @GetMapping(value = "/editar_orden_reposicion")
    public String editOrden(){
        return "adminsede/editar_orden_reposicion";
    }

    @GetMapping(value = "/medicamentos")
    public String showMedicamentos(Model model){
        List<Medicamento> listaMedicamentos = medicamentoRepository.findAll();
        sedeSession = sedeRepository.getSedeByIdSede(1);
        model.addAttribute("sedeSession", sedeSession);
        model.addAttribute("listaMedicamentos", listaMedicamentos);

        //List<MedicamentosSedeStockDto> listaMedicamentosSedeStock = medicamentoRepository.listarMedicamentosSedeStock(1); //seteamos por default
        //model.addAttribute("listaMedicamentosSedeStock", listaMedicamentosSedeStock);
        return "adminsede/medicamentos_sede";
    }

    @GetMapping("/solicitud_farmacista")
    public String solicitudFarmacista(){
        return "adminsede/solicitud_agregar_farmacista";
    }

    @GetMapping("/ver_ordenes_entregadas")
    public String verOrdenesEntregadas(){
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

    @GetMapping("/verDetalles")
    public String verDetalles(){
        return "adminsede/verDetalles";
    }

    @GetMapping("/eliminar_farmacista")
    public String eliminarFarmacista(@RequestParam("id") int id){

        sedeFarmacistaRepository.eliminarFarmacistadeSedeFarmacista(id);
        usuarioRepository.eliminarFarmacistadeUsuario(id);
        return "redirect:/adminsede/farmacista";

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
            sedeFarmacista.setIdUsuario(sedeFarmacistaOld.getIdUsuario());
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




}
