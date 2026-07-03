package tech.zaky.stockpanel.repositories;

import org.hibernate.Session;
import tech.zaky.stockpanel.models.Holding;

public class HoldingRepository {
    private final Session session;

    public HoldingRepository(Session session) {
        this.session = session;
    }

    public Holding findByUserId(Long userId) {
        return session.createQuery("FROM Holding WHERE user.id = :userId", Holding.class)
                .setParameter("userId", userId)
                .uniqueResult();
    }

    public void save(Holding holding) {
        session.beginTransaction();
        session.persist(holding);
        session.getTransaction().commit();
    }

    public void update(Holding holding) {
        session.beginTransaction();
        session.merge(holding);
        session.getTransaction().commit();
    }
}
