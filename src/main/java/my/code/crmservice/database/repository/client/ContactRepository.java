package my.code.crmservice.database.repository.client;

import my.code.crmservice.database.entity.client.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContactRepository extends JpaRepository<Contact, UUID> {

    List<Contact> findAllByClientId(UUID clientId);

    Optional<Contact> findByIdAndClientId(UUID id, UUID clientId);

    void deleteAllByClientId(UUID clientId);
}
