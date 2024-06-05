package pe.sanmiguel.bienestar.proyecto_gtics.Controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.sanmiguel.bienestar.proyecto_gtics.Dto.MedicamentosSedeStockDto;
import pe.sanmiguel.bienestar.proyecto_gtics.Dto.ReposicionContenidoMedicamentoDto;
import pe.sanmiguel.bienestar.proyecto_gtics.Dto.UsuarioSedeFarmacistaDto;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.*;
import pe.sanmiguel.bienestar.proyecto_gtics.Repository.*;
import pe.sanmiguel.bienestar.proyecto_gtics.SHA256;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/adminsede")
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

    @Autowired
    CodigoColegiaturaRepository codigoColegiaturaRepository;

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
    public String showIndexAdminSede(Model model,
                                     HttpServletRequest request, HttpServletResponse response, Authentication authentication){
        //SESSION
        //Iniciamos la sesión
        HttpSession session = request.getSession();
        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName());
        if (usuario == null) {
            usuario = usuarioRepository.findByCorreo(authentication.getName());
            session.setAttribute("usuario", usuario);
        }

        Integer idSede = (Integer) session.getAttribute("idSede");

        if (idSede == null) {
            Sede sedeSession = sedeRepository.sedeAdminID(usuario.getIdUsuario());

            // Verificar si sedeSession es nulo
            if (sedeSession != null) {
                idSede = sedeSession.getIdSede();
                session.setAttribute("idSede", idSede);
            } else {
                // Manejar el caso cuando sedeSession es nulo
                return "errorPage"; // O alguna página de error apropiada
            }
        }

        Optional<Integer> optFinalNumReposicion = reposicionRepository.findLastNumeroporSede(idSede);

        if(optFinalNumReposicion.isPresent()){
            int finalNumReposicion = optFinalNumReposicion.get();
            int preFinalNumReposicion = finalNumReposicion - 1;

            List<Reposicion> listaReposicionNoEntregadasUltimas = reposicionRepository.listarOrdenesReposicionNoEntregadasUltimas(idSede, finalNumReposicion, preFinalNumReposicion);
            if(listaReposicionNoEntregadasUltimas.size() == 0){
                return "adminsede/inicio";
            }
            model.addAttribute("listaReposicionNoEntregadasUltimas", listaReposicionNoEntregadasUltimas);
            model.addAttribute("lista1", medicamentoRepository.listarMedicamentosStockPorAgotar(idSede));
            model.addAttribute("lista2", medicamentoRepository.listarMedicamentosStockPorAgotar(idSede));
            return "adminsede/inicio";

        }else {
            List<Reposicion> listaReposicionNoEntregadasUltimas = new ArrayList<>(); //incializando en size 0
            model.addAttribute("listaReposicionNoEntregadasUltimas", listaReposicionNoEntregadasUltimas);
            return "adminsede/inicio";

        }
    }

    @GetMapping("/doctores")
    public String showDoctors(Model model,
                              HttpServletRequest request, HttpServletResponse response, Authentication authentication){
        //Iniciamos la sesión
        HttpSession session = request.getSession();
        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuario);

        //Sacamos la sede del adminsede
        Sede sedeSession = sedeRepository.sedeAdminID(usuario.getIdUsuario());

        List<Doctor> listaDoctoresporSede = doctorRepository.listarDoctoresporSede(sedeSession.getIdSede());
        model.addAttribute("listaDoctoresD", listaDoctoresporSede);
        return "adminsede/doctores";
    }

    @GetMapping("/farmacista")
    public String showFarmacistas(Model model,
                                  HttpServletRequest request, HttpServletResponse response, Authentication authentication){
        //SESSION
        //Iniciamos la sesión
        HttpSession session = request.getSession();
        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuario);

        //Sacamos la sede del adminsede
        Sede sedeSession = sedeRepository.sedeAdminID(usuario.getIdUsuario());

        List<UsuarioSedeFarmacistaDto> listaFarmacistasNew = usuarioRepository.listarSedeFarmacista(sedeSession.getIdSede());
        model.addAttribute("listaFarmacistasNew", listaFarmacistasNew);
        return "adminsede/farmacistas";
    }

    @GetMapping("/ordenes")
    public String showOrders(Model model,
                             HttpServletRequest request, HttpServletResponse response, Authentication authentication){
        //SESSION
        //Iniciamos la sesión
        HttpSession session = request.getSession();
        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuario);

        //Sacamos la sede del adminsede
        Sede sedeSession = sedeRepository.sedeAdminID(usuario.getIdUsuario());

        List<Reposicion> listaReposicionNoEntregadas = reposicionRepository.listarOrdenesReposicionNoEntregadas(sedeSession.getIdSede()) ;
        model.addAttribute("listaReposicionNoEntregadas", listaReposicionNoEntregadas);
        return "adminsede/ordenes_reposicion";
    }

    @GetMapping("/editar_farmacista")
    public String editFarmacista(@RequestParam("id") int id,
                                 Model model,
                                 @ModelAttribute("usuario") Usuario usuario){
        usuario = usuarioRepository.encontrarFarmacistaporId(id);
        Optional<SedeFarmacista> optionalSedeFarmacista = sedeFarmacistaRepository.buscarCodigoFarmacista(id);
        if(optionalSedeFarmacista.isPresent()){
            SedeFarmacista sedeFarmacista = optionalSedeFarmacista.get();
            model.addAttribute("usuario", usuario);
            model.addAttribute("sedeFarmacista", sedeFarmacista);
            return "adminsede/editar_farmacista";
        }else {
            return "redirect:/adminsede/farmacista";

        }

    }

    @GetMapping("/editar_orden_reposicion")
    public String editOrden(@RequestParam("id") int id,
                            Model model, HttpServletRequest request, HttpServletResponse response, Authentication authentication,
                            RedirectAttributes attr){

        //SESSION
        //Iniciamos la sesión
        HttpSession session = request.getSession();
        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuario);

        //Sacamos la sede del adminsede
        Sede sedeSession = sedeRepository.sedeAdminID(usuario.getIdUsuario());

        int idSede = sedeSession.getIdSede();

        Optional<Reposicion> optReposicion = reposicionRepository.findById(id);

        if(optReposicion.isPresent()){
            Reposicion reposicionSeleccionada = optReposicion.get();
            // Ahora comparamos Ids
            if(reposicionSeleccionada.getIdSede().getIdSede() == idSede){
                List<ReposicionContenidoMedicamentoDto> listaMedicamentosSeleccionados = reposicionContenidoRepository.listaMostrarMedicamentosSeleccionados(id);
                model.addAttribute("idOrden", id);
                model.addAttribute("listaMedicamentosSeleccionados", listaMedicamentosSeleccionados);
                model.addAttribute("reposicion", reposicionSeleccionada);
                return "adminsede/editar_orden_reposicion";
            }else{
                attr.addFlashAttribute("msgred", "La orden de reposición buscada no ha sido encontrada.");
                return "redirect:/adminsede/ordenes";

            }
        }else {
            attr.addFlashAttribute("msgred", "La orden de reposición buscada no ha sido encontrada.");
            return "redirect:/adminsede/ordenes";
        }

    }

    @GetMapping("/medicamentos")
    public String showMedicamentos(@RequestParam(name = "state", required = false) String state,
                                   Model model,
                                   HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        //SESSION
        //Iniciamos la sesión
        HttpSession session = request.getSession();
        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuario);

        //Sacamos la sede del adminsede
        Sede sedeSession = sedeRepository.sedeAdminID(usuario.getIdUsuario());

        int idSession = sedeSession.getIdSede(); //Sede 1
        model.addAttribute("sedeSession", sedeSession);

        if ("disponible".equals(state)) {
            List<MedicamentosSedeStockDto> listaMedicamentosStockDisponible = medicamentoRepository.listarMedicamentosStockDisponible(idSession);
            model.addAttribute("listaMedicamentosSedeStock", listaMedicamentosStockDisponible);
            //hacer lista de fotos de medicamentos
            ArrayList<String> listaFotosMedicamentos = new ArrayList<>();
            List<Medicamento> listaMedicamento = medicamentoRepository.findAll();

            for(MedicamentosSedeStockDto medicamentosSedeStockDto : listaMedicamentosStockDisponible){
                for(Medicamento medicamento : listaMedicamento){
                    if(medicamento.getIdMedicamento() == medicamentosSedeStockDto.getIdMedicamento()){
                        listaFotosMedicamentos.add(medicamento.getImagenBase64());

                    }

                }

            }
            model.addAttribute("listaFotosMedicamentos", listaFotosMedicamentos);
            return "adminsede/medicamentos_sede";

        } else if ("agotado".equals(state)) {
            List<MedicamentosSedeStockDto> listaMedicamentosStockAgotado = medicamentoRepository.listarMedicamentosStockAgotados(idSession);
            model.addAttribute("listaMedicamentosSedeStock", listaMedicamentosStockAgotado);
            //hacer lista de fotos de medicamentos
            ArrayList<String> listaFotosMedicamentos = new ArrayList<>();
            List<Medicamento> listaMedicamento = medicamentoRepository.findAll();

            for(MedicamentosSedeStockDto medicamentosSedeStockDto : listaMedicamentosStockAgotado){
                for(Medicamento medicamento : listaMedicamento){
                    if(medicamento.getIdMedicamento() == medicamentosSedeStockDto.getIdMedicamento()){
                        listaFotosMedicamentos.add(medicamento.getImagenBase64());

                    }

                }

            }
            model.addAttribute("listaFotosMedicamentos", listaFotosMedicamentos);
            return "adminsede/medicamentos_sede";

        } else if ("poragotar".equals(state)) {
            List<MedicamentosSedeStockDto> listaMedicamentosPorAgotar = medicamentoRepository.listarMedicamentosStockPorAgotar(idSession);
            model.addAttribute("listaMedicamentosSedeStock", listaMedicamentosPorAgotar);
            //hacer lista de fotos de medicamentos
            ArrayList<String> listaFotosMedicamentos = new ArrayList<>();
            List<Medicamento> listaMedicamento = medicamentoRepository.findAll();

            for(MedicamentosSedeStockDto medicamentosSedeStockDto : listaMedicamentosPorAgotar){
                for(Medicamento medicamento : listaMedicamento){
                    if(medicamento.getIdMedicamento() == medicamentosSedeStockDto.getIdMedicamento()){
                        listaFotosMedicamentos.add(medicamento.getImagenBase64());

                    }

                }

            }
            model.addAttribute("listaFotosMedicamentos", listaFotosMedicamentos);
            return "adminsede/medicamentos_sede";

        } else {
            List<MedicamentosSedeStockDto> listaMedicamentosSedeStock = medicamentoRepository.listarMedicamentosSedeStock(idSession); //seteamos por default
            model.addAttribute("listaMedicamentosSedeStock", listaMedicamentosSedeStock);
            //hacer lista de fotos de medicamentos
            ArrayList<String> listaFotosMedicamentos = new ArrayList<>();
            List<Medicamento> listaMedicamento = medicamentoRepository.findAll();

            for(MedicamentosSedeStockDto medicamentosSedeStockDto : listaMedicamentosSedeStock){
                for(Medicamento medicamento : listaMedicamento){
                    if(medicamento.getIdMedicamento() == medicamentosSedeStockDto.getIdMedicamento()){
                        listaFotosMedicamentos.add(medicamento.getImagenBase64());

                    }

                }

            }
            model.addAttribute("listaFotosMedicamentos", listaFotosMedicamentos);

            return "adminsede/medicamentos_sede";
        }

    }

    @GetMapping("/solicitud_farmacista")
    public String solicitudFarmacista(@ModelAttribute("usuarioFarmacista") Usuario usuarioFarmacista, //model attribute del farmacista
                                      Model model){
        return "adminsede/solicitud_agregar_farmacista";
    }

    @GetMapping("/ver_ordenes_entregadas")
    public String verOrdenesEntregadas(Model model,
                                       HttpServletRequest request, HttpServletResponse response, Authentication authentication){
        //SESSION
        //Iniciamos la sesión
        HttpSession session = request.getSession();
        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuario);

        //Sacamos la sede del adminsede
        Sede sedeSession = sedeRepository.sedeAdminID(usuario.getIdUsuario());

        int idSede = sedeSession.getIdSede();
        List<Reposicion> listaOrdenesEntregadas = reposicionRepository.listarOrdenesReposicionEntregadas(idSede);
        model.addAttribute("listaOrdenesEntregadas", listaOrdenesEntregadas);
        return "adminsede/ver_ordenes_entregadas";
    }

    @GetMapping("/cambiar_contrasena")
    public String vistaCambiarContra(){
        return "adminsede/cambiar_contrasena_adminsede";
    }

    private String hashToDots(String passwordHash) {
        return "*************"; // Repite el carácter '*' según la longitud del hash
    }

    @GetMapping("/notificaciones_adminsede")
    public String vistaNotificaciones(Model model,
                                      HttpServletRequest request, HttpServletResponse response, Authentication authentication){
        //SESSION
        //Iniciamos la sesión
        HttpSession session = request.getSession();
        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuario);

        //Sacamos la sede del adminsede
        Sede sedeSession = sedeRepository.sedeAdminID(usuario.getIdUsuario());

        int idSede = sedeSession.getIdSede();
        model.addAttribute("medicamentosSinStock", medicamentoRepository.listarMedicamentosStockPorAgotaroAgotados(idSede));
        return "adminsede/notificaciones_adminsede";
    }

    @GetMapping("/verDetalles") //VER DETALLES DE NUEVA COMPRA
    public String verDetalles(Model model){

        model.addAttribute("sede", sedeVistaReposicion);
        model.addAttribute("administradorMostrar", administradorVistaReposicion);
        model.addAttribute("reposicion", reposicionMostrar);
        model.addAttribute("listaMostrarNuevaCompra", listaMostrarNuevaCompra);
        return "adminsede/verDetalles";
    }

    @GetMapping("/generar_orden_forms")
    public String generarOrden(Model model,
                               HttpServletRequest request, HttpServletResponse response, Authentication authentication){

        //SESSION
        //Iniciamos la sesión
        HttpSession session = request.getSession();
        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuario);

        //Sacamos la sede del adminsede
        Sede sedeSession = sedeRepository.sedeAdminID(usuario.getIdUsuario());


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
        model.addAttribute("usuario",usuario );
        model.addAttribute("sedeSession", sedeSession);
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

    @GetMapping("/eliminar_medicamento_lista_seleccionada")
    public String eliminarMedicamentoLista(Model model,
                                           @RequestParam("idMedicamento") int idMedicamento,
                                           @RequestParam("idReposicion") int idReposicion){

        reposicionContenidoRepository.eliminarMedicamentoContenidoReposicion(idMedicamento,idReposicion);
        model.addAttribute("idOrden", idReposicion);
        List<ReposicionContenidoMedicamentoDto> listaMedicamentosSeleccionados = reposicionContenidoRepository.listaMostrarMedicamentosSeleccionados(idReposicion);
        model.addAttribute("listaMedicamentosSeleccionados", listaMedicamentosSeleccionados);


        // Si la orden de reposicion ya no tiene medicamentos entonces se elimina la orden:
        if(listaMedicamentosSeleccionados.size() == 0){
            reposicionRepository.eliminarReposicionporId(idReposicion);
        }

        Reposicion reposicionSeleccionada = reposicionRepository.encontrarReposicionporId(idReposicion);

        model.addAttribute("reposicion", reposicionSeleccionada);

        return "adminsede/editar_orden_reposicion";
    }


    /*---------------------------------------------------------POST---------------------------------------------------------*/

    @PostMapping("/editarFarmacista")
    public String editarFarmacista(RedirectAttributes attr,
                                   SedeFarmacista sedeFarmacista,
                                   @ModelAttribute("usuario") @Valid Usuario usuario, BindingResult bindingResult,
                                   Model model, @RequestParam("correo") String correo){

        List<String> correosUsados = usuarioRepository.listarCorreosUsadosMenosUserID(usuario.getIdUsuario());
        if (correosUsados.contains(correo)) {
            SedeFarmacista datosFarmacistaSede = sedeFarmacistaRepository.buscarFarmacistaSede(usuario.getIdUsuario());
            String passwordHash = usuario.getContrasena(); // Obtener el hash de la contraseña desde la base de datos
            //String passwordDots = hashToDots(passwordHash);
            model.addAttribute("usuario", usuario);
            model.addAttribute("datosFarmacistaSede", datosFarmacistaSede);
            //model.addAttribute("contrasenia", passwordDots);
            bindingResult.rejectValue("correo", "error.correo", "El correo ya se encuentra registrado.");
            return "adminsede/editar_farmacista";
        }



        if(!bindingResult.hasErrors()){
            usuarioRepository.save(usuario);
            Optional<SedeFarmacista> optionalSedeFarmacista = sedeFarmacistaRepository.buscarCodigoFarmacista(usuario.getIdUsuario());
            if(optionalSedeFarmacista.isPresent()){
                SedeFarmacista sedeFarmacistaOld = optionalSedeFarmacista.get();
                sedeFarmacista.setId(sedeFarmacistaOld.getId());
                sedeFarmacista.setIdFarmacista(sedeFarmacistaOld.getIdFarmacista());
                sedeFarmacista.setAprobado(sedeFarmacistaOld.getAprobado());
                //sedeFarmacistaRepository.save(sedeFarmacista);
                attr.addFlashAttribute("msg", "Farmacista: " + usuario.getNombres() + " " + usuario.getApellidos() + " actualizado correctamente");
                return "redirect:/adminsede/farmacista";
            }else {
                return "redirect:/adminsede/farmacista";
            }
        }else { //Si hay 1 o más errores:
            model.addAttribute("id", usuario.getIdUsuario());
            return "adminsede/editar_farmacista";
        }


    }
    @PostMapping("/solicitud_farmacista_post")
    public String solicitudAgregarFarmacista(@ModelAttribute("usuarioFarmacista") @Valid Usuario usuarioFarmacista, BindingResult bindingResult,
                                             @RequestParam("codigoMed") String codigoMed,
                                             RedirectAttributes attr){

        if(!bindingResult.hasErrors()){
            int estadoUsuario = 2;
            int idRol = 3;
            int idUsuario = usuarioRepository.findLastUsuarioId() + 1;
            int aprobado = 2; //El farmacista no está aprobado
            int idSede = 1; //Cambiar
            usuarioFarmacista.setIdUsuario(idUsuario);
            usuarioFarmacista.setRol(idRol);
            usuarioFarmacista.setEstadoUsuario(estadoUsuario);
            //usuarioRepository.crearFarmacista(idUsuario, idRol, correo, contrasena, nombres, apellidos, celular, dni, direccion, distrito, seguro, estadoUsuario);

            //Validacion de codigo de colegiatura
            int codigoValido = 0;
            List<CodigoColegiatura> listaCodigosColegiatura = codigoColegiaturaRepository.findAll(); //lista todos los codigos de colegiatura

            for (CodigoColegiatura codigoColegiatura : listaCodigosColegiatura){

                if(codigoMed.equals(codigoColegiatura.getCodigo()) && usuarioFarmacista.getDni().equals(codigoColegiatura.getDni())){
                    codigoValido = 1;
                }

            }
            // validamos que no sea repetido el codigo medico
            boolean codigoMedicoUnico = true;

            List<SedeFarmacista> listasedeFarmacistas = sedeFarmacistaRepository.findAll();

            for (SedeFarmacista sedeFarmacista : listasedeFarmacistas){
                if(codigoMed.equals(sedeFarmacista.getCodigoMed())){
                    //no se crea el farmacista debido a que es repetido el codigo medico
                    attr.addFlashAttribute("msgred", "Codigo de colegiatura ya existente en el sistema, por favor ingrese uno nuevamente");
                    codigoMedicoUnico  = false;
                    return "redirect:/adminsede/solicitud_farmacista";

                }

            }

            //validamos que no sea un dni existente
            boolean dniNoExistente = true;

            List<Usuario> listaUsuarios = usuarioRepository.findAll();

            for (Usuario usuario : listaUsuarios){
                if(usuarioFarmacista.getDni().equals(usuario.getDni())){
                    //no se crea el farmacista debido a que es repetido el codigo medico
                    attr.addFlashAttribute("msgred", "DNI ya existente en el sistema, por favor ingrese uno nuevamente");
                    dniNoExistente  = false;
                    return "redirect:/adminsede/solicitud_farmacista";

                }

            }

            //validamos correo
            for (Usuario usuario : listaUsuarios){
                if(usuarioFarmacista.getCorreo().equals(usuario.getCorreo())){
                    //no se crea el farmacista debido a que es repetido el codigo medico
                    attr.addFlashAttribute("msgred", "Correo ya existente en el sistema, por favor ingrese uno nuevamente");
                    dniNoExistente  = false;
                    return "redirect:/adminsede/solicitud_farmacista";

                }

            }



            //Pasados los filtros:

            if (codigoValido == 1 && codigoMedicoUnico && dniNoExistente){
                usuarioRepository.crearFarmacistaSinAprobar(idUsuario, idRol, usuarioFarmacista.getCorreo(), SHA256.cipherPassword(usuarioFarmacista.getContrasena()), usuarioFarmacista.getNombres(), usuarioFarmacista.getApellidos(), usuarioFarmacista.getCelular(), usuarioFarmacista.getDni(), usuarioFarmacista.getDireccion(), usuarioFarmacista.getDistrito(), usuarioFarmacista.getSeguro(), estadoUsuario);
                sedeFarmacistaRepository.crearSedeFarmacista(idSede, idUsuario, codigoMed, aprobado);
                attr.addFlashAttribute("msg", "Solicitud de farmacista " + usuarioFarmacista.getNombres() + " " + usuarioFarmacista.getApellidos() + " enviada correctamente");
                return "redirect:/adminsede/farmacista";
            }else {
                //no se crea el farmacista debido a que el dni o el codigo no coinciden
                attr.addFlashAttribute("msgred", "Codigo de colegiatura no válido, por favor ingrese nuevamente");
                return "redirect:/adminsede/solicitud_farmacista";
            }


        }else { //Existen al menos un error y vamos de frente a la vista
            System.out.println(bindingResult.getAllErrors());
            return "adminsede/solicitud_agregar_farmacista";


        }



    }

    @PostMapping("/generar_orden")
    public String fillContentOrder(@RequestParam("listaIds") List<String> listaSelectedIds,
                                   HttpServletRequest request, HttpServletResponse response, Authentication authentication,
                                   RedirectAttributes attr){

        //SESSION
        //Iniciamos la sesión
        HttpSession session = request.getSession();
        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuario);

        //Sacamos la sede del adminsede
        Sede sedeSession = sedeRepository.sedeAdminID(usuario.getIdUsuario());



        if (!listaSelectedIds.isEmpty()){
            medicamentosSeleccionados = getMedicamentosFromLista(listaSelectedIds);
            listaCantidades = getCantidadesFromLista(listaSelectedIds);

            // Solo podemos reponer los medicamentos que se encuentren en sede
            List<Integer> listaIdsMedicamentoEnSede = sedeStockRepository.listarIdsMedicamentoporSede(sedeSession.getIdSede());

            //Extraemos los ids de los medicamentos solicitados
            List<Integer> listaIdsMedicamentosSolicitados = new ArrayList<>();
            for(int i = 0; i < (listaSelectedIds.size()/2); i++){
                listaIdsMedicamentosSolicitados.add(Integer.parseInt(listaSelectedIds.get(2*i)));
            }

            //Verificamos
            for (int idmedicamento : listaIdsMedicamentosSolicitados){

                if(!listaIdsMedicamentoEnSede.contains(idmedicamento)){
                    attr.addFlashAttribute("msgred", "Orden no realizada debido a que el medicamento seleccionado no se encuentra disponible.");
                    return "redirect:/adminsede/medicamentos";
                }
            }

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
                                    @RequestParam("listaIds") List<String> listaIds,
                                    RedirectAttributes attr,
                                    HttpServletRequest request, HttpServletResponse response, Authentication authentication){

        //SESSION
        //Iniciamos la sesión
        HttpSession session = request.getSession();
        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuario);

        //Sacamos la sede del adminsede
        Sede sedeSession = sedeRepository.sedeAdminID(usuario.getIdUsuario());


        // Crear orden de reposición

        String tracking = "RECIBIDO"; //Por Default
        Integer idEstado = 1; //Por Default
        Integer idSede = sedeSession.getIdSede(); // CORREGIR CON SESSION--------------------------------------------------------

        Optional<Integer> optIntegerLastId = reposicionRepository.findLastReposicionId();
        Integer integerLastId = 0;

        if(optIntegerLastId.isPresent()){
            integerLastId = optIntegerLastId.get();
        }

        Integer idReposicion =  integerLastId + 1;
        //Agregamos numero de orden
        Optional<Integer> optlastNumber = reposicionRepository.findLastNumeroporSede(idSede);

        int lastNumber = 0;

        if (optlastNumber.isPresent()){
            lastNumber = optlastNumber.get();
        }

        Integer newNumber = lastNumber + 1;


        reposicionRepository.crearOrdenReposicion(idReposicion, tracking, priceTotal, idEstado, idSede,newNumber);

        List<Medicamento> listaMedicamentosReposicion = new ArrayList<>();

        // Guardar lista de medicamentos
        int m = 0;
        for (int i = 1; i <= (listaIds.size()/2); i++){ //ListaIds tiene [id,cantidad,id2,cantidad2,...]

            Integer idMedicamento = Integer.parseInt(listaIds.get(m));
            Integer cantidadMedicamento = Integer.parseInt(listaIds.get(m+1));
            reposicionContenidoRepository.guardarContenidoReposicion(idMedicamento, idReposicion, cantidadMedicamento);
            m = m + 2;
        }

        sedeVistaReposicion.setNombre(sedeSession.getNombre());
        sedeVistaReposicion.setDireccion(sedeSession.getDireccion());
        administradorVistaReposicion.setDni(usuario.getDni());
        administradorVistaReposicion.setCorreo(usuario.getCorreo());
        administradorVistaReposicion.setNombres(usuario.getNombres());
        administradorVistaReposicion.setApellidos(usuario.getApellidos());
        reposicionMostrar = reposicionRepository.encontrarReposicionporId(idReposicion);
        listaMostrarNuevaCompra = reposicionContenidoRepository.listaMostrarDetalleNuevaCompra(idReposicion); //lista a mostrar
        attr.addFlashAttribute("msg", "Orden de reposición generada correctamente");

        return "redirect:/adminsede/verDetalles";
    }

    @PostMapping("/verDetallesOrdenEntregado")
    public String verDetallesOrdenEntregado(@RequestParam("idOrden") int idOrden){

        listaMostrarNuevaCompra = reposicionContenidoRepository.listaMostrarDetalleNuevaCompra(idOrden);
        reposicionMostrar = reposicionRepository.encontrarReposicionporId(idOrden);

        return "redirect:/adminsede/verDetallesOrdenEntregada";
    }
    @PostMapping("/eliminar_farmacista")
    public String eliminarFarmacista(@RequestParam(value = "idFarmacista") String idFarmacista, RedirectAttributes attr){
        int idFarmacistaID = Integer.parseInt(idFarmacista);

        sedeFarmacistaRepository.eliminarFarmacistadeSedeFarmacista(idFarmacistaID);
        usuarioRepository.eliminarFarmacista(idFarmacistaID);
        attr.addFlashAttribute("msg", "Farmacista eliminado exitosamente");
        return "redirect:/adminsede/farmacista";
    }


    @GetMapping("/verDetallesOrdenEntregada")
    public String verDetallesOrdenEntregado(Model model,
                                            HttpServletRequest request, HttpServletResponse response, Authentication authentication){

        //SESSION
        //Iniciamos la sesión
        HttpSession session = request.getSession();
        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuario);

        //Sacamos la sede del adminsede
        Sede sedeSession = sedeRepository.sedeAdminID(usuario.getIdUsuario());

        //Datos que se corregirán con sesion para la Sede --------------------------------------------
        sedeVistaReposicion.setNombre(sedeSession.getNombre());
        sedeVistaReposicion.setDireccion(sedeSession.getDireccion());

        //Datos que se corregirán con sesion para el Administrador --------------------------------------------
        administradorVistaReposicion.setDni(usuario.getDni());
        administradorVistaReposicion.setCorreo(usuario.getCorreo());
        administradorVistaReposicion.setNombres(usuario.getNombres());
        administradorVistaReposicion.setApellidos(usuario.getApellidos());

        model.addAttribute("sede", sedeVistaReposicion);
        model.addAttribute("administradorMostrar", administradorVistaReposicion);
        model.addAttribute("reposicion", reposicionMostrar);
        model.addAttribute("listaMostrarNuevaCompra", listaMostrarNuevaCompra);
        return "adminsede/verDetallesOrdenEntregada";
    }

    @PostMapping("/eliminar_orden_reposicion")
    public String eliminar_orden_reposicion(@RequestParam("getIdReposicion") int idReposicion, RedirectAttributes attr){

        reposicionContenidoRepository.eliminarContenidoReposicion(idReposicion);
        reposicionRepository.eliminarReposicionporId(idReposicion);
        attr.addFlashAttribute("msg", "La orden de reposición fue eliminada correctamente");
        return "redirect:/adminsede/ordenes";
    }

    @PostMapping("/editarReposicion")
    public String editarReposicion(@RequestParam(name = "listaIds", required = false) List<Integer> listaIds,
                                   @RequestParam(name = "listaCantidades", required = false) List<Integer> listaCantidades,
                                   @RequestParam("idReposicion") int idReposicion,
                                   RedirectAttributes attr, Model model,
                                   HttpServletRequest request, HttpServletResponse response, Authentication authentication){

        //SESSION
        //Iniciamos la sesión
        HttpSession session = request.getSession();
        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName());
        session.setAttribute("usuario", usuario);

        //Sacamos la sede del adminsede
        Sede sedeSession = sedeRepository.sedeAdminID(usuario.getIdUsuario());

        Reposicion reposicion = reposicionRepository.encontrarReposicionporId(idReposicion);

        if(listaIds == null){
            return "redirect:/adminsede/ordenes";
        }else {
            // validamos si las cantidades son negativas o positivas
            boolean cantidadesPositivas = true;
            for (Integer cantidad : listaCantidades){
                if(cantidad <= 0){
                    cantidadesPositivas = false;
                }
            }

            if(cantidadesPositivas){
                for(int i = 0; i < listaIds.size(); i++){
                    reposicionContenidoRepository.actualizarCantidadMedicamentoOrden(listaCantidades.get(i),listaIds.get(i),idReposicion);
                }
                attr.addFlashAttribute("msg", "Orden de reposición #" + reposicion.getNumero() + " actualizada correctamente");
                return "redirect:/adminsede/ordenes";

            }else {
                attr.addFlashAttribute("msgred", "Error en la actualización de cantidad de medicamento en la orden #" + reposicion.getNumero() + ", solo está permitido colocar cantidades positivas");
                return "redirect:/adminsede/ordenes";
            }

        }
    }

    @GetMapping("/perfil")
    public String vistaPerfil(Model model){
        int idSession = 102;
        Optional<Usuario> adminsessionOpt = usuarioRepository.findById(idSession);
        Usuario adminSession = new Usuario();

        if (adminsessionOpt.isPresent()){
            adminSession = adminsessionOpt.get();
        }

        String passwordHash = adminSession.getContrasena(); // Obtener el hash de la contraseña desde la base de datos
        String passwordDots = hashToDots(passwordHash);

        model.addAttribute("admin", adminSession);
        model.addAttribute("contrasena", passwordDots);

        return "perfil";
    }

    @PostMapping("/actualizar_contrasena")
    public String actualizarContrasena(Usuario adminsede, BindingResult bindingResult,
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
        return "redirect:/adminsede/perfil";
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








}
