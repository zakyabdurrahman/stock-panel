package tech.zaky.stockpanel.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import tech.zaky.stockpanel.models.User;
import tech.zaky.stockpanel.repositories.UserRepository;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;




    private UserRepository userRepository;

    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
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
        if (user == null || !user.getPassword().equals(password)) {
            showError("Invalid username or password.");
            return;
        }

        // TODO: navigate to dashboard
    }

    @FXML
    private void onRegisterLink() {
        // TODO: navigate to register screen
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
