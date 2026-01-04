package cz.cvut.ear.sem.aletheia.dto;

/**
 * Request object
 * Data Transfer Object for creating a new Course.
 * Contains only the fields necessary for creation (no ID).
 */
public record CourseCreateDto(
        String code,
        String name,
        int credits
) {}