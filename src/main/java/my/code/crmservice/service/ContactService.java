package my.code.crmservice.service;

import my.code.crmservice.dto.request.CreateContactRequest;
import my.code.crmservice.dto.request.UpdateContactRequest;
import my.code.crmservice.dto.response.ContactResponse;

import java.util.List;
import java.util.UUID;

public interface ContactService {

    List<ContactResponse> getContacts(UUID clientId, Long userId);

    ContactResponse createContact(UUID clientId, CreateContactRequest request, Long userId);

    ContactResponse updateContact(UUID clientId, UUID contactId, UpdateContactRequest request, Long userId);

    void deleteContact(UUID clientId, UUID contactId, Long userId);
}
