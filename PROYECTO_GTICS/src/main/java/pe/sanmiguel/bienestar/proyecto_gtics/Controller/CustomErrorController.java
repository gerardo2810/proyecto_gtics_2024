package pe.sanmiguel.bienestar.proyecto_gtics.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;



@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        // Obtén el código de estado del error
        Object status = request.getAttribute("javax.servlet.error.status_code");

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            // Maneja el error 404
            if (statusCode == 404) {
                return "404";
            }
        }
        // Devuelve una vista genérica para otros errores
        return "login/error404";
    }

}