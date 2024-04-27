package pe.sanmiguel.bienestar.proyecto_gtics.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.*;
import pe.sanmiguel.bienestar.proyecto_gtics.Repository.DoctorRepository;
import pe.sanmiguel.bienestar.proyecto_gtics.Repository.SedeFarmacistaRepository;
import pe.sanmiguel.bienestar.proyecto_gtics.Repository.SedeRepository;
import pe.sanmiguel.bienestar.proyecto_gtics.Repository.UsuarioRepository;

import java.util.List;

@Controller
@RequestMapping(value = {"/superadmin"}, method = RequestMethod.GET)
public class SuperAdminController {

    final UsuarioRepository usuarioRepository;
    final DoctorRepository doctorRepository;
    final SedeRepository sedeRepository;
    final SedeFarmacistaRepository sedeFarmacistaRepository;

    public SuperAdminController(UsuarioRepository usuarioRepository, DoctorRepository doctorRepository, SedeRepository sedeRepository, SedeFarmacistaRepository sedeFarmacistaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.doctorRepository = doctorRepository;
        this.sedeRepository = sedeRepository;
        this.sedeFarmacistaRepository = sedeFarmacistaRepository;
    }

    @GetMapping(value = {""})
    public String showIndexSuperAdmin(Model model){
        List<Sede> adminSedelist = sedeRepository.listarAdministroresSede();
        List<Usuario> pacientelist = usuarioRepository.listarUsuariosSegunRol(3);
        List<Doctor> doctorList = doctorRepository.findAll();
        model.addAttribute("adminSedelist", adminSedelist);
        model.addAttribute("pacientelist", pacientelist);
        model.addAttribute("doctorList", doctorList);
        return "superAdmin/paginaInicio";
    }

    @GetMapping(value = {"/administradoresSede"})
    public String showAdministradoresSede(Model model){
        List<Sede> adminSedelist = sedeRepository.listarAdministroresSede();
        model.addAttribute("adminSedelist", adminSedelist);
        return "superAdmin/listaAdministSede";
    }
    @GetMapping(value = {"/farmacistas"})
    public String showFarmacistas(){
        return "superAdmin/listaFarmacistas";
    }
    @GetMapping(value = {"/pacientes"})
    public String showPacientes(Model model){
        List<Usuario> pacientelist = usuarioRepository.listarUsuariosSegunRol(3);
        model.addAttribute("pacientelist", pacientelist);
        return "superAdmin/listaPacientes";
    }
    @GetMapping(value = {"/doctores"})
    public String showDoctores(Model model){
        List<Doctor> doctorList = doctorRepository.findAll();
        model.addAttribute("doctorList", doctorList);
        return "superAdmin/listaDoctores";
    }

    @GetMapping(value = {"/pedidos"})
    public String showPedidos(){
        return "superAdmin/pedidos";
    }
    @GetMapping(value = {"/solicitudes"})
    public String showSolicitudes(){
        return "superAdmin/solicitudes";
    }

    @GetMapping(value = {"/medicamentos"})
    public String showMedicamentos(){
        return "superAdmin/medicamentos";
    }

    @GetMapping(value = {"/cambiarContraseña"})
    public String cambiarContraseña(){
        return "superAdmin/cambiarcontraseña";
    }

    @GetMapping(value = {"/masdetallesPedidos"})
    public String masDetallesPedidos(){
        return "superAdmin/masdetallesPedidos";
    }
    @GetMapping(value = {"/historialSolicitudes"})
    public String verHistorialSolicitudes(){
        return "superAdmin/historialSolicitudes";
    }

    @GetMapping(value = {"/aprobarSolicitudes"})
    public String aprobarSolicitudes(){
        return "superAdmin/verificarSolicitudes";
    }

    @GetMapping(value = {"/crearAdministrador"})
    public String crearAdminitrador(){
        return "superAdmin/crearAdministrador";
    }

    @GetMapping(value = {"/editarAdministrador"})
    public String editarAdminitrador(){
        return "superAdmin/editarAdministrador";
    }
    @GetMapping(value = {"/crearDoctor"})
    public String crearDoctor(){
        return "superAdmin/crearDoctor";
    }
    @GetMapping(value = {"/editarDoctor"})
    public String editarDoctor(){
        return "superAdmin/editarDoctor";
    }

    @GetMapping(value = {"/editarFarmacista"})
    public String editarFarmacista(){
        return "superAdmin/editarFarmacista";
    }

    @GetMapping(value = {"/crearMedicamento"})
    public String crearMedicamento(){
        return "superAdmin/crearMedicamento";
    }
    @GetMapping(value = {"/editarMedicamento"})
    public String editarMedicamento(){
        return "superAdmin/editarMedicamento";
    }
}
