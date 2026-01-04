package cz.cvut.ear.sem.aletheia.service;

import cz.cvut.ear.sem.aletheia.dao.*;
import cz.cvut.ear.sem.aletheia.model.timetable.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepo;
    private final SectionRepository sectionRepo;
    private final EntityManager em;

    /**
     * Creates a new course with a default lecture section.
     */
    @Transactional
    public Course createCourse(String code, String name, int credits) {
        Course course = new Course();
        course.setCode(code);
        course.setName(name);
        course.setCredits(credits);
        // Business logic: every course must have a default lecture
        LectureSection lecture = new LectureSection();
        lecture.setCapacity(100);
        course.setLecture(lecture);
        return courseRepo.save(course);
    }

    /**
     * Updates capacity. Throws exception if new capacity is too low.
     */
    @Transactional
    public void updateSectionCapacity(Long sectionId, int newCapacity) {
        Section section = sectionRepo.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found ID: %d".formatted(sectionId)));

        // Validation: cannot reduce capacity below current enrollment
        if (newCapacity < section.getEnrollments().size()) {
            throw new IllegalStateException("New capacity (%d) is lower than current enrollments".formatted(newCapacity));
        }
        section.setCapacity(newCapacity);
        sectionRepo.save(section);
    }

    /**
     * Deletes course only if there are no active enrollments.
     */
    @Transactional
    public void deleteCourse(Long id) {
        Course course = courseRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Course %d not found".formatted(id)));
        // Business logic: check if students are already enrolled before deleting
        if (course.getLecture() != null && !course.getLecture().getEnrollments().isEmpty()) {
            throw new IllegalStateException("Cannot delete course with active enrollments");
        }
        courseRepo.delete(course);
    }

    /**
     * Searches for courses using Criteria API.
     * Supports optional filtering by name (partial match) and minimum credits.
     */
    public List<Course> findCoursesByCriteria(String namePart, Integer minCredits) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Course> cq = cb.createQuery(Course.class);
        Root<Course> course = cq.from(Course.class);

        List<Predicate> predicates = new ArrayList<>();
        if (namePart != null) {
            predicates.add(cb.like(cb.lower(course.get("name")), "%%%s%%".formatted(namePart.toLowerCase())));
        }
        if (minCredits != null) {
            // Note: Field 'credits' needs to be in Course entity
            predicates.add(cb.greaterThanOrEqualTo(course.get("credits"), minCredits));
        }

        cq.where(predicates.toArray(new Predicate[0]));
        return em.createQuery(cq).getResultList();
    }

    public List<Course> findAll() {
        return courseRepo.findAll();
    }

}