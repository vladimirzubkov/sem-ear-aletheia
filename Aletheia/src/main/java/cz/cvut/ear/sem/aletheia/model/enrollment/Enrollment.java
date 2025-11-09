package cz.cvut.ear.sem.aletheia.model.enrollment;

import cz.cvut.ear.sem.aletheia.model.entities.AbstractEntity;
import cz.cvut.ear.sem.aletheia.model.timetable.Section;
import cz.cvut.ear.sem.aletheia.model.users.Student;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents a student's enrollment in a specific Section (lecture or seminar).
 * Maintains bidirectional relationships with Student and Section.
 */
@Entity
@Table(name = "enrollment")
@Getter
@NoArgsConstructor
public class Enrollment extends AbstractEntity {

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @Column(nullable = false, updatable = false)
    private LocalDateTime enrolledAt = LocalDateTime.now();

    // Setters with bidirectional synchronization
    public void setStudent(Student student) {
        this.student = student;
        if (student != null && !student.getEnrollments().contains(this)) {
            student.getEnrollments().add(this);
        }
    }

    public void setSection(Section section) {
        this.section = section;
        if (section != null && !section.getEnrollments().contains(this)) {
            section.getEnrollments().add(this);
        }
    }

    public void setEnrolledAt(LocalDateTime enrolledAt) {
        this.enrolledAt = enrolledAt;
    }
}