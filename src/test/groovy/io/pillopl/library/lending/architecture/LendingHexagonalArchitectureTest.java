package io.pillopl.library.lending.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchUnitRunner;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.runner.RunWith;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@RunWith(ArchUnitRunner.class)
@AnalyzeClasses(packages = {"io.pillopl.library.lending", "org.springframework"})
public class LendingHexagonalArchitectureTest {

    @ArchTest
    public static final ArchRule domain_should_not_depend_on_application =
            noClasses()
                    .that()
                    .resideInAPackage("..domain..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("io.pillopl.library.lending.application..");

    @ArchTest
    public static final ArchRule domain_should_not_depend_on_infrastructure =
            noClasses()
                    .that()
                    .resideInAPackage("io.pillopl.library.lending.domain..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("io.pillopl.library.lending.infrastructure..");

    @ArchTest
    public static final ArchRule domain_should_not_depend_on_ui =
            noClasses()
                    .that()
                    .resideInAPackage("io.pillopl.library.lending.domain..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("io.pillopl.library.lending.ui..");

    @ArchTest
    public static final ArchRule application_should_not_depend_on_infrastructure =
            noClasses()
                    .that()
                    .resideInAPackage("io.pillopl.library.lending.application..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("io.pillopl.library.lending.infrastructure..");

    @ArchTest
    public static final ArchRule application_should_not_depend_on_ui =
            noClasses()
                    .that()
                    .resideInAPackage("io.pillopl.library.lending.application..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("io.pillopl.library.lending.ui..");

    @ArchTest
    public static final ArchRule ui_should_not_depend_on_infrastructure =
            noClasses()
                    .that()
                    .resideInAPackage("io.pillopl.library.lending.ui..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("io.pillopl.library.lending.infrastructure..");


}