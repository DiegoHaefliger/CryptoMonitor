package com.haefliger.cryptomonitor.entity;

import com.haefliger.cryptomonitor.enums.OperadorLogicoEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"condicoes"})
@Entity
@Table(name = "estrategias")
public class Estrategia  {

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

    @Enumerated(EnumType.STRING)
    @Column(name = "operador_logico", nullable = false, length = 10)
    private OperadorLogicoEnum operadorLogico;

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

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Estrategia that = (Estrategia) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
