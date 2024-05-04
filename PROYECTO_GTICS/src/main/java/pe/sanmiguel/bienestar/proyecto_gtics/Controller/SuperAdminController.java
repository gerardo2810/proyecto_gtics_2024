package pe.sanmiguel.bienestar.proyecto_gtics.Controller;

import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.Part;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.*;
import pe.sanmiguel.bienestar.proyecto_gtics.Repository.*;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLOutput;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@MultipartConfig
@RequestMapping(value = {"/superadmin"}, method = RequestMethod.GET)
public class SuperAdminController {

    final UsuarioRepository usuarioRepository;
    final DoctorRepository doctorRepository;
    final SedeRepository sedeRepository;
    final SedeFarmacistaRepository sedeFarmacistaRepository;
    final ReposicionRepository reposicionRepository;
    final ReposicionContenidoRepository reposicionContenidoRepository;
    final EstadoUsuarioRepository estadoUsuarioRepository;
    final MedicamentoRepository medicamentoRepository;

    public SuperAdminController(UsuarioRepository usuarioRepository, DoctorRepository doctorRepository, SedeRepository sedeRepository, SedeFarmacistaRepository sedeFarmacistaRepository, ReposicionRepository reposicionRepository, ReposicionContenidoRepository reposicionContenidoRepository, EstadoUsuarioRepository estadoUsuarioRepository, MedicamentoRepository medicamentoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.doctorRepository = doctorRepository;
        this.sedeRepository = sedeRepository;
        this.sedeFarmacistaRepository = sedeFarmacistaRepository;
        this.reposicionRepository = reposicionRepository;
        this.reposicionContenidoRepository = reposicionContenidoRepository;
        this.estadoUsuarioRepository = estadoUsuarioRepository;
        this.medicamentoRepository = medicamentoRepository;
    }

    Integer idVerReposicionCreada;
    Integer idEstadoFarmacista;

    @GetMapping(value = {""})
    public String showIndexSuperAdmin(Model model){
        List<Sede> adminSedelist = sedeRepository.listarAdministroresSede();
        List<SedeFarmacista> farmacistaList = sedeFarmacistaRepository.listarFarmacistasPorSede();
        List<Usuario> pacientelist = usuarioRepository.listarUsuariosSegunRol(5);
        List<Doctor> doctorList = doctorRepository.findAll();
        model.addAttribute("adminSedelist", adminSedelist);
        model.addAttribute("farmacistlist", farmacistaList);
        model.addAttribute("pacientelist", pacientelist);
        model.addAttribute("doctorList", doctorList);
        return "superAdmin/paginaInicio";
    }

    @GetMapping(value = {"/administradoresSede"})
    public String showAdministradoresSede(Model model){
        List<Sede> adminSedelist = sedeRepository.listarAdministroresSede();
        List<Sede> sedeList = sedeRepository.findAll();
        model.addAttribute("adminSedelist", adminSedelist);
        model.addAttribute("sedeList", sedeList);
        return "superAdmin/listaAdministSede";
    }
    @GetMapping(value = {"/farmacistas"})
    public String showFarmacistas(Model model){
        List<SedeFarmacista> farmacistaList = sedeFarmacistaRepository.listarFarmacistasPorSede();
        List<Sede> sedeList = sedeRepository.findAll();
        model.addAttribute("farmacistlist", farmacistaList);
        model.addAttribute("sedeList", sedeList);
        return "superAdmin/listaFarmacistas";
    }

