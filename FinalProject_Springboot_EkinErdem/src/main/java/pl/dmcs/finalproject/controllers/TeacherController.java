package pl.dmcs.finalproject.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.dmcs.finalproject.message.request.AddSubjectForm;
import pl.dmcs.finalproject.message.request.AddTaskForm;
import pl.dmcs.finalproject.message.response.ResponseMessage;
import pl.dmcs.finalproject.model.Grade;
import pl.dmcs.finalproject.model.Subject;
import pl.dmcs.finalproject.model.Task;
import pl.dmcs.finalproject.model.User;
import pl.dmcs.finalproject.repository.GradeRepository;
import pl.dmcs.finalproject.repository.SubjectRepository;
import pl.dmcs.finalproject.repository.TaskRepository;
import pl.dmcs.finalproject.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private GradeRepository gradeRepository;


    @GetMapping("/subjects") //FOR ALL SUBJECTS
    @PreAuthorize("hasRole('TEACHER')")
    public List<Subject> getTeacherSubjects(Authentication authentication) {
        String email = authentication.getName();
        User teacher = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Teacher not found."));
        return subjectRepository.findByTeacherId(teacher.getId());
    }
    @GetMapping("/subjects/{subject-id}") // SEEING CHOSEN SUBJECT
    @PreAuthorize("hasRole('TEACHER')")
    public Subject getSubjectById(@PathVariable("subject-id") Long subjectId) {
        return subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found."));
    }
    @GetMapping("/subjects/{subject-id}/tasks/{task-id}") // SEEING SPECIFIC TASK DETAILS
    @PreAuthorize("hasRole('TEACHER')")
    public Task getTaskById(@PathVariable("subject-id") Long subjectId,
                            @PathVariable("task-id") Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found."));
    }

    @GetMapping("/subjects/{subject-id}/tasks/{task-id}/grades") // GETTING TASK GRADES
    @PreAuthorize("hasRole('TEACHER')")
    public Set<Grade> getTaskGrades(@PathVariable("subject-id") Long subjectId,
                                     @PathVariable("task-id") Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found."));
        return task.getStudentGrades();
    }

    @GetMapping("/students/{subject-id}")//TO SEE WHICH STUDENTS ARE ENROLLED INTO SUBJECT
    @PreAuthorize("hasRole('TEACHER')")
    public Set<User> getEnrolledStudents(@PathVariable("subject-id") Long subjectId) {
      Subject subject = subjectRepository.findById(subjectId)
               .orElseThrow(() -> new RuntimeException("Subject not found."));
       return subject.getStudents();
    }

    @PostMapping("/subjects")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> createSubject(@Valid @RequestBody AddSubjectForm addSubjectForm, Authentication authentication) {
        String email = authentication.getName();
        User teacher = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Teacher not found."));
        Subject subject = new Subject(addSubjectForm.getName(), teacher.getId());
        subjectRepository.save(subject);
        return new ResponseEntity<>(new ResponseMessage("Subject created successfully!"), HttpStatus.CREATED);
    }

    @PostMapping("/subjects/{subject-id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> createTask(@Valid @RequestBody AddTaskForm addTaskForm, Authentication authentication, @PathVariable("subject-id") Long subjectId
    ) {
        String email = authentication.getName();
        User teacher = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Teacher not found."));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found."));

        if (!subject.getTeacherId().equals(teacher.getId())) {
            throw new RuntimeException("You are not authorized to create a task for this subject.");
        }

        Task task = new Task();
        task.setName(addTaskForm.getName());
        task.setDescription(addTaskForm.getDescription());
        task.setDeadline(LocalDateTime.parse(addTaskForm.getDeadline()));
        task.setSubjectId(subjectId);

        Set<User> students = subject.getStudents();
        Set<Grade> studentGrades = new HashSet<>();
        for (User student: students) {
            Grade grade = new Grade(student.getEmail());
            studentGrades.add(grade);
            gradeRepository.save(grade);
        }
        task.setStudentGrades(studentGrades);

        taskRepository.save(task);
        subject.getTasks().add(task);
        subjectRepository.save(subject);
        return new ResponseEntity<>(new ResponseMessage("Task created successfully!"), HttpStatus.CREATED);
    }
    @PatchMapping("/subjects/{subject-id}/{task-id}/{grade-id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> giveGradeToTask(
            @PathVariable("subject-id") Long subjectId,
            @PathVariable("task-id") Long taskId,
            @PathVariable("grade-id") Long gradeId,
            @RequestBody String grade,
            Authentication authentication
    ) {
        String email = authentication.getName();
        User teacher = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Teacher not found."));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found."));

        if (!subject.getTeacherId().equals(teacher.getId())) {
            throw new RuntimeException("You are not authorized to give grades for this subject.");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found."));

        if (!task.getSubjectId().equals(subjectId)) {
            throw new RuntimeException("Task doesn't belong to the specified subject.");
        }

        Grade studentGrade = gradeRepository.findById(gradeId)
                        .orElseThrow(() -> new RuntimeException("Grade not found."));
        studentGrade.setValue(Integer.parseInt(grade));
        gradeRepository.save(studentGrade);

        taskRepository.save(task);

        return ResponseEntity.ok(new ResponseMessage("Graded."));
    }
    @DeleteMapping("/subjects/{subject-id}/{task-id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> deleteTask(
            @PathVariable("task-id") Long taskId, @PathVariable("subject-id") Long subjectId,
            Authentication authentication
    ) {
        String email = authentication.getName();
        User teacher = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Teacher not found."));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found."));

        Subject subject = subjectRepository.findById(task.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found."));

        if (!subject.getId().equals(task.getSubjectId())) {
            throw new RuntimeException("Task doesnt belong to subject.");
        }
        subject.getTasks().remove(task);
        subjectRepository.save(subject);
        taskRepository.delete(task);

        return new ResponseEntity<>(new ResponseMessage("Task deleted successfully!"), HttpStatus.NO_CONTENT);
    }

}

