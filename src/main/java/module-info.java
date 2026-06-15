open module tech.zaky.stockpanel {
    requires javafx.fxml;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires java.sql;
    requires java.naming;
    requires atlantafx.base;


    exports tech.zaky.stockpanel;
    exports tech.zaky.stockpanel.models;
    exports tech.zaky.stockpanel.models.enums;
    exports tech.zaky.stockpanel.repositories;
    exports tech.zaky.stockpanel.controllers;
}