package tech.zaky.stockpanel.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.mindrot.jbcrypt.BCrypt;
import tech.zaky.stockpanel.Navigator;
import tech.zaky.stockpanel.Screens;
import tech.zaky.stockpanel.models.User;
import tech.zaky.stockpanel.repositories.UserRepository;
import tech.zaky.stockpanel.utils.CryptoMachine;
import tech.zaky.stockpanel.utils.UserSession;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;




    private UserRepository userRepository;
    private Navigator navigator;

    public void injectDependencies(UserRepository userRepository, Navigator navigator) {
        this.userRepository = userRepository;
        this.navigator = navigator;
    }

    @FXML
    private void onLogin() {

        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Username and password are required.");
            return;
        }

        User user = userRepository.findByUsername(username);
        System.out.println(user);
        if (user == null || !CryptoMachine.checkPassword(password, user.getPassword())) {
            showError("Invalid username or password.");
            return;
        }


        UserSession.set(user);
        navigator.navigate(Screens.DASHBOARD);
    }

    @FXML
    private void onRegisterLink() {
        this.navigator.navigate(Screens.REGISTER);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
