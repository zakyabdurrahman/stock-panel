package tech.zaky.stockpanel.repositories;

import org.hibernate.Session;
import tech.zaky.stockpanel.models.ReturnRecord;

import java.math.BigDecimal;
import java.util.List;

public class ReturnRecordRepository {
    private final Session session;

    public ReturnRecordRepository(Session session) {
        this.session = session;
    }

    public List<ReturnRecord> findByUserId(Long userId) {
        return session.createQuery("FROM ReturnRecord WHERE user.id = :userId ORDER BY returnDate DESC", ReturnRecord.class)
                .setParameter("userId", userId)
                .list();
    }

    public BigDecimal sumByUserId(Long userId) {
        BigDecimal result = session.createQuery("SELECT COALESCE(SUM(r.amount), 0) FROM ReturnRecord r WHERE r.user.id = :userId", BigDecimal.class)
                .setParameter("userId", userId)
                .uniqueResult();
        return result != null ? result : BigDecimal.ZERO;
    }

    public void save(ReturnRecord record) {
        session.beginTransaction();
        session.persist(record);
        session.getTransaction().commit();
    }

    public void delete(Long id) {
        session.beginTransaction();
        ReturnRecord r = session.find(ReturnRecord.class, id);
        if (r != null) session.remove(r);
        session.getTransaction().commit();
    }
}
