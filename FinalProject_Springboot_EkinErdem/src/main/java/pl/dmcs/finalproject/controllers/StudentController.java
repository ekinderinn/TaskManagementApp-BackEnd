package pl.dmcs.finalproject.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.dmcs.finalproject.message.response.ResponseMessage;
import pl.dmcs.finalproject.model.Grade;
import pl.dmcs.finalproject.model.Subject;
import pl.dmcs.finalproject.model.Task;
import pl.dmcs.finalproject.model.User;
import pl.dmcs.finalproject.repository.GradeRepository;
import pl.dmcs.finalproject.repository.SubjectRepository;
import pl.dmcs.finalproject.repository.TaskRepository;
import pl.dmcs.finalproject.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private GradeRepository gradeRepository;


    @GetMapping("/allsubjects")
    @PreAuthorize("hasRole('STUDENT')")
    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    @PatchMapping("/subjects/{subject-id}") //ENROLMENT
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> enrollIntoSubject(@PathVariable("subject-id") Long subjectId, Authentication authentication) {
        String email = authentication.getName();
        User student = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student not found."));
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found."));

        if (subject.getStudents().contains(student)) {
            return new ResponseEntity<>(new ResponseMessage("Already enrolled in this subject."), HttpStatus.BAD_REQUEST);
        }

        Set<Task> tasks = subject.getTasks();
        for (Task task: tasks) {
            Grade grade = new Grade(student.getEmail());
            gradeRepository.save(grade);
            task.getStudentGrades().add(grade);
            taskRepository.save(task);
        }

        subject.getStudents().add(student);
        student.getSubjects().add(subject);
        subjectRepository.save(subject);
        userRepository.save(student);

        return new ResponseEntity<>(new ResponseMessage("Enrollment is successful!"), HttpStatus.OK);
    }
    @GetMapping("/enrolledsubjects")
    @PreAuthorize("hasRole('STUDENT')")
    public List<Subject> getEnrolledSubjects(Authentication authentication) {
        String email = authentication.getName();
        User student = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student not found."));
        List<Subject> subjects = new ArrayList<>();
        for (Subject subject: student.getSubjects()) {
            subjects.add(subject);
        }
        return subjects;
    }


    @GetMapping("/subjects/{subject-id}") //FOR SEEING THE TASKS
    @PreAuthorize("hasRole('STUDENT')")
    public Subject getSubject(@PathVariable("subject-id") Long subjectId){
        return subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found."));
    }

    @GetMapping("/subjects/{subject-id}/{task-id}")
    @PreAuthorize("hasRole('STUDENT')")
    public List<Grade> getTaskGrade(
            @PathVariable("subject-id") Long subjectId,
            @PathVariable("task-id") Long taskId,
            Authentication authentication
    ) {
        String email = authentication.getName();
        User student = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student not found."));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found."));

        if (!subject.getStudents().contains(student)) {
            throw new RuntimeException("Student is not enrolled in the subject.");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found."));

        if (!task.getSubjectId().equals(subjectId)) {
            throw new RuntimeException("Task doesn't belong to the specified subject.");
        }

        List<Grade> grades = gradeRepository.findGradesByEmail(student.getEmail());

        return grades;
    }


}
