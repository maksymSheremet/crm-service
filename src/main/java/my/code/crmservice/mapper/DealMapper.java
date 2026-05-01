package my.code.crmservice.mapper;

import my.code.crmservice.database.entity.deal.Deal;
import my.code.crmservice.dto.request.CreateDealRequest;
import my.code.crmservice.dto.request.UpdateDealRequest;
import my.code.crmservice.dto.response.DealResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface DealMapper {

    @Mapping(source = "client.id", target = "clientId")
    DealResponse toResponse(Deal deal);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)    // сервіс встановлює
    @Mapping(target = "status", ignore = true)    // default = OPEN (в entity)
    @Mapping(target = "closedAt", ignore = true)  // null до закриття
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    // defaultValue: якщо currency не передано — USD (узгоджується з entity default)
    @Mapping(target = "currency", defaultValue = "USD")
    Deal fromCreateRequest(CreateDealRequest request);

    // Закриття — окрема бізнес-операція через closeDeal() в сервісі,
    // тому тут тільки title/value/currency (не status, не closedAt)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "closedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateFromRequest(UpdateDealRequest request, @MappingTarget Deal deal);
}
