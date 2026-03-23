package my.code.crmservice.database.repository.client;

import my.code.crmservice.database.entity.client.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NoteRepository extends JpaRepository<Note, UUID> {

    List<Note> findAllByClientIdOrderByCreatedAtDesc(UUID clientId);
}
