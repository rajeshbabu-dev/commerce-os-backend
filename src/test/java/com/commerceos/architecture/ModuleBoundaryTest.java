package com.commerceos.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Architecture boundary tests for the CommerceOS modular monolith.
 *
 * <p>These tests enforce the module isolation rules from the technical architecture doc §2:
 *
 * <ul>
 *   <li>No module may import another module's {@code infrastructure.persistence} package
 *   <li>Domain layers must not depend on infrastructure layers
 * </ul>
 */
@AnalyzeClasses(packages = "com.commerceos", importOptions = ImportOption.DoNotIncludeTests.class)
class ModuleBoundaryTest {

  // -- Module boundary: no cross-module persistence access -----------------------
  //
  // Each module's infrastructure.persistence is private to that module.
  // Other modules must go through the owning module's application port.

  @ArchTest
  static final ArchRule IAM_INFRASTRUCTURE_PERSISTENCE_SHOULD_NOT_BE_ACCESSED =
      noClasses()
          .that()
          .resideOutsideOfPackage("..iam..")
          .should()
          .accessClassesThat()
          .resideInAnyPackage("..iam.infrastructure.persistence..")
          .because("IAM persistence is internal and must not be accessed by other modules");

  @ArchTest
  static final ArchRule DOMAIN_SHOULD_NOT_DEPEND_ON_INFRASTRUCTURE =
      noClasses()
          .that()
          .resideInAnyPackage("..domain..")
          .should()
          .accessClassesThat()
          .resideInAnyPackage("..infrastructure..")
          .because("Domain layer must not depend on infrastructure layer");

  @ArchTest
  static final ArchRule APPLICATION_SHOULD_NOT_DEPEND_ON_INFRASTRUCTURE_WEB =
      noClasses()
          .that()
          .resideInAnyPackage("..application..")
          .should()
          .accessClassesThat()
          .resideInAnyPackage("..infrastructure.web..")
          .because("Application layer must not depend on web/infrastructure layer");

  @ArchTest
  static final ArchRule SHARED_KERNEL_SHOULD_NOT_DEPEND_ON_OTHER_PACKAGES =
      noClasses()
          .that()
          .resideInAnyPackage("..sharedkernel..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "..iam..",
              "..inventory..",
              "..supplier..",
              "..procurement..",
              "..workflow..",
              "..recommendation..",
              "..analytics..",
              "..notification..")
          .because("Shared kernel must not depend on any module");
}
