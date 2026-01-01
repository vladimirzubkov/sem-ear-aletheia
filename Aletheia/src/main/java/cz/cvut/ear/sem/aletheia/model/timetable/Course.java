package cz.cvut.ear.sem.aletheia.model.timetable;

import cz.cvut.ear.sem.aletheia.model.entities.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table(name = "course")
@Getter @Setter @NoArgsConstructor
// Requirement: Named Queries
@NamedQuery(name = "Course.findByCode", query = "SELECT c FROM Course c WHERE c.code = :code")
public class Course extends AbstractEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int credits;

    // Requirement: Cascading
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private LectureSection lecture;

    // Requirement: Ordering
    @OrderBy("capacity DESC")
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SeminarSection> seminars = new HashSet<>();

    /**
     * Standard bidirectional helper for seminars.
     */
    public void addSeminar(SeminarSection seminar) {
        seminars.add(seminar);
        seminar.setCourse(this);
    }
}