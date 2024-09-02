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
@Table(name = "T_OP_ARQUIVO ", uniqueConstraints = {
        @UniqueConstraint(name = "UK_NOME", columnNames = "NM_ARQUIVO")
})
public class Arquivo {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SQ_OP_ARQUIVO")
    @SequenceGenerator(name = "SQ_OP_ARQUIVO", sequenceName = "SQ_OP_ARQUIVO", allocationSize = 1)
    @Column(name = "ID_ARQUIVO")
    private Long id;

    @Column(name = "NM_ARQUIVO")
    private String nome;

    @Column(name = "url")
    private String url;

    @Column(name = "EXTENCAO_ARQUIVO")
    private String extensao;

    @Column(name = "TAMANHO")
    private Long tamanho;

    @Column(name = "DT_ARQUIVO")
    private LocalDateTime dataUpload;

    @Column(name = "RESUMO")
    private String resumo;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(
            name = "ID_usuario",
            referencedColumnName = "ID_usuario",
            foreignKey = @ForeignKey(name = "FK_USUARIO_ARQUIVO")
    )
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(
            name = "ID_insight",
            referencedColumnName = "ID_insight",
            foreignKey = @ForeignKey(name = "FK_insight_ARQUIVO")
    )
    private Insight insight;
}
