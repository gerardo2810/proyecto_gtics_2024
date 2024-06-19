package pe.sanmiguel.bienestar.proyecto_gtics;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
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

    /*private static final String BASE_URL = "https://apisperu.net/api/dni/";
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
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            // Realizar la solicitud
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            // Imprimir la respuesta para depuración
            System.out.println(response);

            // Verificar el código de estado de la respuesta
            if (response.getStatusCode().is5xxServerError()) {
                System.out.println("Error del servidor: " + response.getStatusCodeValue());
                return error; // Devolver lista vacía en caso de error del servidor
            } else if (response.getStatusCode().is2xxSuccessful()) {
                // Convertir la respuesta JSON a lista de cadenas
                return responseToList(response);
            } else {
                System.out.println("Respuesta inesperada: " + response.getStatusCodeValue());
                return error; // Manejar otros códigos de estado si es necesario
            }
        } catch (HttpClientErrorException e) {
            // Manejar errores específicos de cliente (4xx)
            System.out.println("Error cliente: " + e.getStatusCode() + " - " + e.getStatusText());
            return error;
        } catch (HttpServerErrorException e) {
            // Manejar errores específicos de servidor (5xx)
            System.out.println("Error servidor: " + e.getStatusCode() + " - " + e.getStatusText());
            return error;
        } catch (RestClientException e) {
            // Manejar errores generales de RestTemplate
            System.out.println("Error al realizar la solicitud: " + e.getMessage());
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

            System.out.println(jsonObject);

            // Extraer los valores de las claves y añadirlos a la lista
            values.add(formatString((String) jsonObject.get("nombre_completo")));
            values.add(formatString((String) jsonObject.get("apellido_paterno")));
            values.add(formatString((String) jsonObject.get("apellido_materno")));
            values.add(formatString((String) jsonObject.get("numero")));

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
