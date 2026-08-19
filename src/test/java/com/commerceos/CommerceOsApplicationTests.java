package com.commerceos;

import org.junit.jupiter.api.Test;

/**
 * Sanity checks for the main application class.
 *
 * <p>The full Spring context integration tests are in the {@code iam.integration} package where
 * Testcontainers provide real Postgres/Redis instances. This test does not load the Spring context
 * to avoid requiring external services in the basic build.
 */
class CommerceOsApplicationTests {

  @Test
  void applicationClassShouldLoad() {
    // Verify the main class loads without errors (no reflective access failures, etc.)
    assertDoesNotThrow(() -> Class.forName("com.commerceos.CommerceOsApplication"));
  }

  private void assertDoesNotThrow(ThrowingRunnable runnable) {
    try {
      runnable.run();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @FunctionalInterface
  private interface ThrowingRunnable {
    void run() throws Exception;
  }
}
