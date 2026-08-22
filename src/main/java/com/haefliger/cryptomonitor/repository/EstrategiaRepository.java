package com.haefliger.cryptomonitor.repository;

import com.haefliger.cryptomonitor.entity.Estrategia;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class EstrategiaRepository {

    private final EntityManager em;

    EstrategiaRepository(EntityManager em) {
        this.em = em;
    }

    public List<Estrategia> findAll() {
        return em.createQuery("select e from Estrategia e", Estrategia.class).getResultList();
    }

    public List<Estrategia> findByAtivo(Boolean ativo) {
        return em.createQuery("select e from Estrategia e where e.ativo = :ativo", Estrategia.class)
                .setParameter("ativo", ativo)
                .getResultList();
    }

    public List<Estrategia> findByAtivoFetchCondicoes(boolean ativo) {
        return em.createQuery(
                        "select e from Estrategia e left join fetch e.condicoes where e.ativo = :ativo",
                        Estrategia.class)
                .setParameter("ativo", ativo)
                .getResultList();
    }

    public Optional<Estrategia> findById(Long id) {
        return Optional.ofNullable(em.find(Estrategia.class, id));
    }

    public Estrategia save(Estrategia estrategia) {
        if (estrategia.getId() == null) {
            em.persist(estrategia);
            return estrategia;
        }
        return em.merge(estrategia);
    }

    public void deleteById(Long id) {
        findById(id).ifPresent(em::remove);
    }
}
