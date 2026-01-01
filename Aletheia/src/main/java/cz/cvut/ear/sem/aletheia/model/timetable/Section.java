package cz.cvut.ear.sem.aletheia.model.timetable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import cz.cvut.ear.sem.aletheia.model.enrollment.Enrollment;
import cz.cvut.ear.sem.aletheia.model.enrollment.SectionTimeSlot;
import cz.cvut.ear.sem.aletheia.model.users.Teacher;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
public abstract class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer capacity;

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private java.time.LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }

    @OneToMany(mappedBy = "section")
    @JsonIgnore // Prevents infinite recursion
    private List<Enrollment> enrollments = new ArrayList<>();

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL)
    private List<SectionTimeSlot> timeSlots = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "section_teacher",
            joinColumns = @JoinColumn(name = "section_id"),
            inverseJoinColumns = @JoinColumn(name = "teacher_id")
    )
    private List<Teacher> teachers = new ArrayList<>();

    /**
     * Checks if the section has reached its capacity.
     * Used by EnrollmentService.
     */
    public boolean isFull() {
        return enrollments.size() >= capacity;
    }
}