package cz.cvut.ear.sem.aletheia.security;

import cz.cvut.ear.sem.aletheia.dao.UserRepository;
import cz.cvut.ear.sem.aletheia.model.users.AbstractUser;
import cz.cvut.ear.sem.aletheia.model.users.Admin;
import cz.cvut.ear.sem.aletheia.model.users.Student;
import cz.cvut.ear.sem.aletheia.model.users.Teacher;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AbstractUser appUser = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Определяем роль на основе класса сущности
        String role = "USER";
        if (appUser instanceof Admin) role = "ADMIN";
        else if (appUser instanceof Teacher) role = "TEACHER";
        else if (appUser instanceof Student) role = "STUDENT";

        // Возвращаем объект, который понимает Spring Security
        return User.builder()
                .username(appUser.getUsername())
                .password(appUser.getPasswordHash())
                .roles(role)
                .build();
    }
}