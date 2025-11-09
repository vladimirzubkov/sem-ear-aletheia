package cz.cvut.ear.sem.aletheia.service;

import cz.cvut.ear.sem.aletheia.dao.CourseRepository;
import cz.cvut.ear.sem.aletheia.dao.EnrollmentRepository;
import cz.cvut.ear.sem.aletheia.dao.SectionRepository;
import cz.cvut.ear.sem.aletheia.model.enrollment.Enrollment;
import cz.cvut.ear.sem.aletheia.model.enrollment.SectionTimeSlot;
import cz.cvut.ear.sem.aletheia.model.timetable.*;
import cz.cvut.ear.sem.aletheia.model.users.Student;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Service responsible for student enrollment logic.
 * Enforces business rules:
 * - Section capacity
 * - Time slot conflicts (ANY overlap: lecture-lecture, lecture-seminar, seminar-seminar)
 * - One lecture per course
 * - Bidirectional relationship consistency
 */
@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepo;
    private final SectionRepository sectionRepo;
    private final CourseRepository courseRepo;

    /**
     * Enrolls a student in a section with full validation.
     *
     * @param student the student to enroll
     * @param section the target section (lecture or seminar)
     * @throws IllegalStateException if any rule is violated
     */
    public void enrollStudent(Student student, Section section) {
        Objects.requireNonNull(student, "Student cannot be null");
        Objects.requireNonNull(section, "Section cannot be null");

        // 1. Check if section is full
        if (section.isFull()) {
            throw new IllegalStateException("Section is full (ID: %d)".formatted(section.getId()));
        }

        // 2. Check if already enrolled in this exact section
        if (enrollmentRepo.existsByStudentAndSection(student, section)) {
            throw new IllegalStateException("Student already enrolled in this section");
        }

        // 3. Check for ANY time conflict with existing enrollments
        if (hasTimeCollision(student, section)) {
            throw new IllegalStateException("Time conflict with existing enrollment");
        }

        // 4. One lecture per course rule
        if (section instanceof LectureSection lecture) {
            Course course = findCourseByLecture(lecture);
            boolean alreadyEnrolledInThisCourseLecture = student.getEnrollments().stream()
                    .map(Enrollment::getSection)
                    .filter(LectureSection.class::isInstance)
                    .map(LectureSection.class::cast)
                    .map(this::findCourseByLecture)
                    .anyMatch(c -> c.equals(course));

            if (alreadyEnrolledInThisCourseLecture) {
                throw new IllegalStateException("Already enrolled in lecture of course: " + course.getCode());
            }
        }

        // 5. Create enrollment with bidirectional consistency
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setSection(section);
        enrollment.setEnrolledAt(LocalDateTime.now());

        // Maintain bidirectional relationships
        student.getEnrollments().add(enrollment);
        section.getEnrollments().add(enrollment);

        enrollmentRepo.save(enrollment);
    }

    /**
     * Checks for any time conflict using clean, lazy Stream API.
     * Stops at the first overlap — production-ready and fast.
     */
    private boolean hasTimeCollision(Student student, Section newSection) {
        return student.getEnrollments().stream()
                .flatMap(e -> e.getSection().getTimeSlots().stream())
                .anyMatch(existing -> newSection.getTimeSlots().stream()
                        .anyMatch(candidate -> slotsOverlap(existing, candidate))
                );
    }

    /**
     * Checks if two time slots overlap on the same day.
     */
    private boolean slotsOverlap(SectionTimeSlot a, SectionTimeSlot b) {
        TimeSlot t1 = a.getTimeSlot();
        TimeSlot t2 = b.getTimeSlot();

        if (!t1.getDayOfWeek().equals(t2.getDayOfWeek())) {
            return false;
        }

        // [start1, end1] overlaps with [start2, end2]
        return !t1.getEndTime().isBefore(t2.getStartTime())
                && !t2.getEndTime().isBefore(t1.getStartTime());
    }

    /**
     * Safely retrieves the course associated with a lecture section.
     *
     * @throws IllegalStateException if lecture is not linked to any course
     */
    private Course findCourseByLecture(LectureSection lecture) {
        Course course = courseRepo.findByLecture(lecture);
        if (course == null) {
            throw new IllegalStateException("Lecture not linked to any course (lecture ID: " + lecture.getId() + ")");
        }
        return course;
    }
}