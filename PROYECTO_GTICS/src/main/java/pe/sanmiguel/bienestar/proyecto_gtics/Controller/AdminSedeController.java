package pe.sanmiguel.bienestar.proyecto_gtics.Controller;

import org.springframework.boot.Banner;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.SedeFarmacista;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Usuario;
import pe.sanmiguel.bienestar.proyecto_gtics.Repository.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = {"/adminsede"}, method = RequestMethod.GET)
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

    @GetMapping(value = {""})
    public String showIndexAdminSede(){
        return "/adminsede/inicio";
    }

    @GetMapping(value = {"/doctores"})
    public String showDoctors(){
        return "/adminsede/doctores";
    }

    @GetMapping(value = {"/farmacista"})
    public String showFarmacistas(Model model){
        List<Usuario> listaFarmacistas = usuarioRepository.listarFarmacistas();
        model.addAttribute("listaFarmacistas", listaFarmacistas);
        return "/adminsede/farmacistas";
    }

    @GetMapping(value = {"/ordenes"})
    public String showOrders(){
        return "/adminsede/ordenes_reposicion";
    }

    @GetMapping(value = {"/editar_farmacista"})
    public String editFarmacista(@RequestParam("id") int id,
                                 Model model){
        Usuario usuarioFarmacista = usuarioRepository.encontrarFarmacistaporId(id);
        Optional<SedeFarmacista> optionalSedeFarmacista = sedeFarmacistaRepository.buscarCodigoFarmacista(id);
        if(optionalSedeFarmacista.isPresent()){
            SedeFarmacista sedeFarmacista = optionalSedeFarmacista.get();
            //String codigoMed = sedeFarmacista.getCodigoMed();
            //model.addAttribute("codigoMed", codigoMed);
            model.addAttribute("farmacista", usuarioFarmacista);
            model.addAttribute("sedeFarmacista", sedeFarmacista);
            return "/adminsede/editar_farmacista";
        }else {
            return "redirect:/adminsede/farmacista";

        }

    }

    @GetMapping(value = {"/editar_orden_reposicion"})
    public String editOrden(){
        return "/adminsede/editar_orden_reposicion";
    }

    @GetMapping(value = {"/medicamentos"}) //Aquiiiiiiiiiiiiiii
    public String showMedicamentos(){
        return "/adminsede/medicamentos_sede";
    }

    @GetMapping(value = {"/solicitud_farmacista"})
    public String solicitudFarmacista(){
        return "/adminsede/solicitud_agregar_farmacista";
    }

    @GetMapping(value = {"/generar_orden"})
    public String generarOrden(){
        return "/adminsede/generar_orden";
    }

    @GetMapping(value = {"/ver_ordenes_entregadas"})
    public String verOrdenesEntregadas(){
        return "/adminsede/ver_ordenes_entregadas";
    }

    @GetMapping(value = {"/cambiar_contrasena"})
    public String vistaCambiarContra(){
        return "/adminsede/cambiar_contrasena_adminsede";
    }

    @GetMapping(value = {"/perfil_adminsede"})
    public String vistaPerfil(){
        return "/adminsede/perfil_adminsede";
    }

    @GetMapping(value = {"/verDetalles"})
    public String verDetalles(){
        return "/adminsede/verDetalles";
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

    @PostMapping(value = {"/editarFarmacista"})
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




}
