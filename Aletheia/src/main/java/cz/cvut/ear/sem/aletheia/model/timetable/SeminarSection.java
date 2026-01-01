package cz.cvut.ear.sem.aletheia.model.timetable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "seminar_section")
@Getter
@Setter
public class SeminarSection extends Section {

    @ManyToOne
    @JoinColumn(name = "course_id")
    @JsonIgnore // Prevents infinite recursion: Course -> Seminar -> Course
    private Course course;
}