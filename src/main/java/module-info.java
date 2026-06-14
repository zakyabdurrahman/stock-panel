open module tech.zaky.stockpanel {
    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires java.sql;
    requires java.naming;


    exports tech.zaky.stockpanel;
    exports tech.zaky.stockpanel.models;
    exports tech.zaky.stockpanel.models.enums;
}