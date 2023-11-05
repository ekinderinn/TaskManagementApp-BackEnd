package pl.dmcs.finalproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dmcs.finalproject.model.Task;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    void deleteBySubjectId(Long id);
    List<Task> findBySubjectId(Long subjectId);

   Optional<Task> findById(Long taskId);
}
