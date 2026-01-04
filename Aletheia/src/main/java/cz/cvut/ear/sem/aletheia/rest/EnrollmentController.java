package cz.cvut.ear.sem.aletheia.rest;

import cz.cvut.ear.sem.aletheia.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    /**
     * Enrolls the currently authenticated student into a section.
     * Delegates all logic (including entity lookup) to the service layer.
     *
     * @param authentication Security context to get current username
     * @param sectionId ID of the section to enroll into
     */
    @PostMapping("/sections/{sectionId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void enroll(Authentication authentication, @PathVariable Long sectionId) {
        // Just pass the raw data (username, ID) to the service
        enrollmentService.enroll(authentication.getName(), sectionId);
    }
}