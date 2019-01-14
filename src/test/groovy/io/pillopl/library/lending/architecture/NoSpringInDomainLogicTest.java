package io.pillopl.library.lending.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchUnitRunner;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.runner.RunWith;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@RunWith(ArchUnitRunner.class)
@AnalyzeClasses(packages = {"io.pillopl.library", "org.springframework"})
public class NoSpringInDomainLogicTest {

    @ArchTest
    public static final ArchRule model_should_not_depend_on_spring =
            noClasses()
                    .that()
                    .resideInAPackage(
                            "..io.pillopl.library.lending..model..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("org.springframework..");

    @ArchTest
    public static final ArchRule application_should_not_depend_on_spring =
            noClasses()
                    .that()
                    .resideInAPackage(
                            "..io.pillopl.library.lending..application..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("org.springframework..");


}