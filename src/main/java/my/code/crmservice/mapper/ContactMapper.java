package my.code.crmservice.mapper;

import my.code.crmservice.database.entity.client.Contact;
import my.code.crmservice.dto.request.CreateContactRequest;
import my.code.crmservice.dto.request.UpdateContactRequest;
import my.code.crmservice.dto.response.ContactResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ContactMapper {

    // Contact має @ManyToOne client — маппимо тільки client.id в clientId
    @Mapping(source = "client.id", target = "clientId")
    ContactResponse toResponse(Contact contact);

    // client встановлюється сервісом (не з request)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Contact fromCreateRequest(CreateContactRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateFromRequest(UpdateContactRequest request, @MappingTarget Contact contact);
}
