package tech.zaky.stockpanel.models;

import jakarta.persistence.*;
import tech.zaky.stockpanel.models.enums.ReturnType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "returns")
public class ReturnRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private ReturnType type;

    private String ticker;

    @Column(nullable = false)
    private LocalDate returnDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public ReturnType getType() { return type; }
    public void setType(ReturnType type) { this.type = type; }
    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker != null ? ticker.toUpperCase() : null; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
}
