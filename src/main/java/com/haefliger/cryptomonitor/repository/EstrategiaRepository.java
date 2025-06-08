package com.haefliger.cryptomonitor.repository;


import com.haefliger.cryptomonitor.entity.Estrategia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstrategiaRepository extends JpaRepository<Estrategia, Long> {
}
