package com.commerceos.platform.logging;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CorrelationIdFilterTest {

  private final CorrelationIdFilter filter = new CorrelationIdFilter();

  @Test
  void doFilter_GeneratesCorrelationIdHeader_WhenMissing() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = (req, res) -> {};

    filter.doFilter(request, response, chain);

    String header = response.getHeader("X-Correlation-Id");
    assertNotNull(header);
    assertFalse(header.isBlank());
  }

  @Test
  void doFilter_PreservesCorrelationIdHeader_WhenProvided() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("X-Correlation-Id", "test-correlation-123");
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = (req, res) -> {};

    filter.doFilter(request, response, chain);

    assertEquals("test-correlation-123", response.getHeader("X-Correlation-Id"));
  }
}
