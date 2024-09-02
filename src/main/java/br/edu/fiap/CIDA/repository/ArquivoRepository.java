package br.edu.fiap.CIDA.repository;

import br.edu.fiap.CIDA.entity.Arquivo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArquivoRepository extends JpaRepository<Arquivo, Long> {
}
