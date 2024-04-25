package pe.sanmiguel.bienestar.proyecto_gtics;

import java.util.Random;

public class TokenRandomGenerator {
    public static String generator() {
        int length = 6;
        StringBuilder key = new StringBuilder();

        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int randomValue = random.nextInt(36);
            char randomChar = (char) (randomValue < 10 ? '0' + randomValue : 'A' + (randomValue - 10));
            key.append(randomChar);
        }

        return key.toString();
    }
}
