package tech.zaky.stockpanel;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import org.hibernate.Session;
import tech.zaky.stockpanel.controllers.DashboardController;
import tech.zaky.stockpanel.controllers.LoginController;
import tech.zaky.stockpanel.controllers.PortfolioDetailController;
import tech.zaky.stockpanel.controllers.RegisterController;
import tech.zaky.stockpanel.repositories.DepositRepository;
import tech.zaky.stockpanel.repositories.HoldingRepository;
import tech.zaky.stockpanel.repositories.ReturnRecordRepository;
import tech.zaky.stockpanel.repositories.UserRepository;
import tech.zaky.stockpanel.utils.UserSession;

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
                case DASHBOARD -> loadDashboard();
                case PORTFOLIO_DETAIL -> loadPortfolioDetail();
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
        UserSession.clear();

    }

    private void loadRegister() throws IOException {
        FXMLLoader loader = new FXMLLoader(StockPanelApplication.class.getResource("register.fxml"));

        Pane root = loader.load();
        primaryScene.setRoot(root);

        RegisterController controller = loader.getController();
        controller.injectDependencies(new UserRepository(session), this);


    }

    private void loadDashboard() throws IOException {
        FXMLLoader loader = new FXMLLoader(StockPanelApplication.class.getResource("dashboard.fxml"));
        Pane root = loader.load();
        primaryScene.setRoot(root);
        DashboardController controller = loader.getController();
        controller.injectDependencies(
                new DepositRepository(session),
                new ReturnRecordRepository(session),
                new HoldingRepository(session),
                this);
    }

    private void loadPortfolioDetail() throws IOException {
        FXMLLoader loader = new FXMLLoader(StockPanelApplication.class.getResource("portfolio_detail.fxml"));
        Pane root = loader.load();
        primaryScene.setRoot(root);
        PortfolioDetailController controller = loader.getController();
        controller.injectDependencies(
                new DepositRepository(session),
                new ReturnRecordRepository(session),
                this);
    }
}
