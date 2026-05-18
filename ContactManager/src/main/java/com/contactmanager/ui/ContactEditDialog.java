package com.contactmanager.ui;

import com.contactmanager.model.Contact;
import com.contactmanager.model.ContactBook;
import com.contactmanager.util.PinyinUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.*;
import javafx.scene.Scene;
import javafx.collections.FXCollections;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.*;

/**
 * 联系人编辑对话框
 * 支持新增和修改联系人
 */
public class ContactEditDialog extends Dialog<Contact> {

    private final Contact contact;
    private final ContactBook contactBook;

    // 表单字段
    private TextField nameField;
    private TextField phoneField;
    private TextField mobileField;
    private ComboBox<String> imToolCombo;
    private TextField imNumberField;
    private TextField emailField;
    private TextField homepageField;
    private TextField birthdayField;
    private TextField companyField;
    private TextField addressField;
    private TextField zipCodeField;
    private TextArea remarkArea;
    private Label photoLabel;
    private String photoPath;

    // 分组复选框
    private Map<String, CheckBox> groupCheckBoxes = new LinkedHashMap<>();

    public ContactEditDialog(ContactBook book, Contact existingContact) {
        this.contactBook = book;
        this.contact = existingContact != null ? copyContact(existingContact) : new Contact();

        setTitle(existingContact == null ? "新增联系人" : "编辑联系人");
        setHeaderText(null);
        setResizable(true);

        buildUI();
        loadData();

        // 设置按钮
        ButtonType saveBtn = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(saveBtn, cancelBtn);

        getDialogPane().setPrefWidth(580);
        getDialogPane().setPrefHeight(640);

        // 结果转换
        setResultConverter(btn -> {
            if (btn == saveBtn) {
                return collectData();
            }
            return null;
        });

        // 样式
        getDialogPane().setStyle("-fx-background-color: #f8f9fa;");
    }

    private void buildUI() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: #f8f9fa;");

        tabPane.getTabs().add(buildBasicTab());
        tabPane.getTabs().add(buildDetailTab());
        tabPane.getTabs().add(buildGroupTab());

        getDialogPane().setContent(tabPane);
    }

    private Tab buildBasicTab() {
        Tab tab = new Tab("基本信息");
        GridPane grid = createGrid();

        // 头像区域
        VBox photoBox = new VBox(8);
        photoBox.setAlignment(Pos.CENTER);
        photoLabel = new Label("暂无头像");
        photoLabel.setPrefSize(80, 80);
        photoLabel.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 40; -fx-alignment: center; -fx-font-size: 11px; -fx-text-fill: #888;");
        Button choosePhotoBtn = new Button("选择头像");
        choosePhotoBtn.setStyle("-fx-font-size: 11px; -fx-background-color: #e8f4fd; -fx-text-fill: #1a73e8; -fx-border-color: #1a73e8; -fx-border-radius: 3; -fx-background-radius: 3;");
        choosePhotoBtn.setOnAction(e -> choosePhoto());
        photoBox.getChildren().addAll(photoLabel, choosePhotoBtn);

        int row = 0;
        grid.add(photoBox, 0, row, 2, 3);

        // 右侧基本信息
        nameField = new TextField();
        addField(grid, "姓    名 *", nameField, row++, 2);
        mobileField = new TextField();
        addField(grid, "手    机", mobileField, row++, 2);
        phoneField = new TextField();
        addField(grid, "电    话", phoneField, row++, 2);

        // IM
        HBox imBox = new HBox(5);
        imToolCombo = new ComboBox<>(FXCollections.observableArrayList("QQ", "微信", "微博", "Skype", "MSN", "其他"));
        imToolCombo.setPrefWidth(90);
        imToolCombo.setPromptText("工具");
        imNumberField = new TextField();
        imNumberField.setPromptText("号码/账号");
        HBox.setHgrow(imNumberField, Priority.ALWAYS);
        imBox.getChildren().addAll(imToolCombo, imNumberField);
        addFieldNode(grid, "即时通信", imBox, row++, 2);

        emailField = new TextField();
        addField(grid, "电子邮箱", emailField, row++, 2);
        birthdayField = new TextField();
        birthdayField.setPromptText("yyyy-MM-dd");
        addField(grid, "生    日", birthdayField, row++, 2);
        companyField = new TextField();
        addField(grid, "工作单位", companyField, row++, 2);

        tab.setContent(new ScrollPane(grid));
        return tab;
    }

    private Tab buildDetailTab() {
        Tab tab = new Tab("详细信息");
        GridPane grid = createGrid();
        int row = 0;

        homepageField = new TextField();
        addField(grid, "个人主页", homepageField, row++, 0);
        addressField = new TextField();
        addField(grid, "家庭地址", addressField, row++, 0);
        zipCodeField = new TextField();
        addField(grid, "邮    编", zipCodeField, row++, 0);

        Label rl = styledLabel("备    注");
        remarkArea = new TextArea();
        remarkArea.setPrefRowCount(5);
        remarkArea.setWrapText(true);
        grid.add(rl, 0, row);
        grid.add(remarkArea, 1, row++);

        tab.setContent(new ScrollPane(grid));
        return tab;
    }

    private Tab buildGroupTab() {
        Tab tab = new Tab("所属分组");
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));

        Label hint = new Label("勾选该联系人所属的分组：");
        hint.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");
        box.getChildren().add(hint);

        for (String g : contactBook.getGroups()) {
            CheckBox cb = new CheckBox(g);
            cb.setStyle("-fx-font-size: 13px;");
            groupCheckBoxes.put(g, cb);
            box.getChildren().add(cb);
        }

        tab.setContent(new ScrollPane(box));
        return tab;
    }

    private GridPane createGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(15));
        ColumnConstraints c0 = new ColumnConstraints(80);
        ColumnConstraints c1 = new ColumnConstraints(200, 300, Double.MAX_VALUE);
        c1.setHgrow(Priority.ALWAYS);
        ColumnConstraints c2 = new ColumnConstraints(80);
        ColumnConstraints c3 = new ColumnConstraints(200, 300, Double.MAX_VALUE);
        c3.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c0, c1, c2, c3);
        return grid;
    }

    private void addField(GridPane grid, String labelText, TextField field, int row, int colStart) {
        field.setStyle("-fx-background-radius: 4; -fx-border-radius: 4; -fx-border-color: #ddd; -fx-padding: 5 8;");
        grid.add(styledLabel(labelText), colStart, row);
        grid.add(field, colStart + 1, row);
    }

    private void addFieldNode(GridPane grid, String labelText, javafx.scene.Node node, int row, int colStart) {
        grid.add(styledLabel(labelText), colStart, row);
        grid.add(node, colStart + 1, row);
    }

    private Label styledLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: #555; -fx-alignment: center-right;");
        l.setAlignment(Pos.CENTER_RIGHT);
        l.setMaxWidth(Double.MAX_VALUE);
        return l;
    }

    private void choosePhoto() {
        FileChooser fc = new FileChooser();
        fc.setTitle("选择头像图片");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("图片文件", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp"));
        File f = fc.showOpenDialog(getOwner());
        if (f != null) {
            photoPath = f.getAbsolutePath();
            try {
                Image img = new Image(f.toURI().toString(), 80, 80, true, true);
                ImageView iv = new ImageView(img);
                Circle clip = new Circle(40, 40, 40);
                iv.setClip(clip);
                photoLabel.setGraphic(iv);
                photoLabel.setText("");
            } catch (Exception ex) {
                photoLabel.setText(f.getName());
            }
        }
    }

    private void loadData() {
        if (contact.getName() != null) nameField.setText(contact.getName());
        if (contact.getPhone() != null) phoneField.setText(contact.getPhone());
        if (contact.getMobile() != null) mobileField.setText(contact.getMobile());
        if (contact.getImTool() != null) imToolCombo.setValue(contact.getImTool());
        if (contact.getImNumber() != null) imNumberField.setText(contact.getImNumber());
        if (contact.getEmail() != null) emailField.setText(contact.getEmail());
        if (contact.getHomepage() != null) homepageField.setText(contact.getHomepage());
        if (contact.getBirthday() != null) birthdayField.setText(contact.getBirthday());
        if (contact.getCompany() != null) companyField.setText(contact.getCompany());
        if (contact.getAddress() != null) addressField.setText(contact.getAddress());
        if (contact.getZipCode() != null) zipCodeField.setText(contact.getZipCode());
        if (contact.getRemark() != null) remarkArea.setText(contact.getRemark());
        photoPath = contact.getPhotoPath();
        if (photoPath != null) {
            File f = new File(photoPath);
            if (f.exists()) {
                try {
                    Image img = new Image(f.toURI().toString(), 80, 80, true, true);
                    ImageView iv = new ImageView(img);
                    Circle clip = new Circle(40, 40, 40);
                    iv.setClip(clip);
                    photoLabel.setGraphic(iv);
                    photoLabel.setText("");
                } catch (Exception ignored) {}
            }
        }
        // 分组
        if (contact.getGroups() != null) {
            for (Map.Entry<String, CheckBox> e : groupCheckBoxes.entrySet()) {
                e.getValue().setSelected(contact.isInGroup(e.getKey()));
            }
        }
    }

    private Contact collectData() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            showAlert("姓名不能为空！");
            return null;
        }
        contact.setName(name);
        contact.setPhone(emptyToNull(phoneField.getText()));
        contact.setMobile(emptyToNull(mobileField.getText()));
        contact.setImTool(imToolCombo.getValue());
        contact.setImNumber(emptyToNull(imNumberField.getText()));
        contact.setEmail(emptyToNull(emailField.getText()));
        contact.setHomepage(emptyToNull(homepageField.getText()));
        contact.setBirthday(emptyToNull(birthdayField.getText()));
        contact.setCompany(emptyToNull(companyField.getText()));
        contact.setAddress(emptyToNull(addressField.getText()));
        contact.setZipCode(emptyToNull(zipCodeField.getText()));
        contact.setRemark(emptyToNull(remarkArea.getText()));
        contact.setPhotoPath(photoPath);

        Set<String> selected = new HashSet<>();
        for (Map.Entry<String, CheckBox> e : groupCheckBoxes.entrySet()) {
            if (e.getValue().isSelected()) selected.add(e.getKey());
        }
        contact.setGroups(selected);

        PinyinUtil.updatePinyin(contact);
        return contact;
    }

    private String emptyToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }

    private Contact copyContact(Contact src) {
        Contact c = new Contact();
        c.setId(src.getId());
        c.setName(src.getName()); c.setPhone(src.getPhone());
        c.setMobile(src.getMobile()); c.setImTool(src.getImTool());
        c.setImNumber(src.getImNumber()); c.setEmail(src.getEmail());
        c.setHomepage(src.getHomepage()); c.setBirthday(src.getBirthday());
        c.setPhotoPath(src.getPhotoPath()); c.setCompany(src.getCompany());
        c.setAddress(src.getAddress()); c.setZipCode(src.getZipCode());
        c.setRemark(src.getRemark()); c.setPinyin(src.getPinyin());
        c.setPinyinInitials(src.getPinyinInitials());
        c.setGroups(new HashSet<>(src.getGroups() != null ? src.getGroups() : Collections.emptySet()));
        return c;
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
