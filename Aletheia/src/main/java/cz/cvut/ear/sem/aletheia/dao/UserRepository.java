package cz.cvut.ear.sem.aletheia.dao;

import cz.cvut.ear.sem.aletheia.model.users.AbstractUser;
import cz.cvut.ear.sem.aletheia.model.users.Student;
import cz.cvut.ear.sem.aletheia.model.users.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Central user repository working with the inheritance hierarchy.
 * Provides natural ID lookups and role-specific queries.
 */
@Repository
public interface UserRepository extends JpaRepository<AbstractUser, Long> {

    Optional<AbstractUser> findByUsername(String username);

    Optional<AbstractUser> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    /** Role-specific convenience queries */
    @Query("SELECT s FROM Student s WHERE s.username = :username")
    Optional<Student> findStudentByUsername(@Param("username") String username);

    @Query("SELECT t FROM Teacher t WHERE t.username = :username")
    Optional<Teacher> findTeacherByUsername(@Param("username") String username);
}