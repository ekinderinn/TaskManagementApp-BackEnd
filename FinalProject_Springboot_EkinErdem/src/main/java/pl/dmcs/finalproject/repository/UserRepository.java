package pl.dmcs.finalproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.dmcs.finalproject.model.Subject;
import pl.dmcs.finalproject.model.User;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
    @Query("SELECT u.subjects FROM User u WHERE u.id = :userId")
    List<Subject> findSubjectsByUserId(@Param("userId") Long userId);
}

