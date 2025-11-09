package cz.cvut.ear.sem.aletheia.model.timetable;

import jakarta.persistence.*;
import lombok.*;

/**
 * Seminar/practical section – multiple per Course.
 */
@Entity
@Table(name = "seminar_section")
@Getter
@Setter
@NoArgsConstructor
public class SeminarSection extends Section {

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
}