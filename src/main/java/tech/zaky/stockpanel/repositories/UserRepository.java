package tech.zaky.stockpanel.repositories;

import org.hibernate.Session;
import tech.zaky.stockpanel.models.User;

import java.util.List;

public class UserRepository {
    private final Session session;

    public UserRepository(Session session) {
        this.session = session;
    }

    public User findById(Long id) {
        return session.find(User.class, id);
    }

    public User findByUsername(String username) {
        return session.createQuery("FROM User WHERE username = :username", User.class)
                .setParameter("username", username)
                .uniqueResult();
    }

    public List<User> findAll() {
        return session.createQuery("FROM User", User.class).list();
    }

    public void save(User user) {
        session.beginTransaction();
        session.persist(user);
        session.getTransaction().commit();
    }

    public void update(User user) {
        session.beginTransaction();
        session.merge(user);
        session.getTransaction().commit();
    }

    public void delete(Long id) {
        session.beginTransaction();
        User deleteTarget = this.findById(id);
        session.remove(deleteTarget);
        session.getTransaction().commit();
    }
}
