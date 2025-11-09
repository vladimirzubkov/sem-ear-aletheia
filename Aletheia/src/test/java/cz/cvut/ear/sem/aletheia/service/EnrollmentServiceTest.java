package cz.cvut.ear.sem.aletheia.service;

import cz.cvut.ear.sem.aletheia.model.enrollment.SectionTimeSlot;
import cz.cvut.ear.sem.aletheia.model.timetable.*;
import cz.cvut.ear.sem.aletheia.model.users.Student;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.Rollback;

import jakarta.transaction.Transactional;
import org.springframework.test.context.ActiveProfiles;

import java.time.*;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ComponentScan(basePackages = "cz.cvut.ear.sem.aletheia")
@Transactional
@Rollback
@ActiveProfiles("test")
public class EnrollmentServiceTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private EnrollmentService enrollmentService;

    private Student student;
    private Course course1;
    private LectureSection lecture1;
    private SeminarSection seminar1;
    private TimeSlot slot1;

    @BeforeEach
    void setUp() {
        student = new Student();
        student.setUsername("student1");
        student.setFirstName("Jan");
        student.setLastName("Novak");
        student.setEmail("jan@cvut.cz");
        student.setPasswordHash("xxx");
        em.persist(student);

        course1 = new Course();
        course1.setCode("B6B36EAR");
        course1.setName("Enterprise Applications");
        course1.setCredits(6);
        em.persist(course1);

        lecture1 = new LectureSection();
        lecture1.setCapacity(100);
        course1.setLecture(lecture1);
        em.persist(lecture1);

        seminar1 = new SeminarSection();
        seminar1.setCapacity(20);
        seminar1.setCourse(course1);
        em.persist(seminar1);

        slot1 = new TimeSlot();
        slot1.setDayOfWeek(DayOfWeek.MONDAY);
        slot1.setStartTime(LocalTime.of(8, 0));
        slot1.setEndTime(LocalTime.of(9, 30));
        em.persist(slot1);

        SectionTimeSlot sts = new SectionTimeSlot();
        sts.setSection(seminar1);
        sts.setTimeSlot(slot1);
        sts.setRoom("T9:301");
        em.persist(sts);

        em.flush();
    }

    // 1. Successful enrollment
    @Test
    @DisplayName("enrollStudent → student not enrolled, section has capacity → enrollment added")
    void enrollStudent_studentNotEnrolled_sectionHasCapacity_enrollmentAdded() {
        enrollmentService.enrollStudent(student, seminar1);
        assertEquals(1, student.getEnrollments().size());
        assertEquals(1, seminar1.getEnrollments().size());
    }

    // 2. Section is full
    @Test
    @DisplayName("enrollStudent → section is full → throws IllegalStateException")
    void enrollStudent_sectionIsFull_throwsIllegalStateException() {
        seminar1.setCapacity(0);
        em.flush();

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> enrollmentService.enrollStudent(student, seminar1));
        assertEquals("Section is full (ID: " + seminar1.getId() + ")", ex.getMessage());
    }

    // 3. Attempt to enroll in same section twice
    @Test
    @DisplayName("enrollStudent → attempt to enroll in same section twice → throws IllegalStateException")
    void enrollStudent_attemptToEnrollInSameSectionTwice_throwsIllegalStateException() {
        enrollmentService.enrollStudent(student, seminar1);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> enrollmentService.enrollStudent(student, seminar1));
        assertEquals("Student already enrolled in this section", ex.getMessage());
    }

    // 4. Already enrolled in lecture of same course
    @Test
    @DisplayName("enrollStudent → already enrolled in lecture of same course → throws IllegalStateException")
    void enrollStudent_alreadyEnrolledInLectureOfSameCourse_throwsIllegalStateException() {
        enrollmentService.enrollStudent(student, lecture1);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> enrollmentService.enrollStudent(student, lecture1));
        assertEquals("Student already enrolled in this section", ex.getMessage());
    }

    // 5. Already enrolled in seminar of same course
    @Test
    @DisplayName("enrollStudent → already enrolled in seminar of same course → throws IllegalStateException")
    void enrollStudent_alreadyEnrolledInSeminarOfSameCourse_throwsIllegalStateException() {
        enrollmentService.enrollStudent(student, seminar1);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> enrollmentService.enrollStudent(student, seminar1));
        assertEquals("Student already enrolled in this section", ex.getMessage());
    }

    // 6. Time conflict: seminar overlaps with another seminar
    @Test
    @DisplayName("enrollStudent → seminar overlaps with another seminar → throws IllegalStateException")
    void enrollStudent_seminarOverlapsWithAnotherSeminar_throwsIllegalStateException() {
        enrollmentService.enrollStudent(student, seminar1);

        SeminarSection seminar2 = new SeminarSection();
        seminar2.setCapacity(20);
        Course course2 = new Course();
        course2.setCode("BI-PST");
        course2.setName("Statistika");
        course2.setCredits(5);
        em.persist(course2);
        seminar2.setCourse(course2);
        em.persist(seminar2);

        SectionTimeSlot sts2 = new SectionTimeSlot();
        sts2.setSection(seminar2);
        sts2.setTimeSlot(slot1);
        sts2.setRoom("T9:999");
        em.persist(sts2);
        em.flush();

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> enrollmentService.enrollStudent(student, seminar2));
        assertEquals("Time conflict with existing enrollment", ex.getMessage());
    }

    // 7. Time conflict: lecture overlaps with seminar
    @Test
    @DisplayName("enrollStudent → lecture overlaps with seminar → throws IllegalStateException")
    void enrollStudent_lectureOverlapsWithSeminar_throwsIllegalStateException() {
        enrollmentService.enrollStudent(student, seminar1);

        LectureSection lecture2 = new LectureSection();
        lecture2.setCapacity(100);
        em.persist(lecture2);

        Course course2 = new Course();
        course2.setCode("BI-AG1");
        course2.setName("Algoritmy");
        course2.setCredits(6);
        course2.setLecture(lecture2);
        em.persist(course2);
        em.flush();

        SectionTimeSlot sts = new SectionTimeSlot();
        sts.setSection(lecture2);
        sts.setTimeSlot(slot1);
        sts.setRoom("TH:A");
        em.persist(sts);
        em.flush();

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> enrollmentService.enrollStudent(student, lecture2));
        assertEquals("Time conflict with existing enrollment", ex.getMessage());
    }

    // 8. Time conflict: any general collision
    @Test
    @DisplayName("enrollStudent → time collision with existing enrollment → throws IllegalStateException")
    void enrollStudent_timeCollisionWithExistingEnrollment_throwsIllegalStateException() {
        enrollmentService.enrollStudent(student, seminar1);

        SeminarSection seminar2 = new SeminarSection();
        seminar2.setCapacity(20);
        Course course2 = new Course();
        course2.setCode("BI-PST");
        course2.setName("Statistika");
        course2.setCredits(5);
        em.persist(course2);
        seminar2.setCourse(course2);
        em.persist(seminar2);

        SectionTimeSlot sts2 = new SectionTimeSlot();
        sts2.setSection(seminar2);
        sts2.setTimeSlot(slot1);
        sts2.setRoom("T9:999");
        em.persist(sts2);
        em.flush();

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> enrollmentService.enrollStudent(student, seminar2));
        assertEquals("Time conflict with existing enrollment", ex.getMessage());
    }

    // 9. Time conflict: two lectures from different courses at the same time
    @Test
    @DisplayName("enrollStudent → two lectures from different courses at the same time → throws IllegalStateException")
    void enrollStudent_twoLecturesFromDifferentCoursesAtSameTime_throwsIllegalStateException() {
        enrollmentService.enrollStudent(student, lecture1);

        LectureSection lecture2 = new LectureSection();
        lecture2.setCapacity(80);
        em.persist(lecture2);

        Course course2 = new Course();
        course2.setCode("BI-AG1");
        course2.setName("Algoritmy");
        course2.setCredits(6);
        course2.setLecture(lecture2);
        em.persist(course2);
        em.flush();

        SectionTimeSlot sts = new SectionTimeSlot();
        sts.setSection(lecture2);
        sts.setTimeSlot(slot1);
        sts.setRoom("TH:B-200");
        em.persist(sts);
        em.flush();

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> enrollmentService.enrollStudent(student, lecture2));
        assertEquals("Time conflict with existing enrollment", ex.getMessage());
    }

    // 10. Lecture enrollment allows seminars of the same course
    @Test
    @DisplayName("enrollStudent → enrolling in lecture automatically allows all seminars of that course")
    void enrollStudent_enrollInLecture_allowsAllSeminarsOfCourse() {
        enrollmentService.enrollStudent(student, lecture1);

        SeminarSection anotherSeminar = new SeminarSection();
        anotherSeminar.setCapacity(20);
        anotherSeminar.setCourse(course1);
        em.persist(anotherSeminar);

        SectionTimeSlot sts = new SectionTimeSlot();
        sts.setSection(anotherSeminar);
        sts.setTimeSlot(slot1);
        sts.setRoom("T9:555");
        em.persist(sts);
        em.flush();

        assertDoesNotThrow(() -> enrollmentService.enrollStudent(student, anotherSeminar));
        assertTrue(student.getEnrollments().stream()
                .anyMatch(e -> e.getSection().equals(anotherSeminar)));
    }
}