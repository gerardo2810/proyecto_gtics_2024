package pe.sanmiguel.bienestar.proyecto_gtics;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;

@Component
public class DniAPI {

    /* APIS.NET.PE */
    private static final String BASE_URL = "https://api.apis.net.pe/v2/reniec/dni?numero=";
    private static final String BEARER_TOKEN = "apis-token-9064.5WCzn6X328Q2gIu0iPb1sCgy35LAQn3H"; // Token

    /* APISPERU.NET */
    /*
    private static final String BASE_URL = "https://apisperu.net/api/dni/";
    private static final String BEARER_TOKEN = "63d3045c50befe3ba510e6cc787916eb9e579cee712dc8fae2ec478386e4f488"; // Token
    */

    public static List<String> getDni(String dni) {
        RestTemplate restTemplate = new RestTemplate();
        String url = BASE_URL + dni;
        List<String> error = new ArrayList<>();

        // Configurar los encabezados
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("Authorization", "Bearer " + BEARER_TOKEN);

        // Crear la entidad HTTP con los encabezados
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Realizar la solicitud
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        // Verificar la respuesta
        if (response.getStatusCode().is2xxSuccessful()) {
            return responseToList(response);
        } else {
            return error;
        }
    }

    public static List<String> responseToList(ResponseEntity<String> response){
        // Crear una lista para almacenar los valores
        List<String> values = new ArrayList<>();
        try {
            // Parsear el JSON
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(response.getBody());

            // Extraer los valores de las claves y añadirlos a la lista
            values.add((String) jsonObject.get("nombres"));
            values.add((String) jsonObject.get("apellidoPaterno"));
            values.add((String) jsonObject.get("apellidoMaterno"));
            values.add((String) jsonObject.get("tipoDocumento"));
            values.add((String) jsonObject.get("numeroDocumento"));
            values.add((String) jsonObject.get("digitoVerificador"));

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return values;
    }


}
