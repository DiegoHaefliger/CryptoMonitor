package com.haefliger.cryptomonitor.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.haefliger.cryptomonitor.enums.OperadorComparacaoEnum;
import com.haefliger.cryptomonitor.enums.TipoIndicadorEnum;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@ToString(exclude = {"estrategia"})
@Table(name = "condicoes_estrategia")
public class CondicaoEstrategia implements Serializable {

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private OperadorComparacaoEnum operador;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal valor;

    @Column(name = "date_created", nullable = false)
    private LocalDateTime dateCreated;

    @PrePersist
    public void prePersist() {
        if (dateCreated == null) {
            dateCreated = LocalDateTime.now();
        }
    }
}
