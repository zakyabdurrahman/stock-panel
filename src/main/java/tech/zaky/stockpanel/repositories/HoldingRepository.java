package tech.zaky.stockpanel.repositories;

import org.hibernate.Session;
import tech.zaky.stockpanel.models.Holding;

import java.util.List;

public class HoldingRepository {
    private final Session session;

    public HoldingRepository(Session session) {
        this.session = session;
    }

    public List<Holding> findByUserId(Long userId) {
        return session.createQuery("FROM Holding WHERE user.id = :userId", Holding.class)
                .setParameter("userId", userId)
                .list();
    }

    public void save(Holding holding) {
        session.beginTransaction();
        session.persist(holding);
        session.getTransaction().commit();
    }
}
