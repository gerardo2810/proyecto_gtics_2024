package pe.sanmiguel.bienestar.proyecto_gtics.Controller;

import jakarta.validation.Valid;
import lombok.Getter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pe.sanmiguel.bienestar.proyecto_gtics.CurrentTimeSQL;
import pe.sanmiguel.bienestar.proyecto_gtics.Dto.MedicamentosSedeStockDto;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.*;
import pe.sanmiguel.bienestar.proyecto_gtics.Repository.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class FarmacistaController {


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

    public FarmacistaController(UsuarioRepository usuarioRepository, SedeRepository sedeRepository, SedeStockRepository sedeStockRepository, MedicamentoRepository medicamentoRepository, OrdenRepository ordenRepository, OrdenContenidoRepository ordenContenidoRepository, ReposicionRepository reposicionRepository, EstadoPreOrdenRepository estadoPreOrdenRepository, DoctorRepository doctorRepository) {
        this.usuarioRepository = usuarioRepository;
        this.sedeRepository = sedeRepository;
        this.sedeStockRepository = sedeStockRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.ordenRepository = ordenRepository;
        this.ordenContenidoRepository = ordenContenidoRepository;
        this.reposicionRepository  =reposicionRepository;
        this.estadoPreOrdenRepository = estadoPreOrdenRepository;
        this.doctorRepository = doctorRepository;
    }

    /* Repositorios */


    /* Variables Internas */

    Sede sedeSession;
    List<Medicamento> medicamentosSeleccionados = new ArrayList<>();
    List<String> listaCantidades = new ArrayList<>();

    List<Medicamento> medicamentosSinStock = new ArrayList<>();
    List<Medicamento> medicamentosConStock = new ArrayList<>();
    List<String> cantidadesFaltantes = new ArrayList<>();
    List<String> cantidadesExistentes = new ArrayList<>();

    Usuario pacienteOnStore = new Usuario();

    Integer idVerOrdenCreada;

    /* Variables Internas */



    @GetMapping(value = {"/farmacista", "/farmacista/"})
    public String farmacistaInicio(Model model) {
        List<Medicamento> listaMedicamentos = medicamentoRepository.findAll();
        sedeSession = sedeRepository.getSedeByIdSede(1);
        int numeroOrdenesPendientes = 0;
        model.addAttribute("sedeSession", sedeSession);
        model.addAttribute("listaMedicamentos", listaMedicamentos);
        model.addAttribute("numeroOrdenesPendientes", numeroOrdenesPendientes);
        return "farmacista/inicio";
    }
    @GetMapping("/farmacista/ver_detalles")
    public String farmacistaDetalles(Model model) {
        List<Medicamento> listaMedicamentos = medicamentoRepository.findAll();
        model.addAttribute("sedeSession", sedeSession);
        model.addAttribute("listaMedicamentos", listaMedicamentos);
        return "farmacista/ver_detalles";
    }

    @PostMapping("/farmacista/continuar_compra")
    public String fillContentOrder(@RequestParam("listaIds") List<String> listaSelectedIds){
        if (!listaSelectedIds.isEmpty()){
            medicamentosSeleccionados = getMedicamentosFromLista(listaSelectedIds);
            listaCantidades = getCantidadesFromLista(listaSelectedIds);
            return "redirect:/farmacista/formulario_paciente";
        } else {
            return "redirect:/farmacista";
        }
    }

    @GetMapping("/farmacista/formulario_paciente")
    public String formPacienteData(@ModelAttribute("usuario") Usuario usuario, Model model) {

        if(medicamentosSeleccionados.isEmpty()){
            return "redirect:/farmacista";
        } else {
            List<Integer> stockSeleccionados = new ArrayList<>();

            for (Medicamento med : medicamentosSeleccionados) {
                if (sedeStockRepository.getSedeStockByIdSedeAndIdMedicamento(sedeSession, med).isPresent()) {
                    stockSeleccionados.add(sedeStockRepository.getSedeStockByIdMedicamentoAndIdSede(med,sedeSession).getCantidad());
                } else {
                    stockSeleccionados.add(0);
                }
            }

            ArrayList<Usuario> listaUsuarios = (ArrayList<Usuario>) usuarioRepository.listarUsuariosSegunRol(4);
            ArrayList<Doctor> listaDoctores = (ArrayList<Doctor>) doctorRepository.findAll();

            model.addAttribute("listaUsuarios", listaUsuarios);
            model.addAttribute("listaDoctores", listaDoctores);
            model.addAttribute("stockSeleccionados", stockSeleccionados);
            model.addAttribute("medicamentosSeleccionados", medicamentosSeleccionados);
            model.addAttribute("listaCantidades", listaCantidades);
            return "farmacista/formulario_paciente";
        }
    }

    @PostMapping("/farmacista/finalizar_compra")
    public String createOrdenVenta(@ModelAttribute("usuario") @Valid Usuario usuario,
                                   BindingResult bindingResult,
                                   Model model,
                                   @RequestParam(value = "nombres") String name,
                                   @RequestParam(value = "apellidos") String lastname,
                                   @RequestParam(value = "dni") String dni,
                                   @RequestParam(value = "distrito") String distrito,
                                   @RequestParam(value = "direccion") String direccion,
                                   @RequestParam(value = "doctor", required = false) String doctor,
                                   @RequestParam(value = "seguro") String seguro,
                                   @RequestParam(value = "correo") String correo,
                                   @RequestParam(value = "celular") String celular,
                                   @RequestParam(value = "listaIds") List<String> listaSelectedIds,
                                   @RequestParam(value = "priceTotal") String priceTotal) {

        if (bindingResult.hasErrors()){

            System.out.println(bindingResult.getAllErrors());

            List<Integer> stockSeleccionados = new ArrayList<>();

            for (Medicamento med : medicamentosSeleccionados) {
                if (sedeStockRepository.getSedeStockByIdSedeAndIdMedicamento(sedeSession, med).isPresent()) {
                    stockSeleccionados.add(sedeStockRepository.getSedeStockByIdMedicamentoAndIdSede(med,sedeSession).getCantidad());
                } else {
                    stockSeleccionados.add(0);
                }
            }

            ArrayList<Usuario> listaUsuarios = (ArrayList<Usuario>) usuarioRepository.listarUsuariosSegunRol(4);
            ArrayList<Doctor> listaDoctores = (ArrayList<Doctor>) doctorRepository.findAll();

            model.addAttribute("listaUsuarios", listaUsuarios);
            model.addAttribute("listaDoctores", listaDoctores);
            model.addAttribute("stockSeleccionados", stockSeleccionados);
            model.addAttribute("medicamentosSeleccionados", medicamentosSeleccionados);
            model.addAttribute("listaCantidades", listaCantidades);

            return "farmacista/formulario_paciente";

        } else {

            idVerOrdenCreada = 0;

            medicamentosSeleccionados = getMedicamentosFromLista(listaSelectedIds);
            listaCantidades = getCantidadesFromLista(listaSelectedIds);

            verificationStock verificationStock = new verificationStock(medicamentosSeleccionados, listaCantidades);
            verificationUser verificationUser = new verificationUser(name,lastname,dni,distrito,direccion,seguro,correo,celular);

            this.pacienteOnStore = verificationUser.getUser();

            if (verificationStock.getMedicamentosSinStock().isEmpty()) {

                LocalDateTime now = LocalDateTime.now();

                Orden newOrden = new Orden();
                newOrden.setFechaIni(now);
                newOrden.setPrecioTotal(Float.parseFloat(priceTotal));
                //Id conocido porque no hay session
                newOrden.setIdFarmacista(7);
                newOrden.setTipoOrden(1);
                newOrden.setEstadoOrden(8);
                newOrden.setSede(sedeSession);


                if (!(doctor == null)) {
                    if (!doctor.isEmpty() && !doctor.equals("no-doctor")) {
                        newOrden.setDoctor(doctorRepository.getByIdDoctor(Integer.valueOf(doctor)));
                    }
                }

                newOrden.setPaciente(this.pacienteOnStore);

                ordenRepository.save(newOrden);

                int i = 0;
                for (Medicamento med : medicamentosSeleccionados){

                    OrdenContenidoId contenidoId = new OrdenContenidoId();
                    contenidoId.setIdOrden(newOrden.getIdOrden());
                    contenidoId.setIdMedicamento(med.getIdMedicamento());

                    OrdenContenido contenido = new OrdenContenido();
                    contenido.setId(contenidoId);
                    contenido.setIdOrden(newOrden);
                    contenido.setIdMedicamento(med);
                    contenido.setCantidad(Integer.parseInt(listaCantidades.get(i)));

                    ordenContenidoRepository.save(contenido);
                    i++;
                }

                idVerOrdenCreada = newOrden.getIdOrden();
                return "redirect:/farmacista/ver_boleta?id=" + idVerOrdenCreada;

            } else {

                this.medicamentosSinStock = verificationStock.getMedicamentosSinStock();
                this.medicamentosConStock = verificationStock.getMedicamentosConStock();
                this.cantidadesFaltantes = verificationStock.getCantidadesFaltantes();
                this.cantidadesExistentes = verificationStock.getCantidadesExistentes();

                return "redirect:/farmacista/crear_preorden";
            }
        }
    }



    @PostMapping("/farmacista/finalizar_compra_preorden")
    public String createOrdenVenta() {

        idVerOrdenCreada = 6; // Ejemplo fijo, falta realizar comprobaciones y llenado a base de datos para hacerlo dinámico

        return "redirect:/farmacista/ver_pre_orden";

    }




    @GetMapping("/farmacista/crear_preorden")
    public String createPreOrden(Model model) {

        model.addAttribute("medicamentosSinStock", medicamentosSinStock);
        model.addAttribute("medicamentosConStock", medicamentosConStock);
        model.addAttribute("cantidadesFaltantes", cantidadesFaltantes);
        model.addAttribute("cantidadesExistentes", cantidadesExistentes);

        return "farmacista/crear_preorden";
    }

    @GetMapping("/farmacista/cambiar_medicamentos")
    public String changeMedicamentos(Model model) {

        model.addAttribute("medicamentosSinStock", medicamentosSinStock);
        model.addAttribute("medicamentosConStock", medicamentosConStock);
        model.addAttribute("cantidadesFaltantes", cantidadesFaltantes);
        model.addAttribute("cantidadesExistentes", cantidadesExistentes);

        return "farmacista/cambiar_medicamentos";
    }


    @GetMapping("/farmacista/ordenes_venta")
    public String tablaOrdenesVenta(Model model) {
        List<Orden> listaOrdenesVenta = ordenRepository.findAllOrdenes();
        model.addAttribute("listaOrdenesVenta", listaOrdenesVenta);
        return "farmacista/ordenes_venta";
    }
    @GetMapping("/farmacista/ordenes_web")
    public String tablaOrdenesWeb(Model model) {
        List<Orden> listaOrdenesWeb = ordenRepository.findAllOrdenesWeb();
        model.addAttribute("listaOrdenesWeb", listaOrdenesWeb);
        return "farmacista/ordenes_web";
    }
    @GetMapping("/farmacista/pre_ordenes")
    public String tablaPreOrdenes(Model model) {
        List<Orden> listaPreOrdenes = ordenRepository.findAllPreOrdenes();
        model.addAttribute("listaPreOrdenes", listaPreOrdenes);
        return "farmacista/pre_ordenes";
    }


    @GetMapping("farmacista/aprobar_orden_web")
    public String aprobarOrdenWeb(Model model, @RequestParam("id") String idOrden){

        Optional<Orden> ordenWebOptional = ordenRepository.findById(Integer.valueOf(idOrden));
        List<OrdenContenido> contenidoOrdenWeb = ordenContenidoRepository.findMedicamentosByOrdenId(idOrden);

        if (ordenWebOptional.isPresent()){
            Orden ordenWebComprobada = ordenWebOptional.get();

            ordenWebComprobada.setEstadoOrden(2);

            ordenRepository.save(ordenWebComprobada);

            model.addAttribute("idOrden", idOrden);
            model.addAttribute("contenidoOrden", contenidoOrdenWeb);
            model.addAttribute("orden",ordenWebComprobada);
            return "farmacista/tracking";
        } else {
            return "farmacista/errorPages/no_existe_orden";
        }
    }

    @GetMapping("farmacista/ver_orden_tracking")
    public String verOrdenTracking(Model model, @RequestParam("id") String idOrden){

        Optional<Orden> ordenOptional = ordenRepository.findById(Integer.valueOf(idOrden));
        List<OrdenContenido> contenidoOrden = ordenContenidoRepository.findMedicamentosByOrdenId(idOrden);

        boolean containsPreOrden = false;
        Orden preOrdenChild = ordenRepository.findPreordenByOrdenId(Integer.valueOf(idOrden));

        if (ordenOptional.isPresent()) {
            Orden ordenWebComprobada = ordenOptional.get();
            ArrayList<OrdenContenido> contenidoPreOrden = new ArrayList<>();

            if (preOrdenChild != null) {
                containsPreOrden = true;
                contenidoPreOrden = (ArrayList<OrdenContenido>) ordenContenidoRepository.findMedicamentosByOrdenId(String.valueOf(preOrdenChild.getIdOrden()));
            }

            model.addAttribute("containsPreOrden", containsPreOrden);

            model.addAttribute("preOrden", preOrdenChild);
            model.addAttribute("contenidoPreOrden", contenidoPreOrden);

            model.addAttribute("idOrden", idOrden);
            model.addAttribute("contenidoOrden", contenidoOrden);
            model.addAttribute("orden", ordenWebComprobada);
            return "farmacista/tracking";
        } else {
            return "farmacista/errorPages/no_existe_orden";
        }
    }

    @GetMapping("/farmacista/ver_boleta")
    public String verBoleta(Model model, @RequestParam("id") String idOrden) {

        Optional<Orden> ordenOptional = ordenRepository.findById(Integer.valueOf(idOrden));
        List<OrdenContenido> contenidoOrden = ordenContenidoRepository.findMedicamentosByOrdenId(idOrden);

        if (ordenOptional.isPresent()){
            Orden ordenComprobada = ordenOptional.get();

            model.addAttribute("orden",ordenComprobada);
            model.addAttribute("contenidoOrden", contenidoOrden);
            return "farmacista/boleta";
        } else {
            return "farmacista/errorPages/no_existe_orden";
        }
    }

    @GetMapping("/farmacista/perfil")
    public String profile() {
        return "farmacista/perfil";
    }

    @GetMapping("/farmacista/facturacion")
    public String facturacion(Model model) {
        List<MedicamentosSedeStockDto> medicamentosLowStock = medicamentoRepository.findMedicamentosLowStock(1);
        model.addAttribute("medicamentosLowStock", medicamentosLowStock);
        return "farmacista/facturacion";
    }

    @GetMapping("/farmacista/cambioContraseña")
    public String cambioContra() {
        return "farmacista/cambioContraseña";
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

    public List<String> getCantidadesFromLista(List<String> listaSelectedIds) {
        List<String> cantidades = new ArrayList<>();
        for (int i = 0; i + 1 < listaSelectedIds.size(); i += 2) {
            cantidades.add(listaSelectedIds.get(i + 1));
        }
        return cantidades;
    }



    @Getter
    public class verificationUser {

        private final boolean userExist;
        private final Usuario user;

        public verificationUser(String name, String lastname, String dni, String distrito, String direccion, String seguro, String correo, String celular) {

            boolean userExist = false;
            Usuario user = new Usuario();

            Optional<Usuario> userOptional = usuarioRepository.findPacienteByCorreoAndDni(correo, dni);

            if (userOptional.isPresent()) {
                userExist = true;
                user = userOptional.get();

            } else {
                Usuario newUser = new Usuario();
                newUser.setRol(4);
                newUser.setCorreo(correo);
                newUser.setContrasena("00000000");
                newUser.setNombres(name);
                newUser.setApellidos(lastname);
                newUser.setCelular(celular);
                newUser.setDni(dni);
                newUser.setDireccion(direccion);
                newUser.setDistrito(distrito);
                newUser.setSeguro(seguro);

                newUser.setEstadoUsuario(2);

                user = newUser;
            }

            usuarioRepository.save(user);

            this.userExist = userExist;
            this.user = user;
        }
    }

    @Getter
    public class verificationStock{
        private final List<Medicamento> medicamentosSinStock;
        private final List<Medicamento> medicamentosConStock;
        private final List<String> cantidadesFaltantes;
        private final List<String> cantidadesExistentes;

        public verificationStock(List<Medicamento> medicamentosSeleccionados, List<String> listaCantidades) {
            List<Medicamento> medicamentosSinStock = new ArrayList<>();
            List<Medicamento> medicamentosConStock = new ArrayList<>();
            List<String> cantidadesFaltantes = new ArrayList<>();
            List<String> cantidadesExistentes = new ArrayList<>();

            int i = 0;

            /* Usaremos la Sede 1 porque aún no contamos con Session*/
            SedeStockId sedeStockId = new SedeStockId();
            sedeStockId.setIdSede(1);

            for (Medicamento med : medicamentosSeleccionados) {

                sedeStockId.setIdMedicamento(med.getIdMedicamento());
                Optional<SedeStock> sedeStockOptional = sedeStockRepository.findById(sedeStockId);

                if (sedeStockOptional.isPresent()) {
                    SedeStock sedeStock = sedeStockOptional.get();

                    Medicamento medicamentoToVerify = sedeStock.getIdMedicamento();

                    if(Integer.parseInt(listaCantidades.get(i)) > sedeStock.getCantidad()){
                        medicamentosSinStock.add(medicamentoToVerify);
                        cantidadesFaltantes.add(String.valueOf(Integer.parseInt(listaCantidades.get(i)) - sedeStock.getCantidad()));

                        medicamentosConStock.add(medicamentoToVerify);
                        cantidadesExistentes.add(String.valueOf(sedeStock.getCantidad()));

                    } else {
                        medicamentosConStock.add(medicamentoToVerify);
                        cantidadesExistentes.add(listaCantidades.get(i));
                    }

                } else {
                    medicamentosSinStock.add(med);
                    cantidadesFaltantes.add(listaCantidades.get(i));
                }

                i++;
            }

            this.medicamentosSinStock = medicamentosSinStock;
            this.medicamentosConStock = medicamentosConStock;
            this.cantidadesFaltantes = cantidadesFaltantes;
            this.cantidadesExistentes = cantidadesExistentes;
        }
    }

}


