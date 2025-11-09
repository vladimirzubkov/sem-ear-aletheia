package cz.cvut.ear.sem.aletheia.model.users;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Admin (rozvrhář) – manages courses, sections, timetables and regular users.
 */
@Entity
@Table(name = "admin_user")
@Getter
@NoArgsConstructor
public class Admin extends AbstractUser {
    // No additional fields – role-based access control
}