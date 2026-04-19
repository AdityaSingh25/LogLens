package com.loglens.alerting.api;

import com.loglens.alerting.domain.AlertRule;
import com.loglens.alerting.repository.AlertRuleRepository;
import com.loglens.alerting.security.TenantContextHolder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/alerting/rules")
@RequiredArgsConstructor
public class AlertRuleController {

    private final AlertRuleRepository ruleRepository;

    @GetMapping
    public ResponseEntity<List<AlertRule>> list() {
        UUID tenantId = UUID.fromString(TenantContextHolder.getTenantId());
        return ResponseEntity.ok(ruleRepository.findByTenantIdOrderByCreatedAtDesc(tenantId));
    }

    @PostMapping
    public ResponseEntity<AlertRule> create(@Valid @RequestBody AlertRuleRequest request) {
        UUID tenantId = UUID.fromString(TenantContextHolder.getTenantId());
        AlertRule rule = new AlertRule();
        rule.setTenantId(tenantId);
        rule.setName(request.name());
        rule.setCondition(request.condition());
        rule.setSeverity(request.severity());
        rule.setNotificationChannels(request.notificationChannels());
        rule.setEnabled(true);
        return ResponseEntity.ok(ruleRepository.save(rule));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AlertRule> update(@PathVariable UUID id, @RequestBody Map<String, Object> patch) {
        UUID tenantId = UUID.fromString(TenantContextHolder.getTenantId());
        AlertRule rule = ruleRepository.findById(id)
                .filter(r -> r.getTenantId().equals(tenantId))
                .orElseThrow(() -> new IllegalArgumentException("Rule not found"));

        if (patch.containsKey("enabled")) {
            rule.setEnabled((Boolean) patch.get("enabled"));
        }
        rule.setUpdatedAt(Instant.now());
        return ResponseEntity.ok(ruleRepository.save(rule));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        UUID tenantId = UUID.fromString(TenantContextHolder.getTenantId());
        ruleRepository.findById(id)
                .filter(r -> r.getTenantId().equals(tenantId))
                .ifPresent(ruleRepository::delete);
        return ResponseEntity.noContent().build();
    }

    record AlertRuleRequest(
            String name,
            Map<String, Object> condition,
            String severity,
            List<String> notificationChannels
    ) {}
}
