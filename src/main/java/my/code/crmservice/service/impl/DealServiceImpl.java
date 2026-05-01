package my.code.crmservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.code.crmservice.database.entity.client.Client;
import my.code.crmservice.database.entity.deal.Deal;
import my.code.crmservice.database.entity.deal.DealStatus;
import my.code.crmservice.database.repository.client.ClientRepository;
import my.code.crmservice.database.repository.deal.DealRepository;
import my.code.crmservice.dto.request.CloseDealRequest;
import my.code.crmservice.dto.request.CreateDealRequest;
import my.code.crmservice.dto.request.UpdateDealRequest;
import my.code.crmservice.dto.response.DealResponse;
import my.code.crmservice.exception.InvalidOperationException;
import my.code.crmservice.exception.ResourceNotFoundException;
import my.code.crmservice.mapper.DealMapper;
import my.code.crmservice.service.DealService;
import my.code.crmservice.service.outbox.OutboxService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DealServiceImpl implements DealService {

    private final DealRepository dealRepository;
    private final ClientRepository clientRepository;
    private final DealMapper dealMapper;
    private final OutboxService outboxService;

    @Override
    public List<DealResponse> getDeals(UUID clientId, Long userId) {
        findClientByIdAndUserId(clientId, userId);
        return dealRepository.findAllByClientId(clientId).stream()
                .map(dealMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public DealResponse createDeal(UUID clientId, CreateDealRequest request, Long userId) {
        Client client = findClientByIdAndUserId(clientId, userId);

        Deal deal = dealMapper.fromCreateRequest(request);
        deal.setClient(client);

        Deal saved = dealRepository.save(deal);
        log.info("Deal created: id={}, clientId={}", saved.getId(), clientId);
        return dealMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public DealResponse updateDeal(UUID clientId, UUID dealId, UpdateDealRequest request, Long userId) {
        findClientByIdAndUserId(clientId, userId);

        Deal deal = findDealByIdAndClientId(dealId, clientId);

        // Не дозволяємо редагувати закриту угоду
        if (deal.getStatus() != DealStatus.OPEN) {
            throw new InvalidOperationException(
                    "Cannot update deal %s: already closed with status %s".formatted(dealId, deal.getStatus())
            );
        }

        dealMapper.updateFromRequest(request, deal);
        Deal saved = dealRepository.save(deal);
        log.debug("Deal updated: id={}, clientId={}", dealId, clientId);
        return dealMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public DealResponse closeDeal(UUID clientId, UUID dealId, CloseDealRequest request, Long userId) {
        findClientByIdAndUserId(clientId, userId);

        Deal deal = findDealByIdAndClientId(dealId, clientId);

        // Бізнес-правило: не можна закрити вже закриту угоду
        if (deal.getStatus() != DealStatus.OPEN) {
            throw new InvalidOperationException(
                    "Deal %s is already closed with status %s".formatted(dealId, deal.getStatus())
            );
        }

        // Бізнес-правило: статус при закритті має бути WON або LOST (не OPEN)
        if (request.status() == DealStatus.OPEN) {
            throw new InvalidOperationException("Cannot close deal with status OPEN");
        }

        Instant closedAt = Instant.now();

        // @Modifying UPDATE: один запит, без завантаження entity повністю
        dealRepository.closeDeal(dealId, request.status(), closedAt);

        // Outbox: DEAL_CLOSED event в тій самій транзакції що й closeDeal.
        // Обидва або записуються — або rollback відкочує обидва.
        deal.setStatus(request.status());
        deal.setClosedAt(closedAt);
        outboxService.publishDealClosed(deal);

        log.info("Deal closed: id={}, status={}, clientId={}", dealId, request.status(), clientId);
        return dealMapper.toResponse(deal);
    }

    private Client findClientByIdAndUserId(UUID clientId, Long userId) {
        return clientRepository.findByIdAndUserId(clientId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", clientId));
    }

    private Deal findDealByIdAndClientId(UUID dealId, UUID clientId) {
        return dealRepository.findByIdAndClientId(dealId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Deal", dealId));
    }
}
