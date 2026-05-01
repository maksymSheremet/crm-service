package my.code.crmservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.code.crmservice.database.entity.client.Client;
import my.code.crmservice.database.entity.client.Note;
import my.code.crmservice.database.repository.client.ClientRepository;
import my.code.crmservice.database.repository.client.NoteRepository;
import my.code.crmservice.dto.request.CreateNoteRequest;
import my.code.crmservice.dto.response.NoteResponse;
import my.code.crmservice.exception.ResourceNotFoundException;
import my.code.crmservice.mapper.NoteMapper;
import my.code.crmservice.service.NoteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final ClientRepository clientRepository;
    private final NoteMapper noteMapper;

    @Override
    public List<NoteResponse> getNotes(UUID clientId, Long userId) {
        findClientByIdAndUserId(clientId, userId);
        // Відсортовані від нових до старих (DESC) — визначено в repository методі
        return noteRepository.findAllByClientIdOrderByCreatedAtDesc(clientId).stream()
                .map(noteMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public NoteResponse createNote(UUID clientId, CreateNoteRequest request, Long userId) {
        Client client = findClientByIdAndUserId(clientId, userId);

        Note note = noteMapper.fromCreateRequest(request);
        note.setClient(client);
        note.setAuthorId(userId); // authorId = поточний user (не з request!)

        Note saved = noteRepository.save(note);
        log.info("Note created: id={}, clientId={}, authorId={}", saved.getId(), clientId, userId);
        return noteMapper.toResponse(saved);
    }

    private Client findClientByIdAndUserId(UUID clientId, Long userId) {
        return clientRepository.findByIdAndUserId(clientId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", clientId));
    }
}
