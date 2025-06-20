package com.haefliger.cryptomonitor.dto.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstrategiaCacheDTO implements Serializable {
    private Long id;
    private String nome;
    private String simbolo;
    private String intervalo;
    private String operadorLogico;
    private LocalDateTime dateCreated;
    private LocalDateTime dateLastUpdate;
    private Boolean ativo;
    private Boolean permanente;
    private List<CondicaoEstrategiaCacheDTO> condicoes;
}

