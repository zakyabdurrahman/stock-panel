package tech.zaky.stockpanel.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tech.zaky.stockpanel.Navigator;
import tech.zaky.stockpanel.Screens;
import tech.zaky.stockpanel.components.FormattedDatePicker;
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
    private ObservableList<TransactionRow> transactions;

    private DepositRepository depositRepository;
    private ReturnRecordRepository returnRecordRepository;
    private HoldingRepository holdingRepository;
    private Navigator navigator;
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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


    private void applyCurrencyFormat(TextField field) {
        field.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText().replace(".", "");
            if (!newText.matches("\\d*")) return null;
            if (newText.isEmpty()) {
                change.setText("");
                change.setRange(0, change.getControlText().length());
                change.setCaretPosition(0);
                change.setAnchor(0);
                return change;
            }
            String formatted = formatWithDots(newText);
            change.setText(formatted);
            change.setRange(0, change.getControlText().length());
            change.setCaretPosition(formatted.length());
            change.setAnchor(formatted.length());
            return change;
        }));
    }

    private String formatWithDots(String digits) {
        StringBuilder sb = new StringBuilder(digits);
        for (int i = sb.length() - 3; i > 0; i -= 3) {
            sb.insert(i, '.');
        }
        return sb.toString();
    }

    private BigDecimal parseAmountField(TextField field) {
        return new BigDecimal(field.getText().replace(".", ""));
    }

    private void updateCards() {
        BigDecimal totalDepositAmount = transactions.stream()
                .filter(t -> t.entityType() == EntityType.DEPOSIT)
                .map(TransactionRow::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalReturnAmount = transactions.stream()
                .filter(t -> t.entityType() == EntityType.RETURN)
                .map(TransactionRow::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal net = totalReturnAmount.subtract(totalDepositAmount);

        totalDepositsLabel.setText("IDR " + NumberFormatter.formatBigDecimal(totalDepositAmount));
        totalReturnsLabel.setText("IDR " + NumberFormatter.formatBigDecimal(totalReturnAmount));
        netStandingLabel.setText("IDR " + NumberFormatter.formatBigDecimal(net));
        netStandingLabel.setStyle("-fx-text-fill: " + (net.signum() < 0 ? "#e74c3c" : "#2ecc71") + ";");
    }

    private void loadData() {
        User user = UserSession.get();
        welcomeLabel.setText("Hello, " + user.getUsername());

        // Holdings sidebar
        List<String> holdingItems = new java.util.ArrayList<>();
        for (Holding h : holdingRepository.findByUserId(user.getId())) {
            holdingItems.add(h.getTicker() + " × " + h.getSharesCount().toPlainString());
        }
        //holdingsListView.setItems(FXCollections.observableArrayList(holdingItems));

        // Transaction table
        dateColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().date()));
        typeColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().entityType().getString()));
        amountColumn.setCellValueFactory(c -> new SimpleStringProperty("IDR " + NumberFormatter.formatBigDecimal(c.getValue().amount())));
        notesColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().details()));

        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");
            {
                deleteBtn.setStyle("-fx-text-fill: #e74c3c;");
                deleteBtn.getStyleClass().add("flat");
                deleteBtn.setOnAction(e -> {
                    TransactionRow row = getTableView().getItems().get(getIndex());
                    if (row.entityType() == EntityType.DEPOSIT) depositRepository.delete(row.id());
                    else returnRecordRepository.delete(row.id());
                    transactions.remove(row);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });

        this.transactions = FXCollections.observableArrayList();

        for (Deposit d : depositRepository.findByUserId(user.getId())) {
            transactions.add(new TransactionRow(d.getId(), EntityType.DEPOSIT, d.getDepositDate().format(dateFormat), d.getAmount(), d.getNotes() != null ? d.getNotes() : ""));
        }
        for (ReturnRecord r : returnRecordRepository.findByUserId(user.getId())) {
            String details = (r.getTicker() != null ? r.getTicker() + " " : "") + r.getType();
            transactions.add(new TransactionRow(r.getId(), EntityType.RETURN, r.getReturnDate().format(dateFormat), r.getAmount(), details));
        }
        transactionTable.setItems(this.transactions);

        transactions.addListener(new ListChangeListener<TransactionRow>() {
            @Override
            public void onChanged(Change<? extends TransactionRow> c) {
                updateCards();
            }
        });
        updateCards();
    }

    @FXML
    private void onLogout() {
        UserSession.clear();
        navigator.navigate(Screens.LOGIN);
    }

    @FXML
    private void onViewChart() {
        navigator.navigate(Screens.PORTFOLIO_DETAIL);
    }



    @FXML
    private void onAddDeposit() {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Add Capital Allocation");

        TextField amountField = new TextField();
        amountField.setPromptText("0");
        applyCurrencyFormat(amountField);
        FormattedDatePicker datePicker = new FormattedDatePicker(LocalDate.now());

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
                BigDecimal amount = parseAmountField(amountField);
                if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new NumberFormatException();
                Deposit deposit = new Deposit()
                        .setUser(UserSession.get())
                        .setAmount(amount)
                        .setDepositDate(datePicker.getValue().atStartOfDay())
                        .setNotes(notesArea.getText().trim());
                depositRepository.save(deposit);
                modal.close();

                transactions.add(new TransactionRow(deposit.getId(), EntityType.DEPOSIT, deposit.getDepositDate().format(dateFormat), deposit.getAmount(), deposit.getNotes() != null ? deposit.getNotes() : ""));

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
        tickerField.setPromptText("e.g ADRO (Sale)");
        ComboBox<ReturnType> typeCombo = new ComboBox<>(FXCollections.observableArrayList(ReturnType.values()));
        typeCombo.setValue(ReturnType.SALE);
        TextField amountField = new TextField();
        amountField.setPromptText("0");
        applyCurrencyFormat(amountField);
        FormattedDatePicker datePicker = new FormattedDatePicker(LocalDate.now());
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
                BigDecimal amount = parseAmountField(amountField);
                if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new NumberFormatException();
                ReturnRecord record = new ReturnRecord()
                        .setUser(UserSession.get())
                        .setAmount(amount)
                        .setTicker(tickerField.getText().trim().toUpperCase())
                        .setType(typeCombo.getValue())
                        .setReturnDate(datePicker.getValue().atStartOfDay());
                returnRecordRepository.save(record);
                modal.close();
                String details = (record.getTicker() != null ? record.getTicker() + " " : "") + record.getType();
                transactions.add(new TransactionRow(record.getId(), EntityType.RETURN, record.getReturnDate().format(dateFormat), record.getAmount(), details));
            } catch (NumberFormatException ex) {
                amountField.getStyleClass().add("danger");
                errorLabel.setText("Enter a valid positive amount.");
                errorLabel.setVisible(true);
            }
        });

        VBox layout = new VBox(12, new Label("Add Performance Return") {{ getStyleClass().add("title-3"); }},
                amountField, typeCombo,  tickerField, datePicker, errorLabel, new javafx.scene.layout.HBox(8, cancelBtn, saveBtn));
        layout.setPadding(new Insets(20));

        modal.setScene(new Scene(layout, 380, 360));
        modal.showAndWait();

    }

    public record TransactionRow(Long id, EntityType entityType, String date, BigDecimal amount, String details) {}

    public enum EntityType {
        RETURN("Return"),
        DEPOSIT("Deposit");

        private final String type;

        EntityType(String type) {
            this.type = type;
        }

        public String getString() {
            return this.type;
        }
    }
}
