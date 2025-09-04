package soat.project.lambdacredential.auth.model;

public class Staff {
    private String id;
    private String cpf;
    private String email;
    private String name;

    public Staff(String id, String cpf, String email, String name) {
        this.id = id;
        this.cpf = cpf;
        this.email = email;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getCpf() {
        return cpf;
    }
}
