package cz.cvut.ear.sem.aletheia.model.users;

import cz.cvut.ear.sem.aletheia.model.entities.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.NaturalId;

/**
 * Abstract base user with JOINED inheritance strategy.
 * Each concrete role (Student, Teacher, Admin, SuperAdmin) has its own table.
 * Username and email are natural IDs – unique across the whole system.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "app_user")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true, of = {"username", "email"})
@ToString(callSuper = true, exclude = "passwordHash")
public abstract class AbstractUser extends AbstractEntity {

    @NaturalId
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @NaturalId
    @Column(nullable = false, unique = true, length = 255)
    private String email;
}