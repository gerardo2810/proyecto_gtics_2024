package pe.sanmiguel.bienestar.proyecto_gtics.Controller;

import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.Part;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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

    private int idUsuario;
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

    Integer idVerReposicionCreada;
    Integer idEstadoFarmacista;

    @GetMapping(value = {""})
    public String showIndexSuperAdmin(Model model){
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
    public String showAdministradoresSede(Model model){
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
    public String showFarmacistas(Model model){
        List<SedeFarmacista> farmacistaList = sedeFarmacistaRepository.listarFarmacistasActivosInactivos();
        List<Sede> sedeList = sedeRepository.findAll();
        model.addAttribute("farmacistlist", farmacistaList);
        model.addAttribute("sedeList", sedeList);
        return "superAdmin/listaFarmacistas";
    }

    @GetMapping(value = {"/pacientes"})
    public String showPacientes(Model model){
        List<Usuario> pacientelist = usuarioRepository.listarUsuariosSegunRol(4);
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
        List<Medicamento> listaMedicamentos = medicamentoRepository.listarMedicamentosActivos();
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
    public String crearAdminitrador(Model model, @ModelAttribute("administrador") @Valid Usuario administrador, BindingResult bindingResul, RedirectAttributes attr){
        List<Sede> sedeDisponibleList = sedeRepository.listarSedesDisponibles();
        model.addAttribute("sedeDisponibleList", sedeDisponibleList);
        return "superAdmin/crearAdministrador";
    }

    int sedeId;
    @PostMapping("/guardarAdministrador")
    public String agregarNuevoAdministrador(@ModelAttribute("administrador") @Valid Usuario administrador, BindingResult bindingResult, @RequestParam(value = "sedeid", required = false) String idSede, RedirectAttributes attr, Model model) throws IOException {
        if(bindingResult.hasErrors()){
            System.out.println("HAY ERRORES DE VALIDACIÓN:");
            for (ObjectError error : bindingResult.getAllErrors()) {
                System.out.println("- " + error.getDefaultMessage());
            }
            List<Sede> sedeDisponibleList = sedeRepository.listarSedesDisponibles();
            model.addAttribute("sedeDisponibleList", sedeDisponibleList);
            return "superAdmin/crearAdministrador";
        }else{

            if(idSede == null || idSede.isEmpty()){
                administrador.setRol(2);
                administrador.setEstadoUsuario(5);
                usuarioRepository.save(administrador);
                return "redirect:/superadmin/administradoresSede";
            }else{
                int idsede = Integer.parseInt(idSede);
                administrador.setRol(2);
                administrador.setEstadoUsuario(1);
                usuarioRepository.save(administrador);
                System.out.println("Valor de id administrador: " + administrador.getIdUsuario());
                System.out.println("Valor de id sede: " + idsede);
                System.out.println("Valor de rol: " + administrador.getRol());
                System.out.println("Valor de estado de usuario: " + administrador.getEstadoUsuario());
                attr.addFlashAttribute("datosAdministrador", administrador);
                attr.addFlashAttribute("sedeid", sedeId);
                return "redirect:/superadmin/asignarSedeAdministradorSede?datosAdministrador=" + administrador.getIdUsuario() + "&sedeid=" + idsede;
            }
        }
    }

    @GetMapping("/asignarSedeAdministradorSede")
    public String asignarSedeAdministrador(@ModelAttribute("administrador") @Valid Usuario administrador, BindingResult bindingResult, @RequestParam("datosAdministrador") Integer datosAdministrador, @RequestParam("sedeid") int sedeId, Model model, RedirectAttributes attr) {
        System.out.println("Valor Admin del administrador: " + administrador.getIdUsuario());
        System.out.println("Valor Admin final: " + datosAdministrador);
        System.out.println("Valor Sede final: " + sedeId);
        sedeRepository.asignarAdministradorSede(datosAdministrador, sedeId);
        return "redirect:/superadmin/administradoresSede";
    }



    @GetMapping(value = {"/editarAdministrador"})
    public String editarAdministrador(Usuario administrador,
                                      Model model, @RequestParam("id") String id){
        int idAdmin = Integer.parseInt(id);
        Optional<Usuario> optionalUsuario = usuarioRepository.findById(idAdmin);
        Optional<Sede> optionalAdministradorSede = sedeRepository.buscarAdminID(idAdmin);
        System.out.println("Valor Dentro de editar de optional usuario: " + optionalUsuario.get().getIdUsuario());
        if(optionalUsuario.isPresent()){
            administrador = optionalUsuario.get();
            Sede sedeAdministrador = optionalAdministradorSede.get();
            List<Sede> sedeList = sedeRepository.findAll();
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
    public String actualizarAdministrador(@ModelAttribute("administrador") @Valid Usuario administrador, BindingResult bindingResult, @RequestParam("sedeid") Integer idSede, RedirectAttributes attr, Model model){
        if(bindingResult.hasErrors()){
            List<Sede> sedeDisponibleList = sedeRepository.findAll();
            model.addAttribute("sedeDisponibleList", sedeDisponibleList);
            return "superAdmin/editarAdministrador";
        }else{
            usuarioRepository.save(administrador);
            int idAdministradorNuevo = administrador.getIdUsuario();
            /*Se ubica el administrador de la sede nueva elegida*/
            Sede sedeTieneAdministrador = sedeRepository.getSedeByIdSede(idSede);
            /*Se obtiene el id del administrador de la sede nueva elegida*/

            /*Se obtiene el id de la sede actual del admin*/
            Sede AdministradorTieneSede = sedeRepository.buscarSedeTieneAdministrador(idAdministradorNuevo);
            int idSedeAntigua = AdministradorTieneSede.getIdSede();

            if (sedeTieneAdministrador.getAdmin()==null){
                sedeRepository.eliminarAdminAntiguo(idSedeAntigua);
                sedeRepository.asignarAdministradorSede(idAdministradorNuevo, idSede);
            }else{
                int idAdministradorAntiguo = sedeTieneAdministrador.getAdmin().getIdUsuario();
                usuarioRepository.administradorSinSede(idAdministradorAntiguo);
                sedeRepository.eliminarAdminAntiguo(idSedeAntigua);
                sedeRepository.asignarAdministradorSede(idAdministradorNuevo, idSede);
            }
            return "redirect:/superadmin/administradoresSede";
        }
    }


    @PostMapping("/eliminarAdministrador")
    public String eliminarAdministrador(@RequestParam(value = "idAdministrador") String idAdministrador, @RequestParam(value = "idSede") int idSede){
        int idAdministradorID = Integer.parseInt(idAdministrador);
        sedeRepository.eliminarAdminAntiguo(idSede);
        usuarioRepository.eliminarFarmacista(idAdministradorID);
        return "redirect:/superadmin/administradoresSede";

    }

    @PostMapping("/banearAdministrador")
    public String banearAdministrador(@RequestParam(value = "idAdministrador") String idAdministrador, @RequestParam(value = "idSede") int idSede){
        int idAdministradorID = Integer.parseInt(idAdministrador);
        sedeRepository.eliminarAdminAntiguo(idSede);
        usuarioRepository.banearAdministrador(idAdministradorID);
        return "redirect:/superadmin/administradoresSede";

    }

    @PostMapping("/eliminarAdministradorSinSede")
    public String eliminarAdminSinSede(@RequestParam(value = "idAdministrador") String idAdministrador){
        int idAdministradorID = Integer.parseInt(idAdministrador);
        usuarioRepository.eliminarFarmacista(idAdministradorID);
        return "redirect:/superadmin/administradoresSede";

    }

    @PostMapping("/desbanearAdministrador")
    public String desbanearAdministrador(@RequestParam(value = "idAdministrador") String idAdministrador){
        int idAdministradorID = Integer.parseInt(idAdministrador);
        usuarioRepository.administradorSinSede(idAdministradorID);
        return "redirect:/superadmin/administradoresSede";

    }

    @GetMapping(value = {"/editarAsignarAdministrador"})
    public String asignarNuevaSedeAdministrador(Usuario administrador,
                                      Model model, @RequestParam("id") String id){
        int idAdmin = Integer.parseInt(id);
        Optional<Usuario> optionalUsuario = usuarioRepository.findById(idAdmin);
        System.out.println("Valor Dentro de editar de optional usuario: " + optionalUsuario.get().getIdUsuario());
        if(optionalUsuario.isPresent()){
            administrador = optionalUsuario.get();
            List<Sede> sedeList = sedeRepository.listarSedesDisponibles();
            model.addAttribute("sedeList", sedeList);
            model.addAttribute("administrador", administrador);
            return "superAdmin/asignarAdministrador";
        }else {
            return "redirect:/superadmin/administradoresSede";
        }
    }


    @GetMapping(value = {"/crearDoctor"})
    public String crearDoctor(@ModelAttribute("doctor") @Valid Doctor doctor, BindingResult bindingResul, RedirectAttributes attr, Model model){
        List<Sede> sedeDisponibleList = sedeRepository.findAll();
        model.addAttribute("sedeDisponibleList", sedeDisponibleList);
        return "superAdmin/crearDoctor";
    }

    @PostMapping("/guardarDoctor")
    public String crearNuevoDoctor(@ModelAttribute("doctor") @Valid Doctor doctor, BindingResult bindingResul, RedirectAttributes attr, Model model) {
        if(bindingResul.hasErrors()){
            List<Sede> sedeDisponibleList = sedeRepository.findAll();
            model.addAttribute("sedeDisponibleList", sedeDisponibleList);
            return "superAdmin/crearDoctor";
        }else{
            doctorRepository.save(doctor);
            return "redirect:/superadmin/doctores";
        }
    }

    @GetMapping(value = {"/editarDoctor"})
    public String editarDoctor(@ModelAttribute("doctor") Doctor doctor, Model model, @RequestParam("id") int id){

        Optional<Doctor> optionalDoctor = doctorRepository.findById(id);

        if (optionalDoctor.isPresent()) {
            doctor = optionalDoctor.get();
            List<Sede> sedeDisponibleList = sedeRepository.findAll();
            List<SedeDoctor> sedeDoctorList = sedeDoctorRepository.listarSedesDondeEstaDoctor(id);
            model.addAttribute("sedeDisponibleList", sedeDisponibleList);
            model.addAttribute("doctor", doctor);
            model.addAttribute("doctoresVisiblesSede", sedeDoctorList);
            return "superAdmin/editarDoctor";
        } else {
            return "redirect:/superadmin/doctores";
        }
    }


    @PostMapping("/actualizarDoctor")
    public String actualizarMedicamento(@ModelAttribute("doctor") @Valid Doctor doctor, BindingResult bindingResult, @RequestParam(value = "sedeid", required = false) List<Integer> idSedesSeleccionadas, RedirectAttributes attr, Model model){
        if(bindingResult.hasErrors()){
            List<Sede> sedeDisponibleList = sedeRepository.findAll();
            List<SedeDoctor> sedeDoctorList = sedeDoctorRepository.listarSedesDondeEstaDoctor(doctor.getIdDoctor());
            model.addAttribute("sedeDisponibleList", sedeDisponibleList);
            model.addAttribute("doctoresVisiblesSede", sedeDoctorList);
            return "superAdmin/editarDoctor";
        }else{

            if (idSedesSeleccionadas == null || idSedesSeleccionadas.isEmpty()) {
                System.out.println("Estoy en si el parámetros es NULL");
                doctorRepository.save(doctor);
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
                    return "redirect:/superadmin/doctores";
                }else{
                    sedeDoctorRepository.borrarSedesAnterioresDoctor(idDoctor);
                    for (Integer idSede : idSedesSeleccionadas) {
                        sedeDoctorRepository.asignarDoctorPorSede(idDoctor,idSede);
                    }
                    doctorRepository.save(doctor);
                    return "redirect:/superadmin/doctores";
                }
            }
        }
    }

    @PostMapping("/eliminarDoctor")
    public String eliminarDoctor(@RequestParam(value = "idDoctor") String idDoctor){
        int idDoctorID = Integer.parseInt(idDoctor);
        sedeDoctorRepository.borrarSedesAnterioresDoctor(idDoctorID);
        doctorRepository.eliminarDoctor(idDoctorID);
        return "redirect:/superadmin/doctores";
    }

    @GetMapping(value = {"/editarFarmacista"})
    public String editarFarmacista(@ModelAttribute("farmacista") @Valid Usuario farmacista, BindingResult bindingResult, Model model, @RequestParam("id") int id){

        Optional<Usuario> optionalFarmacista = usuarioRepository.findById(id);

        if (optionalFarmacista.isPresent()) {
            farmacista = optionalFarmacista.get();
            SedeFarmacista datosFarmacistaSede = sedeFarmacistaRepository.buscarFarmacistaSede(id);
            List<EstadoUsuario> estadoUsuarioList = estadoUsuarioRepository.listarEstadosUsuarios();
            model.addAttribute("farmacista", farmacista);
            model.addAttribute("datosFarmacistaSede", datosFarmacistaSede);
            model.addAttribute("estadoUsuarioList", estadoUsuarioList);
            return "superAdmin/editarFarmacista";
        } else {
            return "redirect:/superadmin/farmacistas";
        }
    }

    @PostMapping("/actualizarDatosFarmacista")
    public String actualizarDatosFarmacista(@ModelAttribute("farmacista") @Valid Usuario farmacista, BindingResult bindingResult, RedirectAttributes attr, Model model){

        if(bindingResult.hasErrors()){
            SedeFarmacista datosFarmacistaSede = sedeFarmacistaRepository.buscarFarmacistaSede(farmacista.getIdUsuario());
            List<EstadoUsuario> estadoUsuarioList = estadoUsuarioRepository.listarEstadosUsuarios();
            model.addAttribute("farmacista", farmacista);
            model.addAttribute("datosFarmacistaSede", datosFarmacistaSede);
            model.addAttribute("estadoUsuarioList", estadoUsuarioList);
            return "superAdmin/editarFarmacista";
        }else{

            usuarioRepository.save(farmacista);
            return "redirect:/superadmin/farmacistas";
        }
    }

    @PostMapping("/eliminarFarmacista")
    public String eliminarFarmacista(@RequestParam(value = "idFarmacista") String idFarmacista){
        int idFarmacistaID = Integer.parseInt(idFarmacista);
        sedeFarmacistaRepository.eliminarFarmacistadeSedeFarmacista(idFarmacistaID);
        usuarioRepository.eliminarFarmacista(idFarmacistaID);
        return "redirect:/superadmin/farmacistas";

    }


    @GetMapping(value = {"/crearMedicamento"})
    public String crearMedicamento(@ModelAttribute("medicamento") @Valid Medicamento medicamento, BindingResult bindingResul, Model model){
        List<Sede> sedeDisponibleList = sedeRepository.findAll();
        model.addAttribute("sedeDisponibleList", sedeDisponibleList);
        return "superAdmin/crearMedicamento";
    }


    @PostMapping("/guardarMedicamento")
    public String agregarNuevoMedicamento(@ModelAttribute("medicamento") @Valid Medicamento medicamento, BindingResult bindingResul, RedirectAttributes attr, Model model) {

        if(bindingResul.hasErrors()){
            System.out.println("HAY ERRORES DE VALIDACIÓN:");
            for (ObjectError error : bindingResul.getAllErrors()) {
                System.out.println("- " + error.getDefaultMessage());
            }
            List<Sede> sedeDisponibleList = sedeRepository.findAll();
            model.addAttribute("sedeDisponibleList", sedeDisponibleList);
            return "superAdmin/crearMedicamento";
        }else{
            System.out.println("NO HAY ERROR");
            medicamento.setCategorias("NNUL");
            medicamento.setEstado(1);
            medicamento.setImagen("-");
            medicamentoRepository.save(medicamento);
            return "redirect:/superadmin/medicamentos";
        }
    }

    @GetMapping(value = {"/editarMedicamento"})
    public String editarMedicamento(@ModelAttribute("product") Medicamento medicamento, Model model, @RequestParam("id") int id){

        Optional<Medicamento> optionalMedicamento = medicamentoRepository.findById(id);

        if (optionalMedicamento.isPresent()) {
            medicamento = optionalMedicamento.get();
            List<Sede> sedeDisponibleList = sedeRepository.findAll();
            List<SedeStock> sedeStockList = sedeStockRepository.medicamentoPresenteSedes(id);
            model.addAttribute("sedeDisponibleList", sedeDisponibleList);
            model.addAttribute("medicamento", medicamento);
            model.addAttribute("medicamentosVisiblesSede", sedeStockList);
            return "superAdmin/editarMedicamento";
        } else {
            return "redirect:/superadmin/medicamentos";
        }
    }

    @PostMapping("/actualizarMedicamento")
    public String actualizarMedicamento(@ModelAttribute("medicamento") @Valid Medicamento medicamento, BindingResult bindingResult, @RequestParam(value = "sedeid", required = false) List<Integer> idSedesSeleccionadas, RedirectAttributes attr, Model model){
        if(bindingResult.hasErrors()){
            List<Sede> sedeDisponibleList = sedeRepository.findAll();
            model.addAttribute("sedeDisponibleList", sedeDisponibleList);
            return "superAdmin/editarMedicamento";
        }else{
            medicamento.setCategorias("NNUL");
            medicamento.setEstado(1);
            if (idSedesSeleccionadas == null || idSedesSeleccionadas.isEmpty()) {
                System.out.println("Estoy en si el parámetros es NULL");
                medicamento.setImagen(null);
                medicamentoRepository.save(medicamento);
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
                    return "redirect:/superadmin/medicamentos";
                }else{
                    sedeStockRepository.borrarSedesAnteriores(idMedicamento);
                    for (Integer idSede : idSedesSeleccionadas) {
                        medicamentoRepository.asignarMedicamentoPorSede(idSede,idMedicamento,0);
                    }
                    medicamento.setImagen(null);
                    medicamentoRepository.save(medicamento);
                    return "redirect:/superadmin/medicamentos";
                }
            }
        }
    }

    @PostMapping("/eliminarMedicamento")
    public String eliminarMedicamento(@RequestParam(value = "idMedicamento") String idMedicamento){
        int idMedicamentoID = Integer.parseInt(idMedicamento);
        sedeStockRepository.borrarSedesAnteriores(idMedicamentoID);
        medicamentoRepository.eliminarMedicamento(idMedicamentoID);
        return "redirect:/superadmin/medicamentos";
    }
}
