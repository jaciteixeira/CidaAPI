package br.edu.fiap.CIDA.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
@Table(name = "T_OP_ROLE", uniqueConstraints = {
        @UniqueConstraint(name = "UK_OP_NAME", columnNames = "NAME")
})
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SQ_OP_ROLE")
    @SequenceGenerator(name = "SQ_OP_ROLE", sequenceName = "SQ_OP_ROLE", allocationSize = 1)
    @Column(name = "ID_ROLE")
    private Long id;
    @Column(name = "NAME")
    private String name;

}
