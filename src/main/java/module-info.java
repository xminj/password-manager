module com.xminj.password.manager {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires eu.hansolo.tilesfx;
    requires javafx.graphics;
    requires java.desktop;
    requires java.logging;
    requires org.slf4j;
    requires jakarta.inject;

    // requires io.micronaut.micronaut_context;
    // requires io.micronaut.micronaut_aop;
    requires java.sql;
    requires io.micronaut.micronaut_core;
    requires io.micronaut.micronaut_inject;
    requires io.micronaut.micronaut_context;

    requires io.micronaut.data.micronaut_data_jdbc;
    requires io.micronaut.sql.micronaut_jdbc_hikari;
    requires com.zaxxer.hikari;


    opens com.xminj.password.manager to javafx.fxml;
    opens com.xminj.password.manager.page to javafx.fxml,io.micronaut.micronaut_inject;
    opens com.xminj.password.manager.service to io.micronaut.micronaut_inject;

    exports com.xminj.password.manager;
    exports com.xminj.password.manager.page;
    exports com.xminj.password.manager.service;

}