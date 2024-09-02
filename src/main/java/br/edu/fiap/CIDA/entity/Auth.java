package br.edu.fiap.CIDA.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
    @Column(name = "id_auth")
    private Long id;
    @Column(name = "EMAIL")
    @NotEmpty(message = "Email não pode estar em branco")
    private String email;
    @Column(name = "HASH_SENHA")
    @Pattern(regexp = "(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*_=+-]).{8,16}")
    @NotEmpty(message = "Senha é obrigatório!")
    private String hashSenha;
    @Column(name = "ULTIMO_LOGIN")
    private LocalDateTime ultimoLogin;
}
