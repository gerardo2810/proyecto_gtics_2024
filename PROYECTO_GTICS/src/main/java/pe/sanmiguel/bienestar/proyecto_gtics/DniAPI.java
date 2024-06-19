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
import java.util.Arrays;
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
            values.add(formatString((String) jsonObject.get("nombres")));
            values.add(formatString((String) jsonObject.get("apellidoPaterno")));
            values.add(formatString((String) jsonObject.get("apellidoMaterno")));
            values.add(formatString((String) jsonObject.get("dni")));
            values.add((String) jsonObject.get("tipoDocumento"));
            values.add((String) jsonObject.get("numeroDocumento"));
            values.add((String) jsonObject.get("digitoVerificador"));

            System.out.println(jsonObject);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return values;
    }

    public static String formatString(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Lista de palabras que deben permanecer en minúsculas
        List<String> lowerCaseWords = Arrays.asList("de", "del", "la", "los", "las", "y", "da", "dos");

        String[] words = input.split("\\s+");
        StringBuilder formattedName = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (!word.isEmpty()) {
                // Capitaliza la primera palabra siempre
                if (i == 0) {
                    formattedName.append(Character.toUpperCase(word.charAt(0)));
                    if (word.length() > 1) {
                        formattedName.append(word.substring(1).toLowerCase());
                    }
                } else {
                    if (lowerCaseWords.contains(word.toLowerCase())) {
                        formattedName.append(word.toLowerCase());
                    } else {
                        formattedName.append(Character.toUpperCase(word.charAt(0)));
                        if (word.length() > 1) {
                            formattedName.append(word.substring(1).toLowerCase());
                        }
                    }
                }
                formattedName.append(" ");
            }
        }

        // Quitar el último espacio adicional
        return formattedName.toString().trim();
    }



}
