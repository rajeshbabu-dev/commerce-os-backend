package com.commerceos.workflow.listener;

import com.commerceos.procurement.config.RabbitMQProcurementConfig;
import com.commerceos.workflow.entity.ApprovalRequest;
import com.commerceos.workflow.service.WorkflowService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PoCreatedListener {

  private final WorkflowService workflowService;
  private final ObjectMapper objectMapper;

  @RabbitListener(queues = RabbitMQProcurementConfig.PO_CREATED_QUEUE)
  public void handlePoCreated(String message) {
    try {
      JsonNode event = objectMapper.readTree(message);
      String poId = event.get("poId").asText();
      String submittedBy = event.get("submittedBy").asText();

      log.info("Received procurement.po-created event for PO: {}", poId);

      ApprovalRequest request =
          workflowService.createApprovalRequest(
              "PURCHASE_ORDER",
              java.util.UUID.fromString(poId),
              java.util.UUID.fromString(submittedBy),
              null,
              null);

      log.info("Created approval request {} for PO {}", request.getId(), poId);
    } catch (Exception e) {
      log.error("Failed to process procurement.po-created event: {}", message, e);
    }
  }
}
