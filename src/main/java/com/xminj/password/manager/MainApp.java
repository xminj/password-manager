package com.xminj.password.manager;

import io.micronaut.context.ApplicationContext;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class MainApp extends Application {
    private static ApplicationContext ctx;

    @Override
    public void start(Stage stage) throws Exception {
        ctx = ApplicationContext.builder()
                .packages("com.xminj.password.manager")
                .mainClass(MainApp.class).start();

        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("login.fxml"));
        loader.setControllerFactory(ctx::getBean);


        Scene scene = new Scene(loader.load(), 600, 400);
        stage.setTitle("密码");
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/images/icon/locked.png")).toExternalForm()));
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        if (Objects.nonNull(ctx)) {
            ctx.stop();
        }
        super.stop();
    }
}
