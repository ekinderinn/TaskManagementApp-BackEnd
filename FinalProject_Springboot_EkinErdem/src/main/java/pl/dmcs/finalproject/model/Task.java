package pl.dmcs.finalproject.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private LocalDateTime deadline;

    private Long subjectId;

    @OneToMany(fetch = FetchType.EAGER)
    private Set<Grade> studentGrades = new HashSet<>();


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {this.name = name; }
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

//    public Map<User, Integer> getStudentGrades() {
//        return studentGrades;
//    }
//
//    public void setStudentGrades(Map<User, Integer> studentGrades) {
//        this.studentGrades = studentGrades;
//    }

    public void setStudentGrades(Set<Grade> studentGrades) {
        this.studentGrades = studentGrades;
    }

    public Set<Grade> getStudentGrades() {
        return studentGrades;
    }
}

