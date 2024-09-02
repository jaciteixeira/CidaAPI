package br.edu.fiap.CIDA.repository;

import br.edu.fiap.CIDA.entity.Insight;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InsightRepository extends JpaRepository<Insight,Long> {
}
