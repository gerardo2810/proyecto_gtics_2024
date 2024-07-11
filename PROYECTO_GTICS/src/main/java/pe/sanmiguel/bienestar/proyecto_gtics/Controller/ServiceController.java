package pe.sanmiguel.bienestar.proyecto_gtics.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;
import pe.sanmiguel.bienestar.proyecto_gtics.DniAPI;
import pe.sanmiguel.bienestar.proyecto_gtics.Dto.MedicamentoBajoStockDto;
import pe.sanmiguel.bienestar.proyecto_gtics.Dto.OrdenCantidadVentasDto;
import pe.sanmiguel.bienestar.proyecto_gtics.Dto.OrdenContenidoMedicamentoFechaDto;
import pe.sanmiguel.bienestar.proyecto_gtics.PasswordService;
import pe.sanmiguel.bienestar.proyecto_gtics.Repository.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping(value = "/service")
public class ServiceController {

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

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private JavaMailSender mailSender;

    final DniAPI dniAPI;

    public ServiceController(UsuarioRepository usuarioRepository, SedeRepository sedeRepository, SedeStockRepository sedeStockRepository, MedicamentoRepository medicamentoRepository, OrdenRepository ordenRepository, OrdenContenidoRepository ordenContenidoRepository, ReposicionRepository reposicionRepository, EstadoPreOrdenRepository estadoPreOrdenRepository, DoctorRepository doctorRepository, SedeFarmacistaRepository sedeFarmacistaRepository, ReposicionContenidoRepository reposicionContenidoRepository, DniAPI dniAPI) {
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
        this.dniAPI = dniAPI;
    }

    //Servicios Rest para consumo en estadísticas

    //Listar cantidad de ventas en sedes
    @ResponseBody
    @GetMapping(value = {"/listaVentas"})
    public List<OrdenCantidadVentasDto> listarCantidadVentas(){
        return ordenRepository.listarCantidadVentasEnSedes();
    }

    //Listar Medicamentos Bajo en Stock x Sede

    @ResponseBody
    @GetMapping(value = {"/listaMedicamentosBajoStock/{idSede}"})
    public ResponseEntity<HashMap<String, Object>> buscarProducto(@PathVariable("idSede") String idSede) {
        HashMap<String, Object> respuesta = new HashMap<>();

        try {
            int id = Integer.parseInt(idSede);
            List<MedicamentoBajoStockDto> medicamentos = medicamentoRepository.listarMedicamentosBajoStockxSede(id);

            if (!medicamentos.isEmpty()) {
                respuesta.put("result", "ok");
                respuesta.put("ListaMedicamentosBajoStock", medicamentos);
            } else {
                respuesta.put("result", "no existe");
            }
            return ResponseEntity.ok(respuesta);
        } catch (NumberFormatException e) {
            respuesta.put("result", "error");
            respuesta.put("message", "ID de sede no es un número válido");
            return ResponseEntity.badRequest().body(respuesta);
        } catch (Exception e) {
            respuesta.put("result", "error");
            respuesta.put("message", "Ocurrió un error al buscar los medicamentos");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuesta);
        }
    }

    //Listar Venta de Medicamentos por fecha (7 días, 15 días y 3 meses)

    @ResponseBody
    @GetMapping(value = {"/listaVentasPreferidasxFecha/{idSede}/{typeLista}"})
    public ResponseEntity<HashMap<String, Object>> listarVentaMedicamentoxFecha(@PathVariable("idSede") String idSede,
                                                                                @PathVariable("typeLista") String typeLista) {

        HashMap<String, Object> respuesta = new HashMap<>();
        List<OrdenContenidoMedicamentoFechaDto> listaVentaMedicamentosxFecha = new ArrayList<>();

        try {
            int id = Integer.parseInt(idSede);

            //Dependiendo del tipo de lista: "7", "15" o "3"

            if(typeLista.equals("7")){
                listaVentaMedicamentosxFecha = ordenContenidoRepository.listarMedicamentos7dias(id);
                
            } else if (typeLista.equals("15")) {
                listaVentaMedicamentosxFecha = ordenContenidoRepository.listarMedicamentos15dias(id);
            } else if (typeLista.equals("3")) {
                listaVentaMedicamentosxFecha = ordenContenidoRepository.listarMedicamentos3meses(id);
            }


            if (!listaVentaMedicamentosxFecha.isEmpty()) {
                respuesta.put("result", "ok");
                respuesta.put("ListaMedicamentosBajoStock", listaVentaMedicamentosxFecha);
            } else {
                respuesta.put("result", "no existe");
            }
            return ResponseEntity.ok(respuesta);
        } catch (NumberFormatException e) {
            respuesta.put("result", "error");
            respuesta.put("message", "ID de sede no es un número válido");
            return ResponseEntity.badRequest().body(respuesta);
        } catch (Exception e) {
            respuesta.put("result", "error");
            respuesta.put("message", "Ocurrió un error al buscar los medicamentos");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuesta);
        }
    }






}
