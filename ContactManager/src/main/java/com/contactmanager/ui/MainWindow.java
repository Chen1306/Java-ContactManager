package com.contactmanager.ui;

import com.contactmanager.model.Contact;
import com.contactmanager.model.ContactBook;
import com.contactmanager.service.StorageService;
import com.contactmanager.util.PinyinUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.*;
import javafx.stage.*;
import java.io.File;
import java.util.*;

/**
 * 通讯录主界面
 * 左侧：分组列表  右侧：联系人列表 + 搜索
 */
public class MainWindow extends BorderPane {

    private final ContactBook contactBook;
    private final StorageService storageService;
    private final Stage stage;

    // 左侧分组
    private ListView<String> groupListView;
    private ObservableList<String> groupItems;

    // 右侧联系人列表
    private TableView<Contact> contactTable;
    private ObservableList<Contact> contactItems;

    // 搜索框
    private TextField searchField;

    // 状态栏
    private Label statusLabel;

    // 当前选中分组
    private String currentGroup = "全部联系人";

    public MainWindow(Stage stage, ContactBook book, StorageService storage) {
        this.stage = stage;
        this.contactBook = book;
        this.storageService = storage;

        buildUI();
        refreshAll();
    }

    // ==================== UI 构建 ====================

    private void buildUI() {
        setStyle("-fx-background-color: #f0f2f5;");

        // 顶部工具栏
        setTop(buildTopBar());

        // 左侧分组面板
        setLeft(buildLeftPanel());

        // 右侧主内容
        setCenter(buildCenterPanel());

        // 状态栏
        setBottom(buildStatusBar());
    }

    private HBox buildTopBar() {
        HBox bar = new HBox(10);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(10, 15, 10, 15));
        bar.setStyle("-fx-background-color: #1a73e8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);");

        // Logo + 标题
        Label title = new Label("📒  通讯录管理系统");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white; -fx-font-family: 'Microsoft YaHei', sans-serif;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 搜索框
        searchField = new TextField();
        searchField.setPromptText("🔍  搜索姓名、电话、拼音...");
        searchField.setPrefWidth(280);
        searchField.setStyle("-fx-background-radius: 20; -fx-border-radius: 20; -fx-padding: 6 12; -fx-font-size: 13px; -fx-background-color: rgba(255,255,255,0.9);");
        searchField.textProperty().addListener((obs, o, n) -> doSearch(n));

        // 菜单
        MenuBar menuBar = buildMenuBar();
        menuBar.setStyle("-fx-background-color: transparent;");
        for (Menu m : menuBar.getMenus()) {
            m.setStyle("-fx-text-fill: white;");
        }

