package my.code.crmservice.mapper;

import my.code.crmservice.database.entity.client.Client;
import my.code.crmservice.dto.request.CreateClientRequest;
import my.code.crmservice.dto.request.UpdateClientRequest;
import my.code.crmservice.dto.response.ClientResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        // IGNORE: якщо поле в request == null — поле entity не змінюється.
        // Це забезпечує PATCH семантику в updateFromRequest.
        // ВАЖЛИВО: IGNORE стосується тільки @MappingTarget методів, не toEntity!
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ClientMapper {

    // Entity → Response: MapStruct сам маппить співпадаючі імена полів
    ClientResponse toResponse(Client client);

    // CreateRequest → Entity.
    // userId і status НЕ в request — сервіс встановлює їх сам
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)      // береться з X-User-Id хедера
    @Mapping(target = "status", ignore = true)      // default = LEAD (в entity)
    @Mapping(target = "createdAt", ignore = true)   // @CreatedDate заповнить
    @Mapping(target = "updatedAt", ignore = true)   // @LastModifiedDate заповнить
    Client fromCreateRequest(CreateClientRequest request);

    // PATCH update: оновлює тільки non-null поля завдяки IGNORE стратегії.
    // @MappingTarget — модифікує існуючий entity замість створення нового
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateFromRequest(UpdateClientRequest request, @MappingTarget Client client);
}
