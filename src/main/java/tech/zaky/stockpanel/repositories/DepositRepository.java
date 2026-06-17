package tech.zaky.stockpanel.repositories;

import org.hibernate.Session;
import tech.zaky.stockpanel.models.Deposit;

import java.math.BigDecimal;
import java.util.List;

public class DepositRepository {
    private final Session session;

    public DepositRepository(Session session) {
        this.session = session;
    }

    public List<Deposit> findByUserId(Long userId) {
        return session.createQuery("FROM Deposit WHERE user.id = :userId ORDER BY depositDate DESC", Deposit.class)
                .setParameter("userId", userId)
                .list();
    }

    public BigDecimal sumByUserId(Long userId) {
        BigDecimal result = session.createQuery("SELECT COALESCE(SUM(d.amount), 0) FROM Deposit d WHERE d.user.id = :userId", BigDecimal.class)
                .setParameter("userId", userId)
                .uniqueResult();
        return result != null ? result : BigDecimal.ZERO;
    }

    public void save(Deposit deposit) {
        session.beginTransaction();
        session.persist(deposit);
        session.getTransaction().commit();
    }

    public void delete(Long id) {
        session.beginTransaction();
        Deposit d = session.find(Deposit.class, id);
        if (d != null) session.remove(d);
        session.getTransaction().commit();
    }
}
