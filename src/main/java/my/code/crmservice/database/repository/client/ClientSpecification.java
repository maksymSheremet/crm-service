package my.code.crmservice.database.repository.client;

import my.code.crmservice.database.entity.client.Client;
import my.code.crmservice.database.entity.client.ClientStatus;
import org.springframework.data.jpa.domain.Specification;

public class ClientSpecification {

    private ClientSpecification() {
    }

    public static Specification<Client> ownedBy(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("userId"), userId);
    }

    public static Specification<Client> hasStatus(ClientStatus status) {
        return (root, query, cb) -> status == null
                ? cb.conjunction()
                : cb.equal(root.get("status"), status);
    }

    public static Specification<Client> searchByName(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }

            String pattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("company")), pattern)
            );
        };
    }
}
