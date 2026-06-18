package tech.zaky.stockpanel.components;

import javafx.scene.Cursor;
import javafx.scene.control.DatePicker;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FormattedDatePicker extends DatePicker {
    public FormattedDatePicker(LocalDate localDate) {
        super(localDate);
        StringConverter<LocalDate> converter = new StringConverter<LocalDate>() {
            String pattern = "yyyy-MM-dd";
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(pattern);
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        };
        converterProperty().set(converter);
        this.setEditable(false);
        this.getEditor().setCursor(Cursor.HAND);
        this.getEditor().setOnMouseClicked(e -> {
            this.show();

        });

    }
}
