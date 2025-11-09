package cz.cvut.ear.sem.aletheia.dao;

import cz.cvut.ear.sem.aletheia.model.timetable.Section;
import cz.cvut.ear.sem.aletheia.model.timetable.SeminarSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link Section} and its subclasses.
 * Provides common queries used across the application.
 */
@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {

    /**
     * Finds all sections (lectures + seminars) taught by a specific teacher.
     */
    @Query("SELECT s FROM Section s JOIN s.teachers t WHERE t.id = :teacherId")
    List<Section> findAllByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * Finds sections with free slots for enrollment.
     */
    @Query("SELECT s FROM Section s WHERE SIZE(s.enrollments) < s.capacity")
    List<Section> findAllWithFreeSlots();

}