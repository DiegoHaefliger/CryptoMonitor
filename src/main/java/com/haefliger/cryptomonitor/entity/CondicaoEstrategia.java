package com.haefliger.cryptomonitor.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.haefliger.cryptomonitor.enums.OperadorComparacaoEnum;
import com.haefliger.cryptomonitor.enums.TipoIndicadorEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Estrategia getEstrategia() {
        return estrategia;
    }

    public void setEstrategia(Estrategia estrategia) {
        this.estrategia = estrategia;
    }

    public TipoIndicadorEnum getTipoIndicador() {
        return tipoIndicador;
    }

    public void setTipoIndicador(TipoIndicadorEnum tipoIndicador) {
        this.tipoIndicador = tipoIndicador;
    }

    public OperadorComparacaoEnum getOperador() {
        return operador;
    }

    public void setOperador(OperadorComparacaoEnum operador) {
        this.operador = operador;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    @Override
    public String toString() {
        return "CondicaoEstrategia{id=" + id + ", tipoIndicador=" + tipoIndicador
                + ", operador=" + operador + ", valor=" + valor + "}";
    }
}
