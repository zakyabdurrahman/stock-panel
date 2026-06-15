package tech.zaky.stockpanel;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import org.hibernate.Session;
import tech.zaky.stockpanel.controllers.LoginController;
import tech.zaky.stockpanel.controllers.RegisterController;
import tech.zaky.stockpanel.repositories.UserRepository;

import java.io.IOException;

public class Navigator {
    private final Scene primaryScene;
    private final Session session;

    public Navigator(Scene primaryScene, Session session) {
        this.primaryScene = primaryScene;
        this.session = session;
    }

    public void navigate(Screens screen) {
        try {
            switch (screen) {
                case LOGIN -> loadLogin();
                case REGISTER -> loadRegister();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadLogin() throws IOException {
        FXMLLoader loader = new FXMLLoader(StockPanelApplication.class.getResource("login.fxml"));
        Pane root = loader.load();
        primaryScene.setRoot(root);
        LoginController controller = loader.getController();
        controller.injectDependencies(new UserRepository(session), this);

    }

    private void loadRegister() throws IOException {
        FXMLLoader loader = new FXMLLoader(StockPanelApplication.class.getResource("register.fxml"));

        Pane root = loader.load();
        primaryScene.setRoot(root);

        RegisterController controller = loader.getController();
        controller.injectDependencies(new UserRepository(session), this);


    }
}