    @GetMapping(value = {"/pacientes"})
    public String showPacientes(Model model){
        List<Usuario> pacientelist = usuarioRepository.listarUsuariosSegunRol(5);
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
    public String showPedidos(Model model){
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
                                     @RequestParam(value = "idUsuario") int idUsuario){
        System.out.println("Valor de Estado solicitud: " + idEstadoSolicitud);
        System.out.println("Valor de id farmacista: " + idUsuario);
        if(idEstadoSolicitud==1){
            sedeFarmacistaRepository.aprobarSolicitud(idUsuario);
            return "redirect:/superadmin/solicitudes";
        }else {
            sedeFarmacistaRepository.denegarSolicitud(idUsuario);
            return "redirect:/superadmin/solicitudes";
        }
    }

    @GetMapping(value = {"/masdetallesPedidos"})
    public String masDetallesPedidos(Model model){

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
    public String showSolicitudes(Model model){
        List<SedeFarmacista> solicitudesList = sedeFarmacistaRepository.listarSolicitudesFarmacistas();
        List<Sede> sedeList = sedeRepository.findAll();
        model.addAttribute("solicitudesList", solicitudesList);
        model.addAttribute("sedeList", sedeList);
        return "superAdmin/solicitudes";
    }

    @GetMapping(value = {"/medicamentos"})
    public String showMedicamentos(Model model){
        List<Medicamento> listaMedicamentos = medicamentoRepository.findAll();
        model.addAttribute("listaMedicamentos", listaMedicamentos);
        return "superAdmin/medicamentos";
    }



    @GetMapping(value = {"/cambiarContraseña"})
    public String cambiarContraseña(){
        return "superAdmin/cambiarcontraseña";
    }


    @GetMapping(value = {"/historialSolicitudes"})
    public String verHistorialSolicitudes(Model model){
        List<SedeFarmacista> solicitudesAcepRech = sedeFarmacistaRepository.listarSolicitudesAceptadasyRechazadas();
        model.addAttribute("solicitudesAcepRech", solicitudesAcepRech);
        return "superAdmin/historialSolicitudes";
    }

    @GetMapping(value = {"/aprobarSolicitudes"})
    public String aprobarSolicitudes(Model model, @RequestParam("id") int id){
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
    public String crearAdminitrador(Model model){
        List<Sede> sedeDisponibleList = sedeRepository.listarSedesDisponibles();
        model.addAttribute("sedeDisponibleList", sedeDisponibleList);
        return "superAdmin/crearAdministrador";
    }

    @PostMapping("/guardarAdministrador")
    public String agregarNuevoAdministrador(@RequestParam("nombres") String nombres,
                                             @RequestParam("apellidos") String apellidos,
                                             @RequestParam("dni") String dni,
                                            @RequestParam("direccion") String direccion,
                                             @RequestParam("distrito") String distrito,
                                            @RequestParam("sedeid") String sedeid,
                                             @RequestParam("correo") String correo,
                                             @RequestParam("contrasenia") String contrasenia,
                                             @RequestParam("telefono") String telefono){

        int estadoUsuario = 1;
        int idRol = 2;
        int idUsuario = usuarioRepository.findLastUsuarioId() + 1;
        int idsede = Integer.parseInt(sedeid);
        String seguro = "-";

        System.out.println("Hola, " + idUsuario);
        System.out.println("Valor de idSede: " + sedeid);

        usuarioRepository.crearAdministradorSede(idUsuario, idRol, correo, contrasenia, nombres, apellidos, telefono, dni, direccion, distrito, seguro, estadoUsuario);
        sedeRepository.asignarAdministradorSede(idUsuario, idsede);
        return "redirect:/superadmin/administradoresSede";
    }


    @GetMapping(value = {"/editarAdministrador"})
    public String editarAdministrador(Model model, @RequestParam("id") String id){
        int idAdmin = Integer.parseInt(id);
        Optional<Sede> optionalAdministradorSede = sedeRepository.buscarAdminID(idAdmin);
        if(optionalAdministradorSede.isPresent()){
            Usuario usuarioAdministrador = usuarioRepository.encontrarAdministradorPorId(idAdmin);
            Sede sedeAdministrador = optionalAdministradorSede.get();
            List<Sede> sedeList = sedeRepository.findAll();
            List<EstadoUsuario> estadoUsuarioList = estadoUsuarioRepository.findAll();
            model.addAttribute("sedeAdministrador", sedeAdministrador);
            model.addAttribute("sedeList", sedeList);
            model.addAttribute("administrador", usuarioAdministrador);
            model.addAttribute("estadoUsuarioList", estadoUsuarioList);
            return "superAdmin/editarAdministrador";
        }else {
            return "redirect:/superadmin/administradoresSede";
        }
    }

    @GetMapping(value = {"/crearDoctor"})
    public String crearDoctor(Model model){
        List<Sede> sedeDisponibleList = sedeRepository.findAll();
        model.addAttribute("sedeDisponibleList", sedeDisponibleList);
        return "superAdmin/crearDoctor";
    }
    @GetMapping(value = {"/editarDoctor"})
    public String editarDoctor(){
        return "superAdmin/editarDoctor";
    }


    @GetMapping(value = {"/editarFarmacista"})
    public String editarFarmacista(Model model, @RequestParam("id") int id){
        Usuario usuarioFarmacista = usuarioRepository.encontrarFarmacistaporIdActivosInactivos(id);
        Optional<SedeFarmacista> optionalSedeFarmacista = sedeFarmacistaRepository.buscarCodigoFarmacista(id);
        if(optionalSedeFarmacista.isPresent()){
            SedeFarmacista sedeFarmacista = optionalSedeFarmacista.get();
            List<EstadoUsuario> estadoUsuarioList = estadoUsuarioRepository.findAll();
            model.addAttribute("farmacista", usuarioFarmacista);
            model.addAttribute("sedeFarmacista", sedeFarmacista);
            model.addAttribute("estadoUsuarioList", estadoUsuarioList);
            return "superAdmin/editarFarmacista";
        }else {
            return "redirect:/superadmin/farmacistas";
        }
    }

    @PostMapping("/actualizarDatosFarmacista")
    public String actualizarDatosFarmacista(Usuario usuario, RedirectAttributes attr,
                                   SedeFarmacista sedeFarmacista){
        usuarioRepository.save(usuario);
        Optional<SedeFarmacista> optionalSedeFarmacista = sedeFarmacistaRepository.buscarCodigoFarmacista(usuario.getIdUsuario());
        if(optionalSedeFarmacista.isPresent()){
            SedeFarmacista sedeFarmacistaOld = optionalSedeFarmacista.get();
            sedeFarmacista.setId(sedeFarmacistaOld.getId());
            sedeFarmacista.setIdFarmacista(sedeFarmacistaOld.getIdFarmacista());
            sedeFarmacista.setAprobado(sedeFarmacistaOld.getAprobado());
            sedeFarmacistaRepository.save(sedeFarmacista);
            return "redirect:/superadmin/farmacistas";

        }else {
            return "redirect:/superadmin/farmacistas";
        }
    }

    @GetMapping("/eliminarFarmacista")
    public String eliminarFarmacista(@RequestParam("id") int id){

        sedeFarmacistaRepository.eliminarFarmacistadeSedeFarmacista(id);
        usuarioRepository.eliminarFarmacistadeUsuario(id);
        return "redirect:/superadmin/farmacistas";

    }


    @GetMapping(value = {"/crearMedicamento"})
    public String crearMedicamento(@ModelAttribute("medicamento") Medicamento medicamento, Model model){
        List<Sede> sedeDisponibleList = sedeRepository.findAll();
        model.addAttribute("sedeDisponibleList", sedeDisponibleList);
        return "superAdmin/crearMedicamento";
    }


    @PostMapping("/guardarMedicamento")
    public String agregarNuevoMedicamento(@ModelAttribute("medicamento") Medicamento medicamento, @RequestParam("imagen") Part imagen, RedirectAttributes attr, Model model) throws IOException {

            InputStream inputStream = imagen.getInputStream();
            byte[] bytes = inputStream.readAllBytes();
            medicamento.setImagen(bytes);
            medicamentoRepository.save(medicamento);
            return "redirect:/superadmin/medicamentos";
    }

    @GetMapping(value = {"/editarMedicamento"})
    public String editarMedicamento(@ModelAttribute("product") Medicamento medicamento, Model model, @RequestParam("id") int id){

        Optional<Medicamento> optionalMedicamento = medicamentoRepository.findById(id);

        if (optionalMedicamento.isPresent()) {
            medicamento = optionalMedicamento.get();
            model.addAttribute("medicamento", medicamento);
            return "superAdmin/editarMedicamento";
        } else {
            return "redirect:/superadmin/medicamentos";
        }
    }
}
