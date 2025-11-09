package cz.cvut.ear.sem.aletheia.model.enrollment;

import cz.cvut.ear.sem.aletheia.model.entities.AbstractEntity;
import cz.cvut.ear.sem.aletheia.model.timetable.Section;
import cz.cvut.ear.sem.aletheia.model.timetable.TimeSlot;
import jakarta.persistence.*;
import lombok.*;

/**
 * Junction entity for M:N relationship between Section and TimeSlot with additional room attribute.
 * Ensures bidirectional consistency on the Section side and proper equals/hashCode based on DB identity.
 */
@Entity
@Table(name = "section_time_slot")
@Getter
@NoArgsConstructor
public class SectionTimeSlot extends AbstractEntity {

    @ManyToOne
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @ManyToOne
    @JoinColumn(name = "time_slot_id", nullable = false)
    private TimeSlot timeSlot;

    @Column(nullable = false, length = 20)
    private String room;

    /**
     * Synchronizes the bidirectional association with Section.
     */
    public void setSection(Section section) {
        this.section = section;
        if (section != null && !section.getTimeSlots().contains(this)) {
            section.getTimeSlots().add(this);
        }
    }

    /**
     * Sets the TimeSlot. No inverse synchronization needed because TimeSlot does not hold a collection of SectionTimeSlot.
     */
    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SectionTimeSlot)) return false;
        SectionTimeSlot that = (SectionTimeSlot) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : System.identityHashCode(this);
    }
}