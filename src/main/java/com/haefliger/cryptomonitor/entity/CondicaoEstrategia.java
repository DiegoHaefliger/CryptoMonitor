package com.haefliger.cryptomonitor.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "condicoes_estrategia")
public class CondicaoEstrategia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "estrategia_id", nullable = false)
    @JsonIgnore
    private Estrategia estrategia;

    @Column(name = "tipo_indicador", nullable = false, length = 20)
    private String tipoIndicador;

    @Column(nullable = false, length = 5)
    private String operador;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String valor;

    @Column(name = "date_created", nullable = false, updatable = false, insertable = false)
    private LocalDateTime dateCreated;

    @PrePersist
    public void prePersist() {
        if (dateCreated == null) {
            dateCreated = LocalDateTime.now();
        }
    }

}
