package com.haefliger.cryptomonitor.repository;

import com.haefliger.cryptomonitor.entity.CondicaoEstrategia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CondicaoEstrategiaRepository extends JpaRepository<CondicaoEstrategia, Long> {
}
