package pe.sanmiguel.bienestar.proyecto_gtics.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = {"/superadmin"}, method = RequestMethod.GET)
public class SuperAdminController {
    @GetMapping(value = {""})
    public String showIndexSuperAdmin(){
        return "superAdmin/paginaInicio";
    }

    @GetMapping(value = {"/administradoresSede"})
    public String showAdministradoresSede(){
        return "superAdmin/listaAdministSede";
    }
    @GetMapping(value = {"/farmacistas"})
    public String showFarmacistas(){
        return "superAdmin/listaFarmacistas";
    }
    @GetMapping(value = {"/pacientes"})
    public String showPacientes(){
        return "superAdmin/listaPacientes";
    }
    @GetMapping(value = {"/doctores"})
    public String showDoctores(){
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
