package my.code.crmservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.code.crmservice.database.entity.client.ClientStatus;
import my.code.crmservice.dto.request.CreateClientRequest;
import my.code.crmservice.dto.request.UpdateClientRequest;
import my.code.crmservice.dto.response.ClientResponse;
import my.code.crmservice.service.ClientService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    // GET /api/clients?status=ACTIVE&search=John&page=0&size=20&sort=name,asc
    // @PageableDefault: дефолтні значення якщо параметри не передано
    @GetMapping
    public ResponseEntity<Page<ClientResponse>> getClients(
            Authentication authentication,
            @RequestParam(required = false) ClientStatus status,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        Long userId = extractUserId(authentication);
        log.debug("GET /api/clients: userId={}, status={}, search={}", userId, status, search);
        return ResponseEntity.ok(clientService.getClients(userId, status, search, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> getClient(
            Authentication authentication,
            @PathVariable UUID id) {

        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(clientService.getClient(id, userId));
    }

    @PostMapping
    public ResponseEntity<ClientResponse> createClient(
            Authentication authentication,
            @Valid @RequestBody CreateClientRequest request) {

        Long userId = extractUserId(authentication);
        log.debug("POST /api/clients: userId={}", userId);
        ClientResponse response = clientService.createClient(request, userId);
        // 201 Created (не 200) — новий ресурс створено
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientResponse> updateClient(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateClientRequest request) {

        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(clientService.updateClient(id, request, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(
            Authentication authentication,
            @PathVariable UUID id) {

        Long userId = extractUserId(authentication);
        clientService.deleteClient(id, userId);
        // 204 No Content — успішне видалення без тіла відповіді
        return ResponseEntity.noContent().build();
    }

    // Консистентно з user-service: userId береться з SecurityContext (не з URL)
    // Підміна через URL неможлива — userId завжди від gateway
    private Long extractUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