        bar.getChildren().addAll(title, spacer, searchField, menuBar);
        return bar;
    }

    private MenuBar buildMenuBar() {
        MenuBar mb = new MenuBar();
        mb.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        // 联系人菜单
        Menu contactMenu = new Menu("联系人");
        MenuItem addItem = new MenuItem("新增联系人 (Ctrl+N)");
        addItem.setOnAction(e -> addContact());
        MenuItem editItem = new MenuItem("编辑联系人");
        editItem.setOnAction(e -> editContact());
        MenuItem deleteItem = new MenuItem("删除联系人 (Del)");
        deleteItem.setOnAction(e -> deleteContact());
        contactMenu.getItems().addAll(addItem, editItem, new SeparatorMenuItem(), deleteItem);

        // 分组菜单
        Menu groupMenu = new Menu("分组");
        MenuItem addGroupItem = new MenuItem("新增分组");
        addGroupItem.setOnAction(e -> addGroup());
        MenuItem deleteGroupItem = new MenuItem("删除分组");
        deleteGroupItem.setOnAction(e -> deleteGroup());
        MenuItem moveToGroupItem = new MenuItem("移入分组...");
        moveToGroupItem.setOnAction(e -> moveToGroup());
        MenuItem removeFromGroupItem = new MenuItem("从分组移除");
        removeFromGroupItem.setOnAction(e -> removeFromGroup());
        groupMenu.getItems().addAll(addGroupItem, deleteGroupItem, new SeparatorMenuItem(), moveToGroupItem, removeFromGroupItem);

        // 导入导出
        Menu ioMenu = new Menu("导入/导出");
        MenuItem exportCSV = new MenuItem("导出为 CSV...");
        exportCSV.setOnAction(e -> exportCSV());
        MenuItem exportVCard = new MenuItem("导出为 vCard...");
        exportVCard.setOnAction(e -> exportVCard());
        MenuItem importCSV = new MenuItem("导入 CSV 文件...");
        importCSV.setOnAction(e -> importCSV());
        MenuItem importVCard = new MenuItem("导入 vCard 文件...");
        importVCard.setOnAction(e -> importVCard());
        ioMenu.getItems().addAll(exportCSV, exportVCard, new SeparatorMenuItem(), importCSV, importVCard);

        for (Menu m : new Menu[]{contactMenu, groupMenu, ioMenu}) {
            m.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
        }
        mb.getMenus().addAll(contactMenu, groupMenu, ioMenu);
        return mb;
    }

    private VBox buildLeftPanel() {
        VBox panel = new VBox(0);
        panel.setPrefWidth(180);
        panel.setMinWidth(150);
        panel.setStyle("-fx-background-color: #2c3e50;");

        // 分组标题
        Label label = new Label("分    组");
        label.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaa; -fx-padding: 12 12 8 12; -fx-font-family: 'Microsoft YaHei';");
        panel.getChildren().add(label);

        // 分组列表
        groupItems = FXCollections.observableArrayList();
        groupListView = new ListView<>(groupItems);
        groupListView.setStyle("-fx-background-color: #2c3e50; -fx-border-color: transparent;");
        VBox.setVgrow(groupListView, Priority.ALWAYS);

        groupListView.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                    return;
                }
                // 图标
                String icon = getGroupIcon(item);
                setText(icon + "  " + item);
                setStyle("-fx-background-color: transparent; -fx-text-fill: #ecf0f1; -fx-font-size: 13px; -fx-padding: 8 12; -fx-font-family: 'Microsoft YaHei';");
                setOnMouseEntered(e -> setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 8 12; -fx-font-family: 'Microsoft YaHei';"));
                setOnMouseExited(e -> {
                    boolean selected = groupListView.getSelectionModel().getSelectedItem() != null &&
                            groupListView.getSelectionModel().getSelectedItem().equals(item);
                    if (!selected) setStyle("-fx-background-color: transparent; -fx-text-fill: #ecf0f1; -fx-font-size: 13px; -fx-padding: 8 12; -fx-font-family: 'Microsoft YaHei';");
                });
            }
        });

        groupListView.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                currentGroup = newVal;
                searchField.clear();
                refreshContactList();
            }
        });

        // 新增分组按钮
        Button addGroupBtn = new Button("＋  新增分组");
        addGroupBtn.setMaxWidth(Double.MAX_VALUE);
        addGroupBtn.setStyle("-fx-background-color: #1a73e8; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8; -fx-background-radius: 0; -fx-border-color: transparent; -fx-cursor: hand;");
        addGroupBtn.setOnAction(e -> addGroup());

        panel.getChildren().addAll(groupListView, addGroupBtn);
        return panel;
    }

    private VBox buildCenterPanel() {
        VBox center = new VBox(0);
        center.setStyle("-fx-background-color: #f0f2f5;");

        // 联系人列表标题区
        HBox header = new HBox(10);
        header.setPadding(new Insets(10, 15, 8, 15));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

        Label listTitle = new Label("联系人列表");
        listTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #333; -fx-font-family: 'Microsoft YaHei';");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = styledBtn("＋ 新增", "#1a73e8", "white");
        addBtn.setOnAction(e -> addContact());
        Button editBtn = styledBtn("✎ 编辑", "#34a853", "white");
        editBtn.setOnAction(e -> editContact());
        Button deleteBtn = styledBtn("✕ 删除", "#ea4335", "white");
        deleteBtn.setOnAction(e -> deleteContact());

        header.getChildren().addAll(listTitle, spacer, addBtn, editBtn, deleteBtn);

        // 联系人表格
        contactTable = buildContactTable();
        VBox.setVgrow(contactTable, Priority.ALWAYS);

        center.getChildren().addAll(header, contactTable);
        return center;
    }

    @SuppressWarnings("unchecked")
    private TableView<Contact> buildContactTable() {
        contactItems = FXCollections.observableArrayList();
        TableView<Contact> table = new TableView<>(contactItems);
        table.setPlaceholder(new Label("暂无联系人，点击「新增」添加"));
        table.setStyle("-fx-background-color: white; -fx-border-color: transparent;");
        table.setRowFactory(tv -> {
            TableRow<Contact> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2 && !row.isEmpty()) {
                    editContact();
                }
            });
            // 右键菜单
            ContextMenu ctx = new ContextMenu();
            MenuItem editI = new MenuItem("编辑");
            editI.setOnAction(e -> editContact());
            MenuItem deleteI = new MenuItem("删除");
            deleteI.setOnAction(e -> deleteContact());
            MenuItem moveI = new MenuItem("移入分组...");
            moveI.setOnAction(e -> moveToGroup());
            ctx.getItems().addAll(editI, deleteI, new SeparatorMenuItem(), moveI);
            row.setContextMenu(ctx);
            return row;
        });

        // 列定义
        TableColumn<Contact, String> avatarCol = new TableColumn<>("");
        avatarCol.setPrefWidth(50);
        avatarCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        avatarCol.setCellFactory(col -> new TableCell<Contact, String>() {
            @Override
            protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty || name == null) { setGraphic(null); return; }
                Label av = new Label(name.isEmpty() ? "?" : String.valueOf(name.charAt(0)));
                av.setPrefSize(34, 34);
                av.setStyle("-fx-background-color: " + getAvatarColor(name) + "; -fx-background-radius: 17; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-alignment: center;");
                setGraphic(av);
                setText(null);
            }
        });

        TableColumn<Contact, String> nameCol = new TableColumn<>("姓名");
        nameCol.setPrefWidth(100);
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));

        TableColumn<Contact, String> mobileCol = new TableColumn<>("手机");
        mobileCol.setPrefWidth(130);
        mobileCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMobile()));

        TableColumn<Contact, String> phoneCol = new TableColumn<>("电话");
        phoneCol.setPrefWidth(120);
        phoneCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPhone()));

        TableColumn<Contact, String> emailCol = new TableColumn<>("电子邮箱");
        emailCol.setPrefWidth(160);
        emailCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));

        TableColumn<Contact, String> companyCol = new TableColumn<>("工作单位");
        companyCol.setPrefWidth(150);
        companyCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCompany()));

        TableColumn<Contact, String> groupCol = new TableColumn<>("分组");
        groupCol.setPrefWidth(120);
        groupCol.setCellValueFactory(c -> {
            Set<String> gs = c.getValue().getGroups();
            return new SimpleStringProperty(gs != null && !gs.isEmpty() ? String.join(", ", gs) : "未分组");
        });

        table.getColumns().addAll(avatarCol, nameCol, mobileCol, phoneCol, emailCol, companyCol, groupCol);

        // 列头样式
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        return table;
    }

    private HBox buildStatusBar() {
        HBox bar = new HBox();
        bar.setPadding(new Insets(5, 15, 5, 15));
        bar.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-width: 1 0 0 0;");
        statusLabel = new Label("就绪");
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
        bar.getChildren().add(statusLabel);
        return bar;
    }

    // ==================== 数据刷新 ====================

    public void refreshAll() {
        refreshGroupList();
        refreshContactList();
    }

    private void refreshGroupList() {
        groupItems.clear();
        groupItems.add("全部联系人");
        groupItems.add("未分组");
        groupItems.addAll(contactBook.getGroups());
        groupListView.getSelectionModel().select(currentGroup);
    }

    private void refreshContactList() {
        List<Contact> contacts;
        if (!searchField.getText().isEmpty()) {
            contacts = contactBook.search(searchField.getText());
        } else {
            contacts = contactBook.getContactsByGroup(currentGroup);
        }
        contactItems.setAll(contacts);
        updateStatus(contacts.size());
    }

    private void updateStatus(int count) {
        statusLabel.setText("当前显示：" + count + " 位联系人 | 共 " + contactBook.getTotalCount() + " 位 | 数据存储于：" + storageService.getDataDir());
    }

    private void doSearch(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            refreshContactList();
        } else {
            List<Contact> results = contactBook.search(keyword);
            contactItems.setAll(results);
            updateStatus(results.size());
        }
    }

    // ==================== 联系人操作 ====================

    private void addContact() {
        ContactEditDialog dlg = new ContactEditDialog(contactBook, null);
        dlg.initOwner(stage);
        Optional<Contact> result = dlg.showAndWait();
        result.ifPresent(c -> {
            if (c != null) {
                contactBook.addContact(c);
                saveData();
                refreshAll();
                showStatus("已添加联系人：" + c.getName());
            }
        });
    }

    private void editContact() {
        Contact selected = contactTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("请先选择一个联系人");
            return;
        }
        ContactEditDialog dlg = new ContactEditDialog(contactBook, selected);
        dlg.initOwner(stage);
        Optional<Contact> result = dlg.showAndWait();
        result.ifPresent(c -> {
            if (c != null) {
                contactBook.updateContact(c);
                saveData();
                refreshAll();
                showStatus("已更新联系人：" + c.getName());
            }
        });
    }

    private void deleteContact() {
        List<Contact> selected = new ArrayList<>(contactTable.getSelectionModel().getSelectedItems());
        if (selected.isEmpty()) {
            showInfo("请先选择要删除的联系人");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "确定要删除选中的 " + selected.size() + " 位联系人吗？此操作不可撤销。",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("确认删除");
        confirm.initOwner(stage);
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.YES) {
            for (Contact c : selected) contactBook.removeContact(c);
            saveData();
            refreshAll();
            showStatus("已删除 " + selected.size() + " 位联系人");
        }
    }

    // ==================== 分组操作 ====================

    private void addGroup() {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("新增分组");
        dlg.setHeaderText("请输入新分组名称：");
        dlg.setContentText("分组名：");
        dlg.initOwner(stage);
        Optional<String> result = dlg.showAndWait();
        result.ifPresent(name -> {
            name = name.trim();
            if (!name.isEmpty()) {
                contactBook.addGroup(name);
                saveData();
                refreshGroupList();
                showStatus("已新增分组：" + name);
            }
        });
    }

    private void deleteGroup() {
        String selectedGroup = groupListView.getSelectionModel().getSelectedItem();
        if (selectedGroup == null || selectedGroup.equals("全部联系人") || selectedGroup.equals("未分组")) {
            showInfo("请先选择一个可删除的分组（不能删除系统分组）");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "删除分组「" + selectedGroup + "」？（该组内联系人不会被删除，仅移出该组）",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("确认删除分组");
        confirm.initOwner(stage);
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.YES) {
            contactBook.removeGroup(selectedGroup);
            currentGroup = "全部联系人";
            saveData();
            refreshAll();
            showStatus("已删除分组：" + selectedGroup);
        }
    }

    private void moveToGroup() {
        List<Contact> selected = new ArrayList<>(contactTable.getSelectionModel().getSelectedItems());
        if (selected.isEmpty()) { showInfo("请先选择联系人"); return; }

        List<String> groups = contactBook.getGroups();
        if (groups.isEmpty()) { showInfo("还没有分组，请先新增分组"); return; }

        ChoiceDialog<String> dlg = new ChoiceDialog<>(groups.get(0), groups);
        dlg.setTitle("移入分组");
        dlg.setHeaderText("选择目标分组：");
        dlg.setContentText("分组：");
        dlg.initOwner(stage);
        Optional<String> res = dlg.showAndWait();
        res.ifPresent(group -> {
            for (Contact c : selected) c.addGroup(group);
            saveData();
            refreshContactList();
            showStatus("已将 " + selected.size() + " 位联系人移入分组：" + group);
        });
    }

    private void removeFromGroup() {
        List<Contact> selected = new ArrayList<>(contactTable.getSelectionModel().getSelectedItems());
        if (selected.isEmpty()) { showInfo("请先选择联系人"); return; }
        if (currentGroup.equals("全部联系人") || currentGroup.equals("未分组")) {
            showInfo("请先在左侧选择一个具体分组");
            return;
        }
        for (Contact c : selected) c.removeGroup(currentGroup);
        saveData();
        refreshContactList();
        showStatus("已从分组「" + currentGroup + "」移除 " + selected.size() + " 位联系人");
    }

    // ==================== 导入导出 ====================

    private void exportCSV() {
        FileChooser fc = new FileChooser();
        fc.setTitle("导出 CSV");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV 文件", "*.csv"));
        fc.setInitialFileName("通讯录.csv");
        File f = fc.showSaveDialog(stage);
        if (f != null) {
            try {
                List<Contact> toExport = contactTable.getItems().isEmpty() ? contactBook.getContacts() : new ArrayList<>(contactTable.getItems());
                storageService.exportCSV(toExport, f.getAbsolutePath());
                showStatus("已导出 " + toExport.size() + " 位联系人到：" + f.getName());
                showInfo("导出成功！文件：" + f.getAbsolutePath());
            } catch (Exception ex) {
                showError("导出失败：" + ex.getMessage());
            }
        }
    }

    private void exportVCard() {
        FileChooser fc = new FileChooser();
        fc.setTitle("导出 vCard");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("vCard 文件", "*.vcf"));
        fc.setInitialFileName("通讯录.vcf");
        File f = fc.showSaveDialog(stage);
        if (f != null) {
            try {
                List<Contact> toExport = new ArrayList<>(contactTable.getItems());
                if (toExport.isEmpty()) toExport = contactBook.getContacts();
                storageService.exportVCard(toExport, f.getAbsolutePath());
                showStatus("已导出 vCard 到：" + f.getName());
                showInfo("导出成功！文件：" + f.getAbsolutePath());
            } catch (Exception ex) {
                showError("导出失败：" + ex.getMessage());
            }
        }
    }

    private void importCSV() {
        FileChooser fc = new FileChooser();
        fc.setTitle("导入 CSV");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV 文件", "*.csv"));
        File f = fc.showOpenDialog(stage);
        if (f != null) {
            try {
                List<Contact> imported = storageService.importCSV(f.getAbsolutePath());
                for (Contact c : imported) contactBook.addContact(c);
                saveData();
                refreshAll();
                showStatus("已导入 " + imported.size() + " 位联系人");
                showInfo("导入成功！共导入 " + imported.size() + " 位联系人。");
            } catch (Exception ex) {
                showError("导入失败：" + ex.getMessage());
            }
        }
    }

    private void importVCard() {
        FileChooser fc = new FileChooser();
        fc.setTitle("导入 vCard");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("vCard 文件", "*.vcf", "*.vcard"));
        File f = fc.showOpenDialog(stage);
        if (f != null) {
            try {
                List<Contact> imported = storageService.importVCard(f.getAbsolutePath());
                for (Contact c : imported) contactBook.addContact(c);
                saveData();
                refreshAll();
                showStatus("已导入 " + imported.size() + " 位联系人");
                showInfo("导入成功！共导入 " + imported.size() + " 位联系人。");
            } catch (Exception ex) {
                showError("导入失败：" + ex.getMessage());
            }
        }
    }

    // ==================== 工具方法 ====================

    private void saveData() {
        try {
            storageService.save(contactBook);
        } catch (Exception ex) {
            showError("保存失败：" + ex.getMessage());
        }
    }

    private void showStatus(String msg) {
        statusLabel.setText(msg + " | 共 " + contactBook.getTotalCount() + " 位");
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null); a.initOwner(stage); a.showAndWait();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText("错误"); a.initOwner(stage); a.showAndWait();
    }

    private String getGroupIcon(String name) {
        switch (name) {
            case "全部联系人": return "👥";
            case "未分组": return "📁";
            case "朋友": return "😊";
            case "家人": return "🏠";
            case "同事": return "💼";
            case "同学": return "🎓";
            default: return "📌";
        }
    }

    private String getAvatarColor(String name) {
        String[] colors = {"#1a73e8","#34a853","#ea4335","#fbbc04","#9c27b0","#00bcd4","#ff5722","#607d8b"};
        int idx = Math.abs(name.hashCode()) % colors.length;
        return colors[idx];
    }

    private Button styledBtn(String text, String bg, String fg) {
        Button b = new Button(text);
        b.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: %s; -fx-font-size: 12px; -fx-padding: 6 14; -fx-background-radius: 4; -fx-cursor: hand;", bg, fg));
        return b;
    }
}
