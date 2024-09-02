package br.edu.fiap.CIDA.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "T_OP_USUARIO", uniqueConstraints = {
        @UniqueConstraint(name = "UK_OP_USER_ID_AUTH", columnNames = "id_auth"),
        @UniqueConstraint(name = "UK_EMAIL", columnNames = "EMAIL_USUARIO")
})
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SQ_OP_USUARIO")
    @SequenceGenerator(name = "SQ_OP_USUARIO", sequenceName = "SQ_OP_USUARIO", allocationSize = 1)
    @Column(name = "ID_USUARIO")
    private Long id;

    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(
            name = "id_auth ",
            referencedColumnName = "id_auth " ,
            foreignKey = @ForeignKey(
                    name = "FK_USUARIO_AUTH"
            )
    )
    private Auth auth_user;

    @Column(name = "TELEFONE")
    @NotEmpty(message = "Telefone não pode estar em branco")
    private String telefone;

    @Column(name = "DATA_CRIACAO")
    private LocalDateTime dataCriacao;

    @Column(name = "STATUS")
    private String status;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento")
    private TipoDocumento tipoDoc;

    @Column(name = "num_documento")
    @NotEmpty(message = "Numero do documento não pode estar em branco")
    private String numeroDocumento;

}