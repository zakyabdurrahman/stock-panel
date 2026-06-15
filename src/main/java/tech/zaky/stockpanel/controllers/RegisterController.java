package tech.zaky.stockpanel.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import tech.zaky.stockpanel.Navigator;
import tech.zaky.stockpanel.Screens;
import tech.zaky.stockpanel.models.User;
import tech.zaky.stockpanel.repositories.UserRepository;

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;

    private UserRepository userRepository;
    private Navigator navigator;

    public void injectDependencies(UserRepository userRepository, Navigator navigator) {
        this.userRepository = userRepository;
        this.navigator = navigator;
    }

    @FXML
    private void onRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("All fields are required.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match.");
            return;
        }

        if (userRepository.findByUsername(username) != null) {
            showError("Username already exists.");
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        userRepository.save(user);

        navigator.navigate(Screens.LOGIN);
    }

    @FXML
    private void onLoginLink() {
        navigator.navigate(Screens.LOGIN);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
