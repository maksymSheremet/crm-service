package my.code.crmservice.database.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

@Getter
@MappedSuperclass
public abstract class AuditableEntity extends CreatedDateEntity {

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
}
