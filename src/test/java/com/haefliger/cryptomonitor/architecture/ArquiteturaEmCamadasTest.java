package com.haefliger.cryptomonitor.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Regras de camada do PADROES_CODIGO.md. Usa a API core do ArchUnit dentro de testes
 * JUnit comuns porque o engine archunit-junit5 ainda mira o JUnit Platform 1.x, e o
 * Quarkus 3.33 ja traz o 6.0.3 — com o engine, as regras nao eram descobertas e a suite
 * passava com zero teste executado.
 */
@DisplayName("Arquitetura em camadas")
class ArquiteturaEmCamadasTest {

    private static final String RAIZ = "com.haefliger.cryptomonitor";

    private static final JavaClasses CLASSES =
            new ClassFileImporter()
                    .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                    .importPackages(RAIZ);

    @Test
    void controllerNaoAcessaJpa() {
        classes()
                .that()
                .haveSimpleNameEndingWith("Controller")
                .should()
                .onlyDependOnClassesThat()
                .resideOutsideOfPackage("jakarta.persistence..")
                .as("Controller nao deve depender de jakarta.persistence")
                .check(CLASSES);
    }

    @Test
    void controllerNaoUsaRepositorio() {
        classes()
                .that()
                .haveSimpleNameEndingWith("Controller")
                .should()
                .onlyDependOnClassesThat()
                .haveSimpleNameNotEndingWith("Repository")
                .as("Controller nao deve depender de Repository")
                .check(CLASSES);
    }

    @Test
    void controllerNaoConheceEntidade() {
        classes()
                .that()
                .haveSimpleNameEndingWith("Controller")
                .should()
                .onlyDependOnClassesThat()
                .resideOutsideOfPackage(RAIZ + ".entity..")
                .as("Controller nao deve depender de Entity")
                .check(CLASSES);
    }

    @Test
    void transacaoSoNoServico() {
        methods()
                .that()
                .areAnnotatedWith(jakarta.transaction.Transactional.class)
                .should()
                .beDeclaredInClassesThat()
                .haveSimpleNameContaining("Service")
                .as("@Transactional so em classe de servico")
                .check(CLASSES);
    }

    @Test
    void repositorioNaoConheceHttp() {
        classes()
                .that()
                .haveSimpleNameEndingWith("Repository")
                .should()
                .onlyDependOnClassesThat()
                .resideOutsideOfPackage("jakarta.ws.rs..")
                .as("Repository nao deve depender de jakarta.ws.rs")
                .check(CLASSES);
    }

    @Test
    void entidadeNaoConheceCamadaDeCima() {
        classes()
                .that()
                .resideInAPackage(RAIZ + ".entity..")
                .should()
                .onlyDependOnClassesThat()
                .resideOutsideOfPackages(
                        RAIZ + ".service..", RAIZ + ".repository..", RAIZ + ".controller..")
                .as("Entity nao deve depender de service, repository ou controller")
                .check(CLASSES);
    }

    @Test
    void dependenciaSempreParaDentro() {
        layeredArchitecture()
                .consideringOnlyDependenciesInAnyPackage(RAIZ + "..")
                .layer("Controller")
                .definedBy(RAIZ + ".controller..")
                .layer("Service")
                .definedBy(RAIZ + ".service..", RAIZ + ".ws.service..")
                .layer("Repository")
                .definedBy(RAIZ + ".repository..")
                .whereLayer("Controller")
                .mayNotBeAccessedByAnyLayer()
                .whereLayer("Repository")
                .mayOnlyBeAccessedByLayers("Service")
                .as("Dependencia sempre para dentro: Controller -> Service -> Repository")
                .check(CLASSES);
    }
}
