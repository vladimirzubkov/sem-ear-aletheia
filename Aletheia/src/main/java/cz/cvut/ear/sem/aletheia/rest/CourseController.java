package cz.cvut.ear.sem.aletheia.rest;

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

    // Admin-only operation in security config
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody CourseRequest request) {
        courseService.createCourse(request.code(), request.name(), request.credits());
    }

    @PatchMapping("/sections/{id}/capacity")
    public void updateCapacity(@PathVariable Long id, @RequestParam int capacity) {
        courseService.updateSectionCapacity(id, capacity);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        courseService.deleteCourse(id);
    }

    @GetMapping
    public List<Course> getAllCourses() {
        return courseService.findAll();
    }

    public record CourseRequest(String code, String name, int credits) {}
}