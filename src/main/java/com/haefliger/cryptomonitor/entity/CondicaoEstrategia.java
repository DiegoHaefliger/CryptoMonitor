package com.haefliger.cryptomonitor.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.haefliger.cryptomonitor.enums.TipoIndicadorEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        CondicaoEstrategia that = (CondicaoEstrategia) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
