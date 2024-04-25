package pe.sanmiguel.bienestar.proyecto_gtics.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.SedeStock;
import pe.sanmiguel.bienestar.proyecto_gtics.Repository.SedeStockRepository;

import java.util.List;

@Controller
public class SedeStockController {

    final SedeStockRepository sedeStockRepository;

    public SedeStockController(SedeStockRepository sedeStockRepository){
        this.sedeStockRepository = sedeStockRepository;
    }


}
