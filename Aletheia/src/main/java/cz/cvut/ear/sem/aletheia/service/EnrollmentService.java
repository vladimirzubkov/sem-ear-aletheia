package cz.cvut.ear.sem.aletheia.service;

import cz.cvut.ear.sem.aletheia.dao.CourseRepository;
import cz.cvut.ear.sem.aletheia.dao.EnrollmentRepository;
import cz.cvut.ear.sem.aletheia.dao.SectionRepository;
import cz.cvut.ear.sem.aletheia.dao.UserRepository; // Added this import
import cz.cvut.ear.sem.aletheia.model.enrollment.Enrollment;
import cz.cvut.ear.sem.aletheia.model.enrollment.SectionTimeSlot;
import cz.cvut.ear.sem.aletheia.model.timetable.*;
import cz.cvut.ear.sem.aletheia.model.users.Student;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Service responsible for student enrollment logic.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepo;
    private final SectionRepository sectionRepo;
    private final CourseRepository courseRepo;
    private final UserRepository userRepo; // Injected to find student

    /**
     * Facade method for controllers. Finds entities by ID/Username and delegates to logic.
     * * @param username The username of the student
     * @param sectionId The ID of the section
     */
    public void enroll(String username, Long sectionId) {
        Section section = sectionRepo.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section %d not found".formatted(sectionId)));

        Student student = userRepo.findStudentByUsername(username)
                .orElseThrow(() -> new RuntimeException("Student not found: " + username));

        enrollStudent(student, section);
    }

    /**
     * Core business logic for enrollment.
     */
    public void enrollStudent(Student student, Section section) {
        Objects.requireNonNull(student, "Student cannot be null");
        Objects.requireNonNull(section, "Section cannot be null");

        // 1. Check Capacity
        if (section.isFull()) {
            throw new IllegalStateException("Section is full (ID: %d)".formatted(section.getId()));
        }

        // 2. Check if already enrolled in this exact section
        if (enrollmentRepo.existsByStudentAndSection(student, section)) {
            throw new IllegalStateException("Student already enrolled in this section");
        }

        // 3. Check for Time Conflicts
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

        // 5. Create enrollment
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setSection(section);
        enrollment.setEnrolledAt(LocalDateTime.now());

        enrollmentRepo.save(enrollment);
    }

    // the rest of private methods: hasTimeCollision, slotsOverlap, findCourseByLecture

    private boolean hasTimeCollision(Student student, Section newSection) {
        return student.getEnrollments().stream()
                .flatMap(e -> e.getSection().getTimeSlots().stream())
                .anyMatch(existing -> newSection.getTimeSlots().stream()
                        .anyMatch(candidate -> slotsOverlap(existing, candidate))
                );
    }

    private boolean slotsOverlap(SectionTimeSlot a, SectionTimeSlot b) {
        TimeSlot t1 = a.getTimeSlot();
        TimeSlot t2 = b.getTimeSlot();

        if (!t1.getDayOfWeek().equals(t2.getDayOfWeek())) {
            return false;
        }
        return t1.getStartTime().isBefore(t2.getEndTime()) &&
                t1.getEndTime().isAfter(t2.getStartTime());
    }

    private Course findCourseByLecture(LectureSection lecture) {
        Course course = courseRepo.findByLecture(lecture);
        if (course == null) {
            throw new IllegalStateException("Lecture not linked to any course (lecture ID: " + lecture.getId() + ")");
        }
        return course;
    }
}