package pl.dmcs.finalproject.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min=3, max = 50)
    private String email;

    @NotBlank
    @Size(min=6, max = 100)
    private String password;



    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Subject> subjects = new HashSet<>();

    public User() {
    }

    public User(@NotBlank @Size(min = 3, max = 50) String email, @NotBlank @Size(min = 6, max = 100) String password) {
        this.email = email;
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public Set<Subject> getSubjects() {
        return subjects;
    }

    public void setSubjects(Set<Subject> subjects) {
        this.subjects = subjects;
    }
}
