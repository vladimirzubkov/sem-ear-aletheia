package cz.cvut.ear.sem.aletheia.dao;

import cz.cvut.ear.sem.aletheia.model.enrollment.Enrollment;
import cz.cvut.ear.sem.aletheia.model.timetable.Section;
import cz.cvut.ear.sem.aletheia.model.users.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for {@link Enrollment}.
 * Contains queries for conflict detection and student timetable.
 */
@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    /**
     * Checks if a student is already enrolled in a specific section.
     */
    boolean existsByStudentAndSection(Student student, Section section);

    /**
     * Finds all enrollments for a student – used to build personal timetable.
     */
    List<Enrollment> findAllByStudentId(Long studentId);

    /**
     * Detects time conflicts for a student when trying to enroll in a new section.
     */
    @Query("""
        SELECT e FROM Enrollment e
        JOIN e.section.timeSlots sts
        JOIN sts.timeSlot ts
        WHERE e.student = :student
          AND sts.timeSlot.dayOfWeek = :dayOfWeek
          AND (
            (ts.startTime <= :newEndTime AND ts.endTime >= :newStartTime)
          )
        """)
    List<Enrollment> findConflictingEnrollments(
            @Param("student") Student student,
            @Param("dayOfWeek") java.time.DayOfWeek dayOfWeek,
            @Param("newStartTime") java.time.LocalTime newStartTime,
            @Param("newEndTime") java.time.LocalTime newEndTime
    );

    /**
     * Finds enrollments created after a specific date – useful for audit.
     */
    List<Enrollment> findAllByEnrolledAtAfter(LocalDateTime date);
}