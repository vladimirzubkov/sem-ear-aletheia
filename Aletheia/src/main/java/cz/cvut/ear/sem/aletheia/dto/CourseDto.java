package cz.cvut.ear.sem.aletheia.dto;

/**
 * Response object
 * Data Transfer Object for sending Course details to the client.
 * Using Java Record for immutability and conciseness.
 */
public record CourseDto(
        Long id,
        String code,
        String name,
        int credits,
        int lectureCapacity // Calculated or nested field
) {}