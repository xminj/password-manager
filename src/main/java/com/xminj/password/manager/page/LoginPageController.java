package com.xminj.password.manager.page;


import com.xminj.password.manager.MainApp;
import com.xminj.password.manager.service.KeyDerivationService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.util.StringUtils;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

@Singleton
public class LoginPageController {
    private final static Logger log = LoggerFactory.getLogger(LoginPageController.class);

    @Inject
    private KeyDerivationService keyDerivationService;
    @Inject
    private ApplicationContext applicationContext;


    @FXML
    private Label titleLabel;
    @FXML
    private Label tipLabel;
    @FXML
    private ImageView iconImage;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField settingPasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Button unlockButton;
    @FXML
    private Button settingPasswordButton;


    @FXML
    public void initialize() throws IOException {
        // 设置登录页面的图标
        iconImage.setImage(new Image(Objects.requireNonNull(getClass().getResource("/images/icon/locked.png")).toExternalForm()));

        // 登录密码输入
        passwordField.setVisible(isRegister());
        unlockButton.setVisible(isRegister());

        // 注册
        settingPasswordField.setVisible(!isRegister());
        confirmPasswordField.setVisible(!isRegister());
        settingPasswordButton.setVisible(!isRegister());

        titleLabel.setText(!isRegister() ? "设置您的密码" : "密码” 已锁定");
        tipLabel.setText(!isRegister() ? "请输入新的密码并确认。" : "输入密码解锁。");

        // 判断是否存在应用登录的密码
       /* passwordField.setVisible(false);
        unlockButton.setVisible(false);

        settingPasswordField.setVisible(true);
        confirmPasswordField.setVisible(true);
        settingPasswordButton.setVisible(true);

        titleLabel.setText("设置您的密码");*/
        // tipLabel.setText("请输入新的密码并确认。");
    }


    /**
     * 登录处理
     */
    @FXML
    protected void onUnlockClicked() {
        String password = passwordField.getText();
        log.debug("尝试解锁，输入的密码是：{}", password);

        if (!StringUtils.hasText(password)) {
            showAlert("错误", "密码不能为空", Alert.AlertType.ERROR);
            return;
        }
        String derivedKey = keyDerivationService.deriveKey(password, loadSalt());
        try {
            initDataSource(derivedKey);
        } catch (Exception e) {
            showAlert("错误", "密码错误", Alert.AlertType.ERROR);
            return;
        }
        // 跳转到主页
        mainPage();
    }


    /**
     * 设置密码
     */
    @FXML
    protected void onSetPasswordClicked() {
        String password = settingPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        log.debug("密码：{}  确认密码： {}", password, confirmPassword);

        if (!StringUtils.hasText(password)) {
            showAlert("错误", "密码不能为空", Alert.AlertType.ERROR);
            return;
        }

        if (!StringUtils.hasText(confirmPassword)) {
            showAlert("错误", "确认密码不能为空", Alert.AlertType.ERROR);
            return;
        }
        if (!password.equals(confirmPassword)) {
            showAlert("错误", "两次输入的密码不一致", Alert.AlertType.ERROR);
            return;
        }
        if (password.length() < 5) {
            showAlert("提示", "密码长度不能少于6位", Alert.AlertType.WARNING);
            return;
        }
        String derivedKey = keyDerivationService.deriveKey(password, loadSalt());
        initDataSource(derivedKey);
        // 跳转到主页
        mainPage();
    }


    private void mainPage() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("main.fxml"));

            // 设置控制器工厂，让 Micronaut 管理控制器的依赖注入
            loader.setControllerFactory(applicationContext::getBean);

            // 3. 加载场景
            Parent root = loader.load();
            Stage mainStage = new Stage();
            mainStage.setTitle("密码管理器 - 主页");
            mainStage.setScene(new Scene(root, 1000, 700));
            mainStage.setMaximized(true); // 可选：最大化
            // mainStage.getIcons().add(new Image("/icons/app-icon.png")); // 可选：设置图标

            // 5. 显示主窗口
            mainStage.show();

        } catch (Exception e) {
            log.error("打开主界面失败", e);
            throw new RuntimeException("无法加载主界面", e);
        }
        // 关闭当前登录窗口
        Stage currentStage = (Stage) passwordField.getScene().getWindow();
        currentStage.close();
    }

    /**
     * 初始化DataSource，并设置到上下文
     *
     * @param derivedKey 密码
     */
    private void initDataSource(String derivedKey) {
        HikariConfig config = new HikariConfig();
        // H2 加密数据库 URL
        config.setJdbcUrl("jdbc:h2:file:./data/password_manager;CIPHER=AES;INIT=CREATE SCHEMA IF NOT EXISTS PUBLIC");

        config.setUsername("sa"); // H2 默认用户
        config.setDriverClassName("org.h2.Driver");

        // 关键：复合密码格式 "文件密码 用户密码"
        config.setPassword(derivedKey + " sa");

        HikariDataSource hikariDataSource = new HikariDataSource(config);
        applicationContext.registerSingleton(hikariDataSource);

    }


    /**
     * 显示提示弹框
     *
     * @param title   弹框标题
     * @param message 提示内容
     * @param type    弹框类型（如：ERROR, WARNING, INFORMATION）
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null); // 不显示头部信息
        alert.setContentText(message);
        alert.showAndWait(); // 阻塞直到用户关闭弹框
    }


    /**
     * 是否注册
     *
     * @return true：不需要注册，false 需要注册
     */
    private boolean isRegister() {
        try {
            byte[] salt = Files.readAllBytes(Paths.get("./data/salt.bin"));
            return salt.length > 1;
        } catch (IOException e) {
            return false;
        }

    }

    private byte[] loadSalt() {
        File saltFile = new File("./data/salt.bin");
        if (saltFile.exists()) {
            try {
                return Files.readAllBytes(saltFile.toPath());
            } catch (IOException e) {
                log.error("读取salt.bin失败： ", e);
                throw new RuntimeException(e);
            }
        }
        byte[] salt = keyDerivationService.generateSalt();
        try {
            Files.createDirectories(saltFile.getParentFile().toPath());
            Files.write(saltFile.toPath(), salt);
            return salt;
        } catch (IOException e) {
            log.error("生出salt失败：", e);
            throw new RuntimeException(e);
        }
    }

}
