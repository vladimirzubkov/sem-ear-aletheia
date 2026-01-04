package cz.cvut.ear.sem.aletheia.rest;

import cz.cvut.ear.sem.aletheia.dto.CourseCreateDto;
import cz.cvut.ear.sem.aletheia.dto.CourseDto;
import cz.cvut.ear.sem.aletheia.model.timetable.Course;
import cz.cvut.ear.sem.aletheia.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    /**
     * Creates a new course based on the provided DTO.
     * The logic is delegated to the service layer.
     *
     * @param dto Data Transfer Object containing course details
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody CourseCreateDto dto) {
        // Pass unpacked data to the service
        courseService.createCourse(dto.code(), dto.name(), dto.credits());
    }

    @PatchMapping("/sections/{id}/capacity")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateCapacity(@PathVariable Long id, @RequestParam int capacity) {
        courseService.updateSectionCapacity(id, capacity);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        courseService.deleteCourse(id);
    }

    /**
     * Retrieves all courses and maps them to DTOs.
     * Hides internal Entity structure from the API response.
     *
     * @return List of CourseDto
     */
    @GetMapping
    public List<CourseDto> getAllCourses() {
        return courseService.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Helper method to map Course Entity to CourseDto.
     * Prevents infinite recursion issues by extracting only necessary fields.
     */
    private CourseDto toDto(Course course) {
        // Handle potential null lecture safely
        int lectureCap = (course.getLecture() != null) ? course.getLecture().getCapacity() : 0;

        return new CourseDto(
                course.getId(),
                course.getCode(),
                course.getName(),
                course.getCredits(),
                lectureCap
        );
    }
}