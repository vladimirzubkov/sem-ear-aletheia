package cz.cvut.ear.sem.aletheia.model.timetable;

import cz.cvut.ear.sem.aletheia.model.enrollment.Enrollment;
import cz.cvut.ear.sem.aletheia.model.enrollment.SectionTimeSlot;
import cz.cvut.ear.sem.aletheia.model.entities.AbstractEntity;
import cz.cvut.ear.sem.aletheia.model.users.Teacher;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

/**
 * Abstract base class for all section types (LectureSection, SeminarSection).
 * Contains common fields and helper methods for capacity check and enrollment management.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "section")
@Getter
@NoArgsConstructor
@ToString(exclude = {"teachers", "enrollments", "timeSlots"})
public abstract class Section extends AbstractEntity {

    @Column(nullable = false)
    private int capacity = 30;

    @ManyToMany
    @JoinTable(
            name = "section_teacher",
            joinColumns = @JoinColumn(name = "section_id"),
            inverseJoinColumns = @JoinColumn(name = "teacher_id")
    )
    private Set<Teacher> teachers = new HashSet<>();

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Enrollment> enrollments = new HashSet<>();

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SectionTimeSlot> timeSlots = new HashSet<>();

    /** Returns true if current enrollment count reached capacity. */
    public boolean isFull() {
        return enrollments.size() >= capacity;
    }

    /** Returns number of remaining free places. */
    public int getFreeSlots() {
        return capacity - enrollments.size();
    }

    /** Convenience method to add enrollment with bidirectional link. */
    public void addEnrollment(Enrollment enrollment) {
        enrollments.add(enrollment);
        enrollment.setSection(this);
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}