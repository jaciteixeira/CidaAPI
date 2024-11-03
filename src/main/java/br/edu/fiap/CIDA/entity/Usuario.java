package br.edu.fiap.CIDA.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "T_OP_USUARIO", uniqueConstraints = {
        @UniqueConstraint(name = "UK_OP_USER_ID_AUTH", columnNames = "id_auth")
})
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SQ_OP_USUARIO")
    @SequenceGenerator(name = "SQ_OP_USUARIO", sequenceName = "SQ_OP_USUARIO", allocationSize = 1)
    @Column(name = "ID_USUARIO")
    private Long id;

    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE})
    @JoinColumn(
            name = "id_auth",
            referencedColumnName = "id_auth",
            foreignKey = @ForeignKey(name = "FK_USUARIO_AUTH")
    )
    private Auth authUser;

    @Column(name = "TELEFONE")
    private String telefone;

    @Column(name = "DATA_CRIACAO")
    private LocalDateTime dataCriacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento")
    private TipoDocumento tipoDoc;

    @Column(name = "num_documento")
    private String numeroDocumento;

    @Column(name = "NOME_CONTAINER")
    private String nomeContainer;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Arquivo> arquivos = new HashSet<>();
}
