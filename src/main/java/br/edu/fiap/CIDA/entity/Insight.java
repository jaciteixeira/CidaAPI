package br.edu.fiap.CIDA.entity;

import jakarta.persistence.*;
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
@Table(name = "T_OP_INSIGHT")
public class Insight {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SQ_OP_INSIGHT")
    @SequenceGenerator(name = "SQ_OP_INSIGHT", sequenceName = "SQ_OP_INSIGHT", allocationSize = 1)
    @Column(name = "ID_INSIGHT")
    private Long id;

    @Column(name = "DATA_GERACAO")
    private LocalDateTime dataGeracao;

    @Column(name = "descricao")
    private String descricao;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(
            name = "id_usuario",
            referencedColumnName = "id_usuario",
            foreignKey = @ForeignKey(
                    name = "FK_OP_USUARIO_INSIGHT"
            )
    )
    private Usuario usuario;



}
