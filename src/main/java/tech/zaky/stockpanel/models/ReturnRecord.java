package tech.zaky.stockpanel.models;

import jakarta.persistence.*;
import tech.zaky.stockpanel.models.enums.ReturnType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "returns")
public class ReturnRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = {CascadeType.PERSIST}, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private ReturnType type;

    private String ticker;

    @Column(nullable = false)
    private LocalDateTime returnDate;

    public Long getId() { return id; }
    public ReturnRecord setId(Long id) { this.id = id; return this; }
    public User getUser() { return user; }
    public ReturnRecord setUser(User user) { this.user = user; return this; }
    public BigDecimal getAmount() { return amount; }
    public ReturnRecord setAmount(BigDecimal amount) { this.amount = amount; return this; }
    public ReturnType getType() { return type; }
    public ReturnRecord setType(ReturnType type) { this.type = type; return this; }
    public String getTicker() { return ticker; }
    public ReturnRecord setTicker(String ticker) { this.ticker = ticker != null ? ticker.toUpperCase() : null; return this; }
    public LocalDateTime getReturnDate() { return returnDate; }
    public ReturnRecord setReturnDate(LocalDateTime returnDate) { this.returnDate = returnDate; return this; }
}
