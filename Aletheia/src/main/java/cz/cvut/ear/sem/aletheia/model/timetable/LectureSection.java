package cz.cvut.ear.sem.aletheia.model.timetable;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

/**
 * Lecture section – exactly one per Course.
 */
@Entity
@Table(name = "lecture_section")
@Getter
@Setter
@NoArgsConstructor
public class LectureSection extends Section {
    // no extra fields
}