package com.commerceos.platform.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
    packages = "com.commerceos",
    importOptions = {ImportOption.DoNotIncludeTests.class})
public class ModuleBoundaryTest {

  @ArchTest
  static final ArchRule noCrossModuleDirectRepositoryAccess =
      noClasses()
          .that()
          .resideInAPackage("com.commerceos.analytics..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("com.commerceos.inventory.repository..");

  @ArchTest
  static final ArchRule iamShouldNotDependOnBusinessModules =
      noClasses()
          .that()
          .resideInAPackage("com.commerceos.iam..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "com.commerceos.procurement..",
              "com.commerceos.recommendation..",
              "com.commerceos.workflow..");
}
