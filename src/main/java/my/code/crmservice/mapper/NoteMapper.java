package my.code.crmservice.mapper;

import my.code.crmservice.database.entity.client.Note;
import my.code.crmservice.dto.request.CreateNoteRequest;
import my.code.crmservice.dto.response.NoteResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface NoteMapper {

    @Mapping(source = "client.id", target = "clientId")
    NoteResponse toResponse(Note note);

    // Note — append-only: тільки create, без update mapper
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)    // сервіс встановлює
    @Mapping(target = "authorId", ignore = true)  // береться з X-User-Id хедера
    @Mapping(target = "createdAt", ignore = true)
    Note fromCreateRequest(CreateNoteRequest request);
}
