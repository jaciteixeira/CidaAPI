package br.edu.fiap.CIDA.repository;

import br.edu.fiap.CIDA.entity.Auth;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthRepository extends JpaRepository<Auth,Long> {
    Auth findByEmail(String email);
}
