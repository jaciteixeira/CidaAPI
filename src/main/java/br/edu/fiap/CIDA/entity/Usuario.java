package br.edu.fiap.CIDA.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(
            name = "id_auth ",
            referencedColumnName = "id_auth " ,
            foreignKey = @ForeignKey(
                    name = "FK_USUARIO_AUTH"
            )
    )
    private Auth auth_user;

    @Column(name = "TELEFONE")
    private String telefone;

    @Column(name = "DATA_CRIACAO")
    private LocalDate dataCriacao;

    @Column(name = "IDENTIFICACAO")
    private String identificacao;


    @Column(name = "STATUS")
    private String status;

//    @Column(name = "EMAIL_USUARIO")
//    private String email;

}