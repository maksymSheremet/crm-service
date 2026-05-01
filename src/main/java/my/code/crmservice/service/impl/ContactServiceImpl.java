package my.code.crmservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.code.crmservice.database.entity.client.Client;
import my.code.crmservice.database.entity.client.Contact;
import my.code.crmservice.database.repository.client.ClientRepository;
import my.code.crmservice.database.repository.client.ContactRepository;
import my.code.crmservice.dto.request.CreateContactRequest;
import my.code.crmservice.dto.request.UpdateContactRequest;
import my.code.crmservice.dto.response.ContactResponse;
import my.code.crmservice.exception.ResourceNotFoundException;
import my.code.crmservice.mapper.ContactMapper;
import my.code.crmservice.service.ContactService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;
    private final ClientRepository clientRepository;
    private final ContactMapper contactMapper;

    @Override
    public List<ContactResponse> getContacts(UUID clientId, Long userId) {
        // Спочатку перевіряємо ownership клієнта — якщо клієнт чужий або не існує → 404
        // Тільки потім завантажуємо contacts
        validateClientOwnership(clientId, userId);
        return contactRepository.findAllByClientId(clientId).stream()
                .map(contactMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ContactResponse createContact(UUID clientId, CreateContactRequest request, Long userId) {
        // Ownership check через clientRepository: user A не може додати contact
        // до клієнта user B навіть знаючи clientId
        Client client = findClientByIdAndUserId(clientId, userId);

        Contact contact = contactMapper.fromCreateRequest(request);
        contact.setClient(client); // зв'язок встановлюється вручну (не в mapper)

        Contact saved = contactRepository.save(contact);
        log.info("Contact created: id={}, clientId={}", saved.getId(), clientId);
        return contactMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ContactResponse updateContact(UUID clientId, UUID contactId, UpdateContactRequest request, Long userId) {
        validateClientOwnership(clientId, userId);

        // Додаткова перевірка: contact належить саме цьому clientId (захист від IDOR)
        Contact contact = contactRepository.findByIdAndClientId(contactId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact", contactId));

        contactMapper.updateFromRequest(request, contact);
        Contact saved = contactRepository.save(contact);
        log.debug("Contact updated: id={}, clientId={}", contactId, clientId);
        return contactMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteContact(UUID clientId, UUID contactId, Long userId) {
        validateClientOwnership(clientId, userId);

        Contact contact = contactRepository.findByIdAndClientId(contactId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact", contactId));

        contactRepository.delete(contact);
        log.info("Contact deleted: id={}, clientId={}", contactId, clientId);
    }

    // Перевіряє ownership і кидає 404 якщо клієнт не знайдений або чужий
    private void validateClientOwnership(UUID clientId, Long userId) {
        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Client", clientId);
        }
        findClientByIdAndUserId(clientId, userId);
    }

    private Client findClientByIdAndUserId(UUID clientId, Long userId) {
        return clientRepository.findByIdAndUserId(clientId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", clientId));
    }
}
