package pl.dmcs.finalproject.message.request;

import jakarta.validation.constraints.NotBlank;

public class AddSubjectForm {

    @NotBlank
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
