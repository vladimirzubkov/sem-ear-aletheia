package cz.cvut.ear.sem.aletheia.model.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import cz.cvut.ear.sem.aletheia.model.enrollment.Enrollment;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("STUDENT")
@Getter
@Setter
public class Student extends AbstractUser {

    @OneToMany(mappedBy = "student")
    @JsonIgnore // Prevents infinite recursion: Student -> Enrollment -> Student
    private List<Enrollment> enrollments = new ArrayList<>();
}