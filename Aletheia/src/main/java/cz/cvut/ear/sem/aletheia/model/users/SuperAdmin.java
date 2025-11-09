package cz.cvut.ear.sem.aletheia.model.users;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * SuperAdmin – highest privilege level.
 * Can manage all users including other administrators.
 */
@Entity
@Table(name = "super_admin")
@Getter
@NoArgsConstructor
public class SuperAdmin extends AbstractUser {
    // No additional fields – privileges handled by security layer
}