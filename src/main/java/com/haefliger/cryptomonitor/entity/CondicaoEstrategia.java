package com.haefliger.cryptomonitor.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.haefliger.cryptomonitor.enums.TipoIndicadorEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"estrategia"})
@ToString(exclude = {"estrategia"})
@Entity
@Table(name = "condicoes_estrategia")
public class CondicaoEstrategia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "estrategia_id", nullable = false)
    @JsonIgnore
    private Estrategia estrategia;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_indicador", nullable = false, length = 20)
    private TipoIndicadorEnum tipoIndicador;

    @Column(nullable = false, length = 5)
    private String operador;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String valor;

    @Column(name = "date_created", nullable = false)
    private LocalDateTime dateCreated;

    @PrePersist
    public void prePersist() {
        if (dateCreated == null) {
            dateCreated = LocalDateTime.now();
        }
    }

}
