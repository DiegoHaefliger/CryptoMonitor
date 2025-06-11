package com.haefliger.cryptomonitor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "estrategias")
public class Estrategia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "estrategia", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CondicaoEstrategia> condicoes;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 20)
    private String simbolo;

    @Column(nullable = false, length = 10)
    private String intervalo;

    @Column(name = "operador_logico", nullable = false, length = 10)
    private String operadorLogico;

    @Column(name = "date_created", nullable = false, updatable = false, insertable = false)
    private LocalDateTime dateCreated;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @PrePersist
    public void prePersist() {
        if (dateCreated == null) {
            dateCreated = LocalDateTime.now();
        }
    }
}
