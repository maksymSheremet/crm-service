package my.code.crmservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.code.crmservice.dto.request.CloseDealRequest;
import my.code.crmservice.dto.request.CreateDealRequest;
import my.code.crmservice.dto.request.UpdateDealRequest;
import my.code.crmservice.dto.response.DealResponse;
import my.code.crmservice.service.DealService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/clients/{clientId}/deals")
@RequiredArgsConstructor
public class DealController {

    private final DealService dealService;

    @GetMapping
    public ResponseEntity<List<DealResponse>> getDeals(
            Authentication authentication,
            @PathVariable UUID clientId) {

        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(dealService.getDeals(clientId, userId));
    }

    @PostMapping
    public ResponseEntity<DealResponse> createDeal(
            Authentication authentication,
            @PathVariable UUID clientId,
            @Valid @RequestBody CreateDealRequest request) {

        Long userId = extractUserId(authentication);
        DealResponse response = dealService.createDeal(clientId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{dealId}")
    public ResponseEntity<DealResponse> updateDeal(
            Authentication authentication,
            @PathVariable UUID clientId,
            @PathVariable UUID dealId,
            @Valid @RequestBody UpdateDealRequest request) {

        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(dealService.updateDeal(clientId, dealId, request, userId));
    }

    // PATCH /deals/{id}/close — явна бізнес-команда (не PUT з повним станом).
    // @PatchMapping: часткова зміна ресурсу — саме те що відбувається при close
    @PatchMapping("/{dealId}/close")
    public ResponseEntity<DealResponse> closeDeal(
            Authentication authentication,
            @PathVariable UUID clientId,
            @PathVariable UUID dealId,
            @Valid @RequestBody CloseDealRequest request) {

        Long userId = extractUserId(authentication);
        log.debug("PATCH /api/clients/{}/deals/{}/close: userId={}, status={}",
                clientId, dealId, userId, request.status());
        return ResponseEntity.ok(dealService.closeDeal(clientId, dealId, request, userId));
    }

    private Long extractUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
