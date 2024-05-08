package pe.sanmiguel.bienestar.proyecto_gtics.Controller;

import lombok.Getter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pe.sanmiguel.bienestar.proyecto_gtics.CurrentTimeSQL;
import pe.sanmiguel.bienestar.proyecto_gtics.Dto.MedicamentosSedeStockDto;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.*;
import pe.sanmiguel.bienestar.proyecto_gtics.Repository.*;

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



    @GetMapping("/farmacista")
    public String farmacistaInicio(Model model) {
        List<Medicamento> listaMedicamentos = medicamentoRepository.findAll();
        sedeSession = sedeRepository.getSedeByIdSede(1);
        model.addAttribute("sedeSession", sedeSession);
        model.addAttribute("listaMedicamentos", listaMedicamentos);
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
    public String formPacienteData(Model model) {

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
        return "farmacista/formulario_paciente";
    }

    @PostMapping("/farmacista/finalizar_compra")
    public String createOrdenVenta(@RequestParam(value = "name") String name,
                                   @RequestParam(value = "lastname") String lastname,
                                   @RequestParam(value = "dni") String dni,
                                   @RequestParam(value = "distrito") String distrito,
                                   @RequestParam(value = "direccion") String direccion,
                                   @RequestParam(value = "doctor") String doctor,
                                   @RequestParam(value = "seguro") String seguro,
                                   @RequestParam(value = "correo") String correo,
                                   @RequestParam(value = "celular") String celular,

                                   @RequestParam(value = "listaIds") List<String> listaSelectedIds,
                                   @RequestParam(value = "priceTotal") String priceTotal) {

        idVerOrdenCreada = 0;

        medicamentosSeleccionados = getMedicamentosFromLista(listaSelectedIds);
        listaCantidades = getCantidadesFromLista(listaSelectedIds);

        verificationStock verificationStock = new verificationStock(medicamentosSeleccionados, listaCantidades);
        verificationUser verificationUser = new verificationUser(name,lastname,dni,distrito,direccion,seguro,correo,celular);

        this.pacienteOnStore = verificationUser.getUser();

        if (verificationStock.getMedicamentosSinStock().isEmpty()){

            Orden newOrden = new Orden();

            Integer lastOrderId = ordenRepository.findLastOrdenId();
            int newOrderId = (lastOrderId != null) ? lastOrderId + 1 : 1;
            newOrden.setIdOrden(newOrderId);
            newOrden.setFechaIni(CurrentTimeSQL.getCurrentDate());
            newOrden.setPrecioTotal(Float.parseFloat(priceTotal));
            //Id conocido porque no hay session
            newOrden.setIdFarmacista(7);
            newOrden.setTipoOrden(1);
            newOrden.setEstadoOrden(8);
            newOrden.setSede(sedeSession);

            if (!doctor.equals("sin-doctor")) {
                newOrden.setDoctor(doctorRepository.getByIdDoctor(Integer.valueOf(doctor)));
            }

            newOrden.setPaciente(verificationUser.getUser());

            ordenRepository.save(newOrden);

            int i = 0;
            for (Medicamento med : medicamentosSeleccionados){

                OrdenContenidoId contenidoId = new OrdenContenidoId();
                contenidoId.setIdOrden(newOrderId);
                contenidoId.setIdMedicamento(med.getIdMedicamento());

                OrdenContenido contenido = new OrdenContenido();
                contenido.setId(contenidoId);
                contenido.setIdOrden(newOrden);
                contenido.setIdMedicamento(med);
                contenido.setCantidad(Integer.parseInt(listaCantidades.get(i)));
                ordenContenidoRepository.save(contenido);
                i++;
            }

            idVerOrdenCreada = newOrderId;
            return "redirect:/farmacista/ver_orden_venta";
        } else {


            this.medicamentosSinStock = verificationStock.getMedicamentosSinStock();
            this.medicamentosConStock = verificationStock.getMedicamentosConStock();
            this.cantidadesFaltantes = verificationStock.getCantidadesFaltantes();
            this.cantidadesExistentes = verificationStock.getCantidadesExistentes();

            return "redirect:/farmacista/crear_preorden";
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



    @GetMapping("/farmacista/ver_orden_venta")
    public String verOrdenVenta(Model model) {

        Optional<Orden> ordenOptional = ordenRepository.findById(idVerOrdenCreada);
        List<OrdenContenido> contenidoOrden = ordenContenidoRepository.findMedicamentosByOrdenId(String.valueOf(idVerOrdenCreada));

        if (ordenOptional.isPresent()){
            Orden ordenComprobada = ordenOptional.get();
            model.addAttribute("orden",ordenComprobada);
            model.addAttribute("contenidoOrden", contenidoOrden);
            return "farmacista/ver_orden_venta";
        } else {

            return "farmacista/errorPages/no_existe_orden";
        }
    }


    @PostMapping("/farmacista/ver_orden_venta_tabla")
    public String verOrdenesVentaTabla(@RequestParam(value = "idOrden") String idOrdenTabla){

        idVerOrdenCreada = Integer.valueOf(idOrdenTabla);

        return "redirect:/farmacista/ver_orden_venta";
    }

    @GetMapping("/farmacista/ordenes_venta")
    public String tablaOrdenesVenta(Model model) {
        List<Orden> listaOrdenesVenta = ordenRepository.findAllOrdenes();
        model.addAttribute("listaOrdenesVenta", listaOrdenesVenta);
        return "farmacista/ordenes_venta";
    }

    @GetMapping("farmacista/ver_pre_orden")
    public String verPreOrden(Model model) {

        Optional<Orden> preOrdenOptional = ordenRepository.findById(idVerOrdenCreada);
        List<OrdenContenido> contenidoPreOrden = ordenContenidoRepository.findMedicamentosByOrdenId(String.valueOf(idVerOrdenCreada));


        if (preOrdenOptional.isPresent()){
            Orden preOrdenComprobada = preOrdenOptional.get();
            Orden ordenParent = ordenRepository.getOrdenByIdOrden(preOrdenComprobada.getOrdenParent());

            List<OrdenContenido> contenidoOrden = ordenContenidoRepository.findMedicamentosByOrdenId(String.valueOf(ordenParent.getIdOrden()));

            model.addAttribute("orden",ordenParent);
            model.addAttribute("contenidoOrden", contenidoOrden);
            model.addAttribute("preOrden",preOrdenComprobada);
            model.addAttribute("contenidoPreOrden", contenidoPreOrden);
            return "farmacista/ver_pre_orden";
        } else {

            return "farmacista/errorPages/no_existe_orden";
        }
    }

    @PostMapping("/farmacista/ver_pre_orden_tabla")
    public String verPreOrdenesVentaTabla(@RequestParam(value = "idOrden") String idOrdenTabla){

        idVerOrdenCreada = Integer.valueOf(idOrdenTabla);

        return "redirect:/farmacista/ver_pre_orden";
    }

    @GetMapping("/farmacista/pre_ordenes")
    public String tablaPreOrdenes(Model model) {
        List<Orden> listaPreOrdenes = ordenRepository.findAllPreOrdenes();
        model.addAttribute("listaPreOrdenes", listaPreOrdenes);
        return "farmacista/pre_ordenes";
    }





    @GetMapping("/farmacista/ver_orden_web")
    public String verOrdenWeb(Model model) {

        Optional<Orden> ordenWebOptional = ordenRepository.findById(idVerOrdenCreada);
        List<OrdenContenido> contenidoOrdenWeb = ordenContenidoRepository.findMedicamentosByOrdenId(String.valueOf(idVerOrdenCreada));


        if (ordenWebOptional.isPresent()){
            Orden ordenWebComprobada = ordenWebOptional.get();

            model.addAttribute("contenidoOrden", contenidoOrdenWeb);
            model.addAttribute("orden",ordenWebComprobada);
            return "farmacista/ver_orden_web";
        } else {

            return "farmacista/errorPages/no_existe_orden";
        }
    }

    @GetMapping("/farmacista/ver_orden_web_sinstock")
    public String verOrdenWebSinStock() {

        return "farmacista/ver_orden_web_sinStock";
    }

    @PostMapping("/farmacista/ver_orden_web_tabla")
    public String verPreOrdenesWebTabla(@RequestParam(value = "idOrden") String idOrdenTabla){

        idVerOrdenCreada = Integer.valueOf(idOrdenTabla);

        return "redirect:/farmacista/ver_orden_web";
    }

    @GetMapping("/farmacista/ordenes_web")
    public String tablaOrdenesWeb(Model model) {
        List<Orden> listaOrdenesWeb = ordenRepository.findAllOrdenesWeb();
        model.addAttribute("listaOrdenesWeb", listaOrdenesWeb);
        return "farmacista/ordenes_web";
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

            Optional<Usuario> userOptional = usuarioRepository.findByCorreoAndDni(correo, dni);

            if (userOptional.isPresent()) {
                userExist = true;
                user = userOptional.get();

            } else {
                Usuario newUser = new Usuario();

                Integer lastUserId = usuarioRepository.findLastUsuarioId();
                int newUserId = (lastUserId != null) ? lastUserId + 1 : 1;
                newUser.setIdUsuario(newUserId);

                newUser.setRol(5);
                newUser.setCorreo(correo);
                newUser.setContrasena("");
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


