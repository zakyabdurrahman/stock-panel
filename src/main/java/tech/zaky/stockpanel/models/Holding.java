package tech.zaky.stockpanel.models;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "holdings")
public class Holding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    public Long getId() { return id; }
    public Holding setId(Long id) { this.id = id; return this; }
    public User getUser() { return user; }
    public Holding setUser(User user) { this.user = user; return this; }
    public BigDecimal getAmount() { return amount; }
    public Holding setAmount(BigDecimal amount) { this.amount = amount; return this; }
}
