package tech.zaky.stockpanel.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "deposits")
public class Deposit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime depositDate;

    private String notes;

    public Long getId() { return id; }
    public Deposit setId(Long id) { this.id = id; return this; }
    public User getUser() { return user; }
    public Deposit setUser(User user) { this.user = user; return this; }
    public BigDecimal getAmount() { return amount; }
    public Deposit setAmount(BigDecimal amount) { this.amount = amount; return this; }
    public LocalDateTime getDepositDate() { return depositDate; }
    public Deposit setDepositDate(LocalDateTime depositDate) { this.depositDate = depositDate; return this; }
    public String getNotes() { return notes; }
    public Deposit setNotes(String notes) { this.notes = notes; return this; }
}
