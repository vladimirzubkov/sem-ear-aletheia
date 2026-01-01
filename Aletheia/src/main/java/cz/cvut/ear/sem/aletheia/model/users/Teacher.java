package cz.cvut.ear.sem.aletheia.model.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import cz.cvut.ear.sem.aletheia.model.timetable.Section;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("TEACHER")
@Getter
@Setter
public class Teacher extends AbstractUser {

    @ManyToMany(mappedBy = "teachers")
    @JsonIgnore // Prevents infinite recursion: Teacher -> Section -> Teacher
    private List<Section> sections = new ArrayList<>();
}