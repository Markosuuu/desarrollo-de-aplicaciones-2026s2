package com.prontaentrega.architecture;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;

@Tag("arquitectura")
class ArchitectureTest {

    private static final String BASE_PACKAGE = "com.prontaentrega";

    private final JavaClasses importedClasses =
            new ClassFileImporter()
                    .withImportOption(new ImportOption.DoNotIncludeTests())
                    .importPackages(BASE_PACKAGE);

    @Test
    void controllersShouldNotAccessRepositories() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..controllers..")
                .should().dependOnClassesThat()
                .resideInAPackage("..persistence.repository..");

        rule.check(importedClasses);
    }

    @Test
    void servicesShouldNotDependOnControllers() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..services..")
                .should().dependOnClassesThat()
                .resideInAPackage("..controllers..");

        rule.check(importedClasses);
    }

    @Test
    void modelsShouldNotDependOnControllers() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..models..")
                .should().dependOnClassesThat()
                .resideInAPackage("..controllers..");

        rule.check(importedClasses);
    }

    @Test
    void modelsShouldNotDependOnServices() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..models..")
                .should().dependOnClassesThat()
                .resideInAPackage("..services..");

        rule.check(importedClasses);
    }

    @Test
    void modelsShouldNotDependOnrepository() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..models..")
                .should().dependOnClassesThat()
                .resideInAPackage("..persistence.repository..");

        rule.check(importedClasses);
    }

    @Test
    void repositoriesShouldNotDependOnServices() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..persistence.repository..")
                .should().dependOnClassesThat()
                .resideInAPackage("..services..");

        rule.check(importedClasses);
    }
}


