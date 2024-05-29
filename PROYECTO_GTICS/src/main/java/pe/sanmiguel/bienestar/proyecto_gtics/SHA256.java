package pe.sanmiguel.bienestar.proyecto_gtics;

import org.springframework.security.crypto.bcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256 {
    public static String cipherPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

}
