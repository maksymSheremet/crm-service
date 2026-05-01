package my.code.crmservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.code.crmservice.dto.request.CreateContactRequest;
import my.code.crmservice.dto.request.UpdateContactRequest;
import my.code.crmservice.dto.response.ContactResponse;
import my.code.crmservice.service.ContactService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
// Nested resource: contacts завжди в контексті конкретного client
@RequestMapping("/api/clients/{clientId}/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @GetMapping
    public ResponseEntity<List<ContactResponse>> getContacts(
            Authentication authentication,
            @PathVariable UUID clientId) {

        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(contactService.getContacts(clientId, userId));
    }

    @PostMapping
    public ResponseEntity<ContactResponse> createContact(
            Authentication authentication,
            @PathVariable UUID clientId,
            @Valid @RequestBody CreateContactRequest request) {

        Long userId = extractUserId(authentication);
        ContactResponse response = contactService.createContact(clientId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{contactId}")
    public ResponseEntity<ContactResponse> updateContact(
            Authentication authentication,
            @PathVariable UUID clientId,
            @PathVariable UUID contactId,
            @Valid @RequestBody UpdateContactRequest request) {

        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(contactService.updateContact(clientId, contactId, request, userId));
    }

    @DeleteMapping("/{contactId}")
    public ResponseEntity<Void> deleteContact(
            Authentication authentication,
            @PathVariable UUID clientId,
            @PathVariable UUID contactId) {

        Long userId = extractUserId(authentication);
        contactService.deleteContact(clientId, contactId, userId);
        return ResponseEntity.noContent().build();
    }

    private Long extractUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
