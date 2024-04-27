package pe.sanmiguel.bienestar.proyecto_gtics.Dto;


import jakarta.persistence.criteria.CriteriaBuilder;

public interface UsuarioSedeFarmacistaDto {

    Integer getId();

    Integer getIdRol();

    String getCorreo();

    String getContrasena();

    String getNombres();

    String getApellidos();

    String getCelular();

    String getDni();

    String getDireccion();

    String getDistrito();

    String getSeguro();

    Integer getEstadoUsuario();

    Integer getIdSede();

    String getCodigoMed();

    Integer getAprobado();

}
