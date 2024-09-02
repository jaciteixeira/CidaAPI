package br.edu.fiap.CIDA.repository;

import br.edu.fiap.CIDA.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario,Long> {
}
