package com.haefliger.cryptomonitor.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"condicoes"})
@ToString(exclude = {"condicoes"})
@Entity
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

    @Column(name = "date_created", nullable = false)
    private LocalDateTime dateCreated;

    @Column(name = "date_last_update")
    private LocalDateTime dateLastUpdate;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo;

    @Column(name = "permanente", nullable = false)
    private Boolean permanente;

    @PrePersist
    public void prePersist() {
        if (dateCreated == null) {
            dateCreated = LocalDateTime.now();
        }
        if (ativo == null) {
            ativo = Boolean.TRUE;
        }
        if (permanente == null) {
            permanente = Boolean.FALSE;
        }
    }
}
