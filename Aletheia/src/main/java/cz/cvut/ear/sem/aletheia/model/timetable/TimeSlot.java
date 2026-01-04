package cz.cvut.ear.sem.aletheia.model.timetable;

import cz.cvut.ear.sem.aletheia.model.entities.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Immutable reusable time slot definition.
 * Equality based on day, start and end time (business key).
 */
@Entity
@Table(name = "time_slot")
@Getter
@Setter
@NoArgsConstructor
// REMOVED @EqualsAndHashCode(callSuper = true, of = {"dayOfWeek", "startTime", "endTime"}) -> Conflict with final method in parent
@ToString(callSuper = true)
public class TimeSlot extends AbstractEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "day_of_week")
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    // Consider making fields final and initializing via all-args constructor for true immutability
}