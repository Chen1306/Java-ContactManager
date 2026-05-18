package com.contactmanager;

import com.contactmanager.model.ContactBook;
import com.contactmanager.service.StorageService;
import com.contactmanager.ui.MainWindow;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * 通讯录管理系统 - 程序入口
 * 面向对象程序设计课设
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 初始化存储服务
        StorageService storageService = new StorageService();

        // 加载通讯录数据
        ContactBook loadedBook;
        try {
            loadedBook = storageService.load();
        } catch (Exception e) {
            System.err.println("加载数据失败，使用空通讯录：" + e.getMessage());
            loadedBook = new ContactBook();
            addSampleData(loadedBook);
        }
        // 用 final 变量，使 lambda 可以引用
        final ContactBook contactBook = loadedBook;

        // 构建主界面
        MainWindow mainWindow = new MainWindow(primaryStage, contactBook, storageService);

        Scene scene = new Scene(mainWindow, 1000, 660);
        scene.getStylesheets().add(getClass().getResource("/styles/main.css") != null
                ? getClass().getResource("/styles/main.css").toExternalForm() : "");

        primaryStage.setTitle("通讯录管理系统");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(500);

        // 关闭时自动保存
        primaryStage.setOnCloseRequest(e -> {
            try {
                storageService.save(contactBook);
            } catch (Exception ex) {
                System.err.println("退出时保存失败：" + ex.getMessage());
            }
        });

        primaryStage.show();
        mainWindow.refreshAll();
    }

    /**
     * 添加示例数据（首次启动时）
     */
    private void addSampleData(ContactBook book) {
        com.contactmanager.model.Contact c1 = new com.contactmanager.model.Contact("张伟");
        c1.setMobile("13800138001"); c1.setPhone("010-12345678");
        c1.setEmail("zhangwei@example.com"); c1.setCompany("北京科技有限公司");
        c1.setBirthday("1990-03-15"); c1.addGroup("朋友"); c1.addGroup("同学");
        c1.setRemark("大学同学，现在在北京工作");
        com.contactmanager.util.PinyinUtil.updatePinyin(c1);

        com.contactmanager.model.Contact c2 = new com.contactmanager.model.Contact("李娜");
        c2.setMobile("13900139002"); c2.setEmail("lina@example.com");
        c2.setCompany("上海文化传媒"); c2.addGroup("同事");
        c2.setBirthday("1992-07-20");
        com.contactmanager.util.PinyinUtil.updatePinyin(c2);

        com.contactmanager.model.Contact c3 = new com.contactmanager.model.Contact("王芳");
        c3.setMobile("15011112222"); c3.setAddress("北京市海淀区中关村大街1号");
        c3.setZipCode("100080"); c3.addGroup("家人");
        com.contactmanager.util.PinyinUtil.updatePinyin(c3);

        com.contactmanager.model.Contact c4 = new com.contactmanager.model.Contact("陈磊");
        c4.setMobile("18800001234"); c4.setEmail("chenlei@qq.com");
        c4.setImTool("QQ"); c4.setImNumber("123456789");
        c4.addGroup("朋友"); c4.setHomepage("https://chenlei.blog");
        com.contactmanager.util.PinyinUtil.updatePinyin(c4);

        com.contactmanager.model.Contact c5 = new com.contactmanager.model.Contact("刘洋");
        c5.setMobile("13612345678"); c5.setCompany("深圳互联网公司");
        c5.addGroup("同事");
        com.contactmanager.util.PinyinUtil.updatePinyin(c5);

        book.addContact(c1);
        book.addContact(c2);
        book.addContact(c3);
        book.addContact(c4);
        book.addContact(c5);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
