package pe.sanmiguel.bienestar.proyecto_gtics;


import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Usuario;
import pe.sanmiguel.bienestar.proyecto_gtics.Repository.UsuarioRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class EmailRetryService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordService passwordService;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void scheduleEmailRetry(Usuario usuario) {
        Runnable emailTask = new Runnable() {
            @Override
            public void run() {
                Usuario updatedUsuario = usuarioRepository.findById(usuario.getIdUsuario()).orElse(null);
                if (updatedUsuario != null && updatedUsuario.getEstadoContra() == 2) {
                    String temporaryPassword = passwordService.generateTemporaryPassword();
                    updatedUsuario.setContrasena(new BCryptPasswordEncoder(12).encode(temporaryPassword));
                    usuarioRepository.save(updatedUsuario); // Guarda la nueva contraseña en la base de datos

                    Map<String, Object> variables = new HashMap<>();
                    variables.put("nombre", updatedUsuario.getNombres());
                    variables.put("contra", temporaryPassword);  // Actualiza la variable de la contraseña

                    try {
                        emailService.sendHtmlEmail(updatedUsuario.getCorreo(), "Bienvenido(a) a Bienestar San Miguel", "login/correo2", variables);
                        System.out.println("Correo enviado correctamente.");
                    } catch (MessagingException e) {
                        e.printStackTrace();
                        System.out.println("Error al enviar el correo, se volverá a intentar.");
                    }
                } else {
                    // Detener el scheduler si el estadoContra es 1
                    System.out.println("Deteniendo el envío de correos, el estadoContra ya no es 2.");
                    scheduler.shutdown();
                }
            }
        };

        // Programar la tarea para que se ejecute cada 5 minutos
        scheduler.scheduleAtFixedRate(emailTask, 0, 5, TimeUnit.MINUTES);
    }
}