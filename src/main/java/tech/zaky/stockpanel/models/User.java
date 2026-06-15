package tech.zaky.stockpanel.models;

import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @OneToMany(mappedBy = "user")
    private Set<ReturnRecord> returns;

    @Column(nullable = false)
    private String password;

    public Long getId() { return id; }
    public User setId(Long id) { this.id = id; return this; }
    public String getUsername() { return username; }
    public User setUsername(String username) { this.username = username; return this; }
    public String getPassword() { return password; }
    public User setPassword(String password) { this.password = password; return this; }
}
