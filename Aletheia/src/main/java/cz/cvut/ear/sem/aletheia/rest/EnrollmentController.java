package cz.cvut.ear.sem.aletheia.rest;

import cz.cvut.ear.sem.aletheia.model.timetable.Section;
import cz.cvut.ear.sem.aletheia.model.users.Student;
import cz.cvut.ear.sem.aletheia.service.EnrollmentService;
import cz.cvut.ear.sem.aletheia.dao.SectionRepository;
import cz.cvut.ear.sem.aletheia.dao.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final SectionRepository sectionRepo;
    private final UserRepository userRepo;

    /**
     * Enrolls the currently authenticated student into a section.
     */
    @PostMapping("/sections/{sectionId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void enroll(Authentication authentication, @PathVariable Long sectionId) {
        // Find section or throw 404
        Section section = sectionRepo.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section %d not found".formatted(sectionId)));

        // Get student from current security context
        Student student = userRepo.findStudentByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        enrollmentService.enrollStudent(student, section);
    }
}