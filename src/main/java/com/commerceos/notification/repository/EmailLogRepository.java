package com.commerceos.notification.repository;

import com.commerceos.notification.entity.EmailLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, UUID> {}
