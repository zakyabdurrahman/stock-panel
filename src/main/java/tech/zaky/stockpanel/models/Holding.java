package tech.zaky.stockpanel.models;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "holdings", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "ticker"})})
public class Holding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String ticker;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal sharesCount;

    public Long getId() { return id; }
    public Holding setId(Long id) { this.id = id; return this; }
    public User getUser() { return user; }
    public Holding setUser(User user) { this.user = user; return this; }
    public String getTicker() { return ticker; }
    public Holding setTicker(String ticker) { this.ticker = ticker; return this; }
    public BigDecimal getSharesCount() { return sharesCount; }
    public Holding setSharesCount(BigDecimal sharesCount) { this.sharesCount = sharesCount; return this; }
}
