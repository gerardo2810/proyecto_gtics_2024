package pe.sanmiguel.bienestar.proyecto_gtics.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.CodigoColegiatura;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Token;

import java.util.List;

public interface TokenRepository extends JpaRepository<Token, Integer> {
    @Query(nativeQuery = true, value = "SELECT * FROM tokens")
    List<Token> listarTokens();
}
