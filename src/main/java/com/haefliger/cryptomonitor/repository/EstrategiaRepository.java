package com.haefliger.cryptomonitor.repository;


import com.haefliger.cryptomonitor.entity.Estrategia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EstrategiaRepository extends JpaRepository<Estrategia, Long> {

    List<Estrategia> findByAtivo(Boolean ativo);

    @Query("SELECT e FROM Estrategia e LEFT JOIN FETCH e.condicoes WHERE e.ativo = :ativo")
    List<Estrategia> findByAtivoFetchCondicoes(@Param("ativo") boolean ativo);
}
