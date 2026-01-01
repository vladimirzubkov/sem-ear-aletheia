package cz.cvut.ear.sem.aletheia.config;

import com.github.javafaker.Faker;
import cz.cvut.ear.sem.aletheia.dao.*;
import cz.cvut.ear.sem.aletheia.model.enrollment.Enrollment;
import cz.cvut.ear.sem.aletheia.model.enrollment.SectionTimeSlot;
import cz.cvut.ear.sem.aletheia.model.timetable.*;
import cz.cvut.ear.sem.aletheia.model.users.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

@Configuration
@RequiredArgsConstructor
@Profile("!test")
public class GeneratorConfig {

    private final UserRepository userRepo;
    private final CourseRepository courseRepo;
    private final SectionRepository sectionRepo;
    private final EnrollmentRepository enrollmentRepo;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner generateData() {
        return args -> {
            // Avoid duplicates if DB is already populated
            if (userRepo.count() > 0) {
                System.out.println("Database already contains data. Skipping generation.");
                return;
            }

            Faker faker = new Faker(new Locale("en-US"));
            String commonPassword = passwordEncoder.encode("password");

            // --- 1. CREATE FIXED USERS (For login/testing) ---

            // ADMIN: Tony Stark
            Admin admin = new Admin();
            createUser(admin, "Tony", "Stark", "tony.stark@avengers.com", commonPassword);

            // SUPER ADMIN: Gandalf Grey
            SuperAdmin superAdmin = new SuperAdmin();
            createUser(superAdmin, "Gandalf", "Grey", "gandalf@middle-earth.edu", commonPassword);

            // TEACHER: Obi-Wan Kenobi
            Teacher mainTeacher = new Teacher();
            createUser(mainTeacher, "ObiWan", "Kenobi", "hello.there@jedi.org", commonPassword);

            // STUDENT: Marty McFly
            Student mainStudent = new Student();
            createUser(mainStudent, "Marty", "McFly", "marty.mcfly@hillvalley.edu", commonPassword);


            // --- 2. GENERATE RANDOM USERS ---

            // Generate 10 random students
            List<Student> randomStudents = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                Student s = new Student();
                String first = faker.name().firstName();
                String last = faker.name().lastName();
                String email = faker.internet().emailAddress();
                createUser(s, first, last, email, commonPassword);
                randomStudents.add(s);
            }

            // Generate 3 random teachers
            for (int i = 0; i < 3; i++) {
                Teacher t = new Teacher();
                String first = faker.name().firstName();
                String last = faker.name().lastName();
                String email = faker.internet().emailAddress();
                createUser(t, first, last, email, commonPassword);
            }


            // --- 3. CREATE COURSE ---
            Course course = new Course();
            course.setName("Enterprise Architectures");
            course.setCode("B6B36EAR");
            course.setCredits(6);

            LectureSection lecture = new LectureSection();
            lecture.setCapacity(150);

            // Assign Obi-Wan as lecturer
            lecture.getTeachers().add(mainTeacher);

            course.setLecture(lecture);
            courseRepo.save(course); // Cascade persists lecture

            // Add TimeSlot: Monday 9:00, Room T9:105
            addTimeSlotToSection(lecture, DayOfWeek.MONDAY, 9, "T9:105");


            // --- 4. ENROLL STUDENTS ---

            // Enroll Marty McFly
            enrollStudent(mainStudent, lecture);

            // Enroll 5 random students
            for (int i = 0; i < 5; i++) {
                enrollStudent(randomStudents.get(i), lecture);
            }

            System.out.println("==========================================");
            System.out.println("DATA GENERATED SUCCESSFULLY");
            System.out.println("Login credentials (password is 'password'):");
            System.out.println("ADMIN:       tony.stark");
            System.out.println("TEACHER:     obiwan.kenobi");
            System.out.println("STUDENT:     marty.mcfly");
            System.out.println("SUPERADMIN:  gandalf.grey");
            System.out.println("==========================================");
        };
    }

    // Helper method to create users with standardized username format (first.last)
    private void createUser(AbstractUser user, String first, String last, String email, String pwd) {
        user.setFirstName(first);
        user.setLastName(last);
        // Generate username: tony.stark (lowercase)
        user.setUsername("%s.%s".formatted(first.toLowerCase(), last.toLowerCase()));
        user.setEmail(email);
        user.setPasswordHash(pwd);
        userRepo.save(user);
    }

    private void enrollStudent(Student student, Section section) {
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setSection(section);
        enrollment.setEnrolledAt(LocalDateTime.now());

        // Maintain bidirectional consistency
        student.getEnrollments().add(enrollment);
        section.getEnrollments().add(enrollment);

        enrollmentRepo.save(enrollment);
    }

    private void addTimeSlotToSection(Section section, DayOfWeek day, int hour, String room) {
        TimeSlot ts = new TimeSlot();
        ts.setDayOfWeek(day);
        ts.setStartTime(LocalTime.of(hour, 0));
        ts.setEndTime(LocalTime.of(hour + 1, 30));

        SectionTimeSlot sts = new SectionTimeSlot();
        sts.setSection(section);
        sts.setTimeSlot(ts);
        sts.setRoom(room);

        section.getTimeSlots().add(sts);
        // Saving section cascades to SectionTimeSlot and TimeSlot (assuming CascadeType.ALL in model)
        sectionRepo.save(section);
    }
}