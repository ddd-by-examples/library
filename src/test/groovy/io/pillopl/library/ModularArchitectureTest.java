package io.pillopl.library;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchUnitRunner;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.runner.RunWith;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@RunWith(ArchUnitRunner.class)
@AnalyzeClasses(packages = "io.pillopl")
public class ModularArchitectureTest {

    @ArchTest
    public static final ArchRule catalogue_should_not_depend_on_lending =
            noClasses()
                    .that()
                    .resideInAPackage("..catalogue..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("..lending..");

    @ArchTest
    public static final ArchRule commons_should_not_depend_on_catalogue =
            noClasses()
                    .that()
                    .resideInAPackage("..commons..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("..catalogue..");

    @ArchTest
    public static final ArchRule commons_should_not_depend_on_lending =
            noClasses()
                    .that()
                    .resideInAPackage("..commons..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("..lending..");


}