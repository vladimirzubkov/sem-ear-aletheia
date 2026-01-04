package cz.cvut.ear.sem.aletheia.model.enrollment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import cz.cvut.ear.sem.aletheia.model.entities.AbstractEntity;
import cz.cvut.ear.sem.aletheia.model.timetable.Section;
import cz.cvut.ear.sem.aletheia.model.timetable.TimeSlot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class SectionTimeSlot extends AbstractEntity {

    @ManyToOne
    @JoinColumn(name = "section_id", nullable = false)
    @JsonIgnore // Prevents infinite recursion: Section -> SectionTimeSlot -> Section
    private Section section;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "time_slot_id", nullable = false)
    private TimeSlot timeSlot;

    private String room;
}