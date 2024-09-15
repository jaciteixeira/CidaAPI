package br.edu.fiap.CIDA.repository;

import br.edu.fiap.CIDA.entity.Arquivo;
import br.edu.fiap.CIDA.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArquivoRepository extends JpaRepository<Arquivo, Long> {
    List<Arquivo> findByUsuario(Usuario user);
}
