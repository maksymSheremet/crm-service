package my.code.crmservice.database.repository.client;

import my.code.crmservice.database.entity.client.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID>, JpaSpecificationExecutor<Client> {

    Optional<Client> findByIdAndUserId(UUID id, Long userId);

    boolean existsByEmailAndUserId(String email, Long userId);
}
