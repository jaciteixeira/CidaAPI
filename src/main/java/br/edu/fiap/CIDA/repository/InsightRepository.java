package br.edu.fiap.CIDA.repository;

import br.edu.fiap.CIDA.entity.Insight;
import br.edu.fiap.CIDA.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InsightRepository extends JpaRepository<Insight,Long> {
    List<Insight> findByUsuario(Usuario usuario);


}
