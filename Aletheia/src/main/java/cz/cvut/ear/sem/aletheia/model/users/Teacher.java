package cz.cvut.ear.sem.aletheia.model.users;

import cz.cvut.ear.sem.aletheia.model.timetable.Section;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * Teacher entity.
 * Inverse side of ManyToMany with Section (owning side is Section.teachers).
 * Provides helper for safe section addition.
 */
@Entity
@Table(name = "teacher")
@Getter
@NoArgsConstructor
public class Teacher extends AbstractUser {

    @ManyToMany(mappedBy = "teachers")
    private Set<Section> teaches = new HashSet<>();

    /**
     * Adds a section to the taught set and synchronizes the owning side.
     */
    public void addSection(Section section) {
        teaches.add(section);
        section.getTeachers().add(this);
    }

    /**
     * Removes a section and keeps both sides consistent.
     */
    public void removeSection(Section section) {
        teaches.remove(section);
        section.getTeachers().remove(this);
    }
}