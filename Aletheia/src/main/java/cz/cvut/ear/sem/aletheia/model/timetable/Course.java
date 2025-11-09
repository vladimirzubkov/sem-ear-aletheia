package cz.cvut.ear.sem.aletheia.model.timetable;

import cz.cvut.ear.sem.aletheia.model.entities.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a university course with exactly one lecture and multiple seminars.
 * Provides helper method for safe bidirectional seminar addition.
 */
@Entity
@Table(name = "course")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"lecture", "seminars"})
public class Course extends AbstractEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int credits;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private LectureSection lecture;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SeminarSection> seminars = new HashSet<>();

    /**
     * Adds a seminar and maintains bidirectional relationship.
     */
    public void addSeminar(SeminarSection seminar) {
        seminars.add(seminar);
        seminar.setCourse(this);
    }
}