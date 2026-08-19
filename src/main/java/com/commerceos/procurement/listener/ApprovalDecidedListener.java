package com.commerceos.procurement.listener;

import com.commerceos.procurement.service.ProcurementService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApprovalDecidedListener {

  private final ProcurementService procurementService;
  private final ObjectMapper objectMapper;

  @RabbitListener(queues = "procurement.approval-decided.queue")
  public void handleApprovalDecided(String message) {
    try {
      JsonNode event = objectMapper.readTree(message);
      String entityType = event.get("entityType").asText();
      String entityId = event.get("entityId").asText();
      String decision = event.get("decision").asText();

      if (!"PURCHASE_ORDER".equals(entityType)) {
        log.debug("Ignoring approval event for non-PO entity: {}", entityType);
        return;
      }

      log.info("Received workflow.approval-decided for PO {}: {}", entityId, decision);

      procurementService.updatePoStatus(
          UUID.fromString(entityId), decision, "Approval decision: " + decision, UUID.randomUUID());

      log.info("Updated PO {} status to {}", entityId, decision);
    } catch (Exception e) {
      log.error("Failed to process workflow.approval-decided event: {}", message, e);
    }
  }
}
