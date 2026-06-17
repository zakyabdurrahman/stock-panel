package tech.zaky.stockpanel.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tech.zaky.stockpanel.Navigator;
import tech.zaky.stockpanel.Screens;
import tech.zaky.stockpanel.models.Deposit;
import tech.zaky.stockpanel.models.Holding;
import tech.zaky.stockpanel.models.ReturnRecord;
import tech.zaky.stockpanel.models.User;
import tech.zaky.stockpanel.models.enums.ReturnType;
import tech.zaky.stockpanel.repositories.DepositRepository;
import tech.zaky.stockpanel.repositories.HoldingRepository;
import tech.zaky.stockpanel.repositories.ReturnRecordRepository;
import tech.zaky.stockpanel.utils.NumberFormatter;
import tech.zaky.stockpanel.utils.UserSession;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Label totalDepositsLabel;
    @FXML private Label totalReturnsLabel;
    @FXML private Label netStandingLabel;
    @FXML private ListView<String> holdingsListView;
    @FXML private TableView<TransactionRow> transactionTable;
    @FXML private TableColumn<TransactionRow, String> dateColumn;
    @FXML private TableColumn<TransactionRow, String> typeColumn;
    @FXML private TableColumn<TransactionRow, String> amountColumn;
    @FXML private TableColumn<TransactionRow, String> notesColumn;
    @FXML private TableColumn<TransactionRow, Void> actionColumn;

    private DepositRepository depositRepository;
    private ReturnRecordRepository returnRecordRepository;
    private HoldingRepository holdingRepository;
    private Navigator navigator;

    public void injectDependencies(DepositRepository depositRepository,
                                   ReturnRecordRepository returnRecordRepository,
                                   HoldingRepository holdingRepository,
                                   Navigator navigator) {
        this.depositRepository = depositRepository;
        this.returnRecordRepository = returnRecordRepository;
        this.holdingRepository = holdingRepository;
        this.navigator = navigator;
        loadData();
    }

    private void loadData() {
        User user = UserSession.get();
        welcomeLabel.setText("Hello, " + user.getUsername());

        // Metrics
        BigDecimal totalDeposits =  depositRepository.sumByUserId(user.getId());
        BigDecimal totalReturns = returnRecordRepository.sumByUserId(user.getId());
        BigDecimal net = totalReturns.subtract(totalDeposits);

        totalDepositsLabel.setText("IDR " + NumberFormatter.formatBigDecimal(totalDeposits));
        totalReturnsLabel.setText("IDR " + NumberFormatter.formatBigDecimal(totalReturns));
        netStandingLabel.setText("IDR " + NumberFormatter.formatBigDecimal(net));
        netStandingLabel.setStyle("-fx-text-fill: " + (net.signum() < 0 ? "#e74c3c" : "#2ecc71") + ";");

        // Holdings sidebar
        List<String> holdingItems = new java.util.ArrayList<>();
        for (Holding h : holdingRepository.findByUserId(user.getId())) {
            holdingItems.add(h.getTicker() + " × " + h.getSharesCount().toPlainString());
        }
        holdingsListView.setItems(FXCollections.observableArrayList(holdingItems));

        // Transaction table
        dateColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().date()));
        typeColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().type()));
        amountColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().amount()));
        notesColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().details()));

        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");
            {
                deleteBtn.setStyle("-fx-text-fill: #e74c3c;");
                deleteBtn.getStyleClass().add("flat");
                deleteBtn.setOnAction(e -> {
                    TransactionRow row = getTableView().getItems().get(getIndex());
                    if ("DEPOSIT".equals(row.entityType())) depositRepository.delete(row.id());
                    else returnRecordRepository.delete(row.id());
                    loadData();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });

        List<TransactionRow> transactions = new java.util.ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Deposit d : depositRepository.findByUserId(user.getId())) {
            transactions.add(new TransactionRow(d.getId(), "DEPOSIT", d.getDepositDate().format(fmt), "DEPOSIT", "IDR " + NumberFormatter.formatBigDecimal(d.getAmount()), d.getNotes() != null ? d.getNotes() : ""));
        }
        for (ReturnRecord r : returnRecordRepository.findByUserId(user.getId())) {
            String details = (r.getTicker() != null ? r.getTicker() + " " : "") + r.getType();
            transactions.add(new TransactionRow(r.getId(), "RETURN", r.getReturnDate().format(fmt), "RETURN", "IDR " + NumberFormatter.formatBigDecimal(r.getAmount()), details));
        }
        transactionTable.setItems(FXCollections.observableArrayList(transactions));
    }

    @FXML
    private void onLogout() {
        UserSession.clear();
        navigator.navigate(Screens.LOGIN);
    }

    @FXML
    private void onAddDeposit() {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Add Capital Allocation");

        TextField amountField = new TextField();
        amountField.setPromptText("0.00");
        DatePicker datePicker = new DatePicker(LocalDate.now());
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Optional allocation notes...");
        notesArea.setPrefHeight(80);
        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("text-danger");
        errorLabel.setVisible(false);

        Button saveBtn = new Button("Save Allocation");
        saveBtn.getStyleClass().addAll("success", "raised");
        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("flat");
        cancelBtn.setOnAction(e -> modal.close());

        saveBtn.setOnAction(e -> {
            try {
                BigDecimal amount = new BigDecimal(amountField.getText().trim());
                if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new NumberFormatException();
                Deposit deposit = new Deposit()
                        .setUser(UserSession.get())
                        .setAmount(amount)
                        .setDepositDate(datePicker.getValue().atStartOfDay())
                        .setNotes(notesArea.getText().trim());
                depositRepository.save(deposit);
                modal.close();
                loadData();
            } catch (NumberFormatException ex) {
                amountField.getStyleClass().add("danger");
                errorLabel.setText("Enter a valid positive amount.");
                errorLabel.setVisible(true);
            }
        });

        VBox layout = new VBox(12, new Label("Add Capital Allocation") {{ getStyleClass().add("title-3"); }},
                amountField, datePicker, notesArea, errorLabel, new javafx.scene.layout.HBox(8, cancelBtn, saveBtn));
        layout.setPadding(new Insets(20));

        modal.setScene(new Scene(layout, 380, 340));
        modal.showAndWait();
    }

    @FXML
    private void onAddReturn() {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Add Performance Return");

        TextField tickerField = new TextField();
        tickerField.setPromptText("e.g. AAPL");
        ComboBox<ReturnType> typeCombo = new ComboBox<>(FXCollections.observableArrayList(ReturnType.values()));
        typeCombo.setValue(ReturnType.SALE);
        TextField amountField = new TextField();
        amountField.setPromptText("0.00");
        DatePicker datePicker = new DatePicker(LocalDate.now());
        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("text-danger");
        errorLabel.setVisible(false);

        Button saveBtn = new Button("Save Return");
        saveBtn.getStyleClass().addAll("accent", "raised");
        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("flat");
        cancelBtn.setOnAction(e -> modal.close());

        saveBtn.setOnAction(e -> {
            try {
                BigDecimal amount = new BigDecimal(amountField.getText().trim());
                if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new NumberFormatException();
                ReturnRecord record = new ReturnRecord()
                        .setUser(UserSession.get())
                        .setAmount(amount)
                        .setTicker(tickerField.getText().trim().toUpperCase())
                        .setType(typeCombo.getValue())
                        .setReturnDate(datePicker.getValue().atStartOfDay());
                returnRecordRepository.save(record);
                modal.close();
                loadData();
            } catch (NumberFormatException ex) {
                amountField.getStyleClass().add("danger");
                errorLabel.setText("Enter a valid positive amount.");
                errorLabel.setVisible(true);
            }
        });

        VBox layout = new VBox(12, new Label("Add Performance Return") {{ getStyleClass().add("title-3"); }},
                tickerField, typeCombo, amountField, datePicker, errorLabel, new javafx.scene.layout.HBox(8, cancelBtn, saveBtn));
        layout.setPadding(new Insets(20));

        modal.setScene(new Scene(layout, 380, 360));
        modal.showAndWait();
    }

    public record TransactionRow(Long id, String entityType, String date, String type, String amount, String details) {}
}
