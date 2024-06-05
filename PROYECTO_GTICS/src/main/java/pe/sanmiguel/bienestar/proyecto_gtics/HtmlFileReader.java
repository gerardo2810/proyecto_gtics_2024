package pe.sanmiguel.bienestar.proyecto_gtics;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Component
public class HtmlFileReader {

    private final ResourceLoader resourceLoader;

    public HtmlFileReader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public String readHtmlFile() {
        try {
            Resource resource = resourceLoader.getResource("classpath:templates/login/correo.html");
            InputStream inputStream = resource.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            // Manejar la excepción apropiadamente
            return null;
        }
    }
}