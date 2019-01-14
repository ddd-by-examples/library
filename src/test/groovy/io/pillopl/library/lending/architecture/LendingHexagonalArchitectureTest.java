package io.pillopl.library.lending.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchUnitRunner;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.runner.RunWith;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@RunWith(ArchUnitRunner.class)
@AnalyzeClasses(packages = "io.pillopl.library.lending")
public class LendingHexagonalArchitectureTest {

    @ArchTest
    public static final ArchRule model_should_not_depend_on_application =
            noClasses()
                    .that()
                    .resideInAPackage("..model..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("..application..");

    @ArchTest
    public static final ArchRule model_should_not_depend_on_infrastructure =
            noClasses()
                    .that()
                    .resideInAPackage("..model..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("..infrastructure..");

    @ArchTest
    public static final ArchRule model_should_not_depend_on_ui =
            noClasses()
                    .that()
                    .resideInAPackage("..model..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("..ui..");

    @ArchTest
    public static final ArchRule application_should_not_depend_on_infrastructure =
            noClasses()
                    .that()
                    .resideInAPackage("..application..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("..infrastructure..");

    @ArchTest
    public static final ArchRule application_should_not_depend_on_ui =
            noClasses()
                    .that()
                    .resideInAPackage("..application..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("..ui..");

    @ArchTest
    public static final ArchRule ui_should_not_depend_on_infrastructure =
            noClasses()
                    .that()
                    .resideInAPackage("..ui..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("..infrastructure..");



}