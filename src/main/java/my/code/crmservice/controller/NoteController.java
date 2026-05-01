package my.code.crmservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.code.crmservice.dto.request.CreateNoteRequest;
import my.code.crmservice.dto.response.NoteResponse;
import my.code.crmservice.service.NoteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/clients/{clientId}/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @GetMapping
    public ResponseEntity<List<NoteResponse>> getNotes(
            Authentication authentication,
            @PathVariable UUID clientId) {

        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(noteService.getNotes(clientId, userId));
    }

    // Тільки POST — Note є append-only (немає PUT/DELETE ендпоінтів)
    @PostMapping
    public ResponseEntity<NoteResponse> createNote(
            Authentication authentication,
            @PathVariable UUID clientId,
            @Valid @RequestBody CreateNoteRequest request) {

        Long userId = extractUserId(authentication);
        NoteResponse response = noteService.createNote(clientId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private Long extractUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
