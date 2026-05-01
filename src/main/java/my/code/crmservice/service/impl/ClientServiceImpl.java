package my.code.crmservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.code.crmservice.database.entity.client.Client;
import my.code.crmservice.database.entity.client.ClientStatus;
import my.code.crmservice.database.repository.client.ClientRepository;
import my.code.crmservice.database.repository.client.ClientSpecification;
import my.code.crmservice.dto.request.CreateClientRequest;
import my.code.crmservice.dto.request.UpdateClientRequest;
import my.code.crmservice.dto.response.ClientResponse;
import my.code.crmservice.exception.ResourceNotFoundException;
import my.code.crmservice.mapper.ClientMapper;
import my.code.crmservice.service.ClientService;
import my.code.crmservice.service.outbox.OutboxService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // всі методи READ за замовчуванням
public class ClientServiceImpl implements ClientService {
    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final OutboxService outboxService;

    @Override
    public Page<ClientResponse> getClients(Long userId, ClientStatus status, String search, Pageable pageable) {
        // Composable специфікації: ownedBy ЗАВЖДИ, status і search — якщо передано
        Specification<Client> spec = Specification
                .allOf(
                        ClientSpecification.ownedBy(userId),
                        ClientSpecification.hasStatus(status),
                        ClientSpecification.searchByName(search)
                );

        // findAll(Specification, Pageable) — повертає Page з метаданими (total, pages тощо)
        return clientRepository.findAll(spec, pageable)
                .map(clientMapper::toResponse);
    }

    @Override
    public ClientResponse getClient(UUID id, Long userId) {
        return clientMapper.toResponse(findByIdAndUserId(id, userId));
    }

    @Override
    @Transactional // перевизначає readOnly = true — тут потрібен повноцінний write
    public ClientResponse createClient(CreateClientRequest request, Long userId) {
        Client client = clientMapper.fromCreateRequest(request);
        // userId не в request — встановлюємо вручну з X-User-Id хедера
        client.setUserId(userId);

        Client saved = clientRepository.save(client);

        // Outbox: CLIENT_CREATED event в тій самій транзакції що й save(client).
        // Якщо Kafka впаде — event не загубиться (є в outbox таблиці)
        outboxService.publishClientCreated(saved);

        log.info("Client created: id={}, userId={}", saved.getId(), userId);
        return clientMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ClientResponse updateClient(UUID id, UpdateClientRequest request, Long userId) {
        Client client = findByIdAndUserId(id, userId);

        // PATCH: mapper з IGNORE стратегією оновлює тільки non-null поля
        clientMapper.updateFromRequest(request, client);

        // save() не обов'язковий (entity вже в persistence context і dirty checking
        // збереже зміни при commit), але явний save() краще для читабельності
        Client saved = clientRepository.save(client);
        log.debug("Client updated: id={}, userId={}", id, userId);
        return clientMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteClient(UUID id, Long userId) {
        Client client = findByIdAndUserId(id, userId);
        clientRepository.delete(client);
        log.info("Client deleted: id={}, userId={}", id, userId);
        // Примітка: contacts і notes видаляться автоматично через ON DELETE CASCADE.
        // deals — не видаляться (ON DELETE RESTRICT): якщо є deals → delete впаде.
        // Це навмисно: спочатку закрий або видали deals вручну.
    }

    // Private helper: знаходить client або кидає 404.
    // userId перевірка вбудована в запит — один SELECT замість двох
    private Client findByIdAndUserId(UUID id, Long userId) {
        return clientRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));
    }
}
