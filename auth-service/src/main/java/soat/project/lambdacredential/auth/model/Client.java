package soat.project.lambdacredential.auth.model;

public class Client {
    private String publicId;
    private String cpf;
    private String email;
    private String name;

    public Client(String publicId, String cpf, String email, String name) {
        this.publicId = publicId;
        this.cpf = cpf;
        this.email = email;
        this.name = name;
    }

    public String getPublicId() {
        return publicId;
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
