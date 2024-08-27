package br.edu.fiap.CIDA.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "T_OP_AUTH")
public class Auth {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SQ_OP_AUTH")
    @SequenceGenerator(name = "SQ_OP_AUTH", sequenceName = "SQ_OP_AUTH", allocationSize = 1)
    @Column(name = "id_auth ")
    private Long id;
    @NotNull
    @Column(name = "EMAIL")
    private String email;
    @Column(name = "HASH_SENHA")
    private String hashSenha;
    @Column(name = "ULTIMO_LOGIN")
    private LocalDateTime ultimoLogin;
}
