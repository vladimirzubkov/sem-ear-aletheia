package cz.cvut.ear.sem.aletheia.dao;

import cz.cvut.ear.sem.aletheia.model.timetable.Course;
import cz.cvut.ear.sem.aletheia.model.timetable.LectureSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link Course}.
 * Includes search by code and lecture section.
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByCode(String code);

    Course findByLecture(LectureSection lecture);

    /** Finds courses that have at least one seminar with free slots */
    @Query("""
        SELECT DISTINCT c FROM Course c
        JOIN c.seminars s
        WHERE SIZE(s.enrollments) < s.capacity
        """)
    List<Course> findAllWithAvailableSeminars();

    /** Full-text like search by code or name */
    @Query("SELECT c FROM Course c WHERE LOWER(c.code) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Course> searchByCodeOrName(@Param("query") String query);
}