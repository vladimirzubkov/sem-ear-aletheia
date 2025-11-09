package cz.cvut.ear.sem.aletheia.model.users;

import cz.cvut.ear.sem.aletheia.model.enrollment.Enrollment;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * Student entity.
 * Holds bidirectional OneToMany relationship with Enrollment.
 * Provides convenience method for safe enrollment addition.
 */
@Entity
@Table(name = "student")
@Getter
@NoArgsConstructor
public class Student extends AbstractUser {

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Enrollment> enrollments = new HashSet<>();

    /**
     * Adds an enrollment and maintains bidirectional consistency.
     */
    public void addEnrollment(Enrollment enrollment) {
        enrollments.add(enrollment);
        enrollment.setStudent(this);
    }
}