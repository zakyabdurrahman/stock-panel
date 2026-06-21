package tech.zaky.stockpanel.controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;
import tech.zaky.stockpanel.Navigator;
import tech.zaky.stockpanel.Screens;
import tech.zaky.stockpanel.models.Deposit;
import tech.zaky.stockpanel.models.ReturnRecord;
import tech.zaky.stockpanel.repositories.DepositRepository;
import tech.zaky.stockpanel.repositories.ReturnRecordRepository;
import tech.zaky.stockpanel.utils.UserSession;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

public class PortfolioDetailController {

    @FXML
    private LineChart<String, Number> chart;

    private Navigator navigator;
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public void injectDependencies(DepositRepository depositRepository,
                                   ReturnRecordRepository returnRecordRepository,
                                   Navigator navigator) {
        this.navigator = navigator;
        loadChart(depositRepository, returnRecordRepository);
    }

    private void loadChart(DepositRepository depositRepository, ReturnRecordRepository returnRecordRepository) {
        Long userId = UserSession.get().getId();

        //make a tree map to merge the value (add sum if the data has same date)
        TreeMap<LocalDate, BigDecimal> timeline = new TreeMap<>();
        for (Deposit d : depositRepository.findByUserId(userId))
            if (timeline.get(d.getDepositDate().toLocalDate()) == null) {
                timeline.put(d.getDepositDate().toLocalDate(), BigDecimal.ZERO.subtract(d.getAmount()));
            } else {
                timeline.merge(d.getDepositDate().toLocalDate(), d.getAmount(), BigDecimal::subtract);
            }
        for (ReturnRecord r : returnRecordRepository.findByUserId(userId))
            timeline.merge(r.getReturnDate().toLocalDate(), r.getAmount(), BigDecimal::add);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Cumulative Value");

        BigDecimal cumulative = BigDecimal.ZERO;
        for (Map.Entry<LocalDate, BigDecimal> entry : timeline.entrySet()) {
            cumulative = cumulative.add(entry.getValue());
            series.getData().add(new XYChart.Data<>(entry.getKey().format(dateFormat), cumulative));
        }



        chart.getData().add(series);

        for (XYChart.Data<String, Number> data : series.getData()) {
            BigDecimal val = new BigDecimal(data.getYValue().toString());
            String sign = val.signum() >= 0 ? "+" : "";
            Tooltip tooltip = new Tooltip(sign + val.toPlainString());
            tooltip.setShowDelay(Duration.millis(100));
            Tooltip.install(data.getNode(), tooltip);
        }


    }

    @FXML
    private void onBack() {
        navigator.navigate(Screens.DASHBOARD);
    }
}
