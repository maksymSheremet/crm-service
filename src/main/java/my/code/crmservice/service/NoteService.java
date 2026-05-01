package my.code.crmservice.service;

import my.code.crmservice.dto.request.CreateNoteRequest;
import my.code.crmservice.dto.response.NoteResponse;

import java.util.List;
import java.util.UUID;

public interface NoteService {

    List<NoteResponse> getNotes(UUID clientId, Long userId);

    NoteResponse createNote(UUID clientId, CreateNoteRequest request, Long userId);
    // Немає update і delete — Note є append-only за архітектурним рішенням
}
