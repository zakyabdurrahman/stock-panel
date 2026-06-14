package tech.zaky.stockpanel;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.hibernate.Session;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.jdbc.Work;
import tech.zaky.stockpanel.models.Deposit;
import tech.zaky.stockpanel.models.Holding;
import tech.zaky.stockpanel.models.ReturnRecord;
import tech.zaky.stockpanel.models.User;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class StockPanelApplication extends Application {
    Session session;
    Connection conn;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(StockPanelApplication.class.getResource("hello-view.fxml"));



        stage.setMaximized(true);

        //scene take a root node (like GridPane or StackPane)
        Scene scene = new Scene(fxmlLoader.load(), stage.getMaxWidth(), stage.getMaxHeight());
        setupSession();


        stage.setTitle("Hello!");
        stage.setScene(scene);
        

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

            System.out.println("session created");
            System.out.println(session);

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