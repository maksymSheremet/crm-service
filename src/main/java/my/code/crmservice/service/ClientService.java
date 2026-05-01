package my.code.crmservice.service;

import my.code.crmservice.database.entity.client.ClientStatus;
import my.code.crmservice.dto.request.CreateClientRequest;
import my.code.crmservice.dto.request.UpdateClientRequest;
import my.code.crmservice.dto.response.ClientResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ClientService {

    // Pageable = сторінка + розмір + сортування (Spring Data)
    // status і search — nullable фільтри (null = не застосовувати)
    Page<ClientResponse> getClients(Long userId, ClientStatus status, String search, Pageable pageable);

    ClientResponse getClient(UUID id, Long userId);

    ClientResponse createClient(CreateClientRequest request, Long userId);

    ClientResponse updateClient(UUID id, UpdateClientRequest request, Long userId);

    void deleteClient(UUID id, Long userId);
}
