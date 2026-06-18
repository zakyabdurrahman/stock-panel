package tech.zaky.stockpanel;

import atlantafx.base.theme.NordDark;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.hibernate.Session;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.jdbc.Work;
import tech.zaky.stockpanel.controllers.LoginController;
import tech.zaky.stockpanel.models.Deposit;
import tech.zaky.stockpanel.models.Holding;
import tech.zaky.stockpanel.models.ReturnRecord;
import tech.zaky.stockpanel.models.User;
import tech.zaky.stockpanel.repositories.UserRepository;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class StockPanelApplication extends Application {
    Session session;
    Connection conn;

    @Override
    public void stop() {
        session.close();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(StockPanelApplication.class.getResource("login.fxml"));
        
        //setup db session
        setupSession();
        //setup css
        Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());

        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().add(StockPanelApplication.class.getResource("appli.css").toExternalForm());

        UserRepository userRepository = new UserRepository(session);
        Navigator navigator = new Navigator(scene, session);
        LoginController loginController = fxmlLoader.getController();
        loginController.injectDependencies(userRepository, navigator);

        //ObservableList<User> users = FXCollections.observableArrayList(new User(), new User());


        stage.setTitle("StockPanel - Portfolio Manager");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public void setupSession() {
        final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .build();

        try {
            session = new MetadataSources(registry)
                    .addAnnotatedClass(Deposit.class)
                    .addAnnotatedClass(Holding.class)
                    .addAnnotatedClass(ReturnRecord.class)
                    .addAnnotatedClass(User.class)
                    .buildMetadata()
                    .buildSessionFactory()
                    .openSession();



            session.doWork(new Work() {
                @Override
                public void execute(Connection connection) throws SQLException {
                    conn = connection;
                }
            });

        } catch (Exception e) {
            System.out.println(e.getMessage());
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    public static void main(String[] args) {
        launch();

    }
}