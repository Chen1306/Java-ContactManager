package com.contactmanager.service;

import com.contactmanager.model.Contact;
import com.contactmanager.model.ContactBook;
import com.contactmanager.util.PinyinUtil;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;

/**
 * 数据存储服务
 * 使用 XML 格式持久化通讯录数据
 */
public class StorageService {

    private String dataDir = System.getProperty("user.home") + File.separator + "ContactManager";
    private static final String FILE_NAME = "contacts.xml";

    public StorageService() {
        // 确保数据目录存在
        new File(getDataDir()).mkdirs();
    }

    public String getDataDir() {
        return dataDir;
    }

    private File getDataFile() {
        return new File(getDataDir(), FILE_NAME);
    }

    // ========== 保存 ==========

    public void save(ContactBook book) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();

        Element root = doc.createElement("contactbook");
        doc.appendChild(root);

        // 保存分组
        Element groupsEl = doc.createElement("groups");
        for (String g : book.getGroups()) {
            Element ge = doc.createElement("group");
            ge.setTextContent(g);
            groupsEl.appendChild(ge);
        }
        root.appendChild(groupsEl);

        // 保存联系人
        Element contactsEl = doc.createElement("contacts");
        for (Contact c : book.getContacts()) {
            contactsEl.appendChild(contactToXml(doc, c));
        }
        root.appendChild(contactsEl);

        // 写文件
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tf.transform(new DOMSource(doc), new StreamResult(getDataFile()));
    }

    // ========== 加载 ==========

    public ContactBook load() throws Exception {
        ContactBook book = new ContactBook();
        File f = getDataFile();
        if (!f.exists()) return book;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(f);
        doc.getDocumentElement().normalize();

        // 加载分组
        NodeList groupNodes = doc.getElementsByTagName("group");
        List<String> groups = new ArrayList<>();
        for (int i = 0; i < groupNodes.getLength(); i++) {
            String g = groupNodes.item(i).getTextContent().trim();
            if (!g.isEmpty()) groups.add(g);
        }
        if (!groups.isEmpty()) book.setGroups(groups);

        // 加载联系人
        NodeList contactNodes = doc.getElementsByTagName("contact");
        List<Contact> contacts = new ArrayList<>();
        for (int i = 0; i < contactNodes.getLength(); i++) {
            contacts.add(xmlToContact((Element) contactNodes.item(i)));
        }
        book.setContacts(contacts);
        return book;
    }

    // ========== XML 转换辅助 ==========

    private Element contactToXml(Document doc, Contact c) {
        Element el = doc.createElement("contact");
        addChild(doc, el, "id", c.getId());
        addChild(doc, el, "name", c.getName());
        addChild(doc, el, "phone", c.getPhone());
        addChild(doc, el, "mobile", c.getMobile());
        addChild(doc, el, "imTool", c.getImTool());
        addChild(doc, el, "imNumber", c.getImNumber());
        addChild(doc, el, "email", c.getEmail());
        addChild(doc, el, "homepage", c.getHomepage());
        addChild(doc, el, "birthday", c.getBirthday());
        addChild(doc, el, "photoPath", c.getPhotoPath());
        addChild(doc, el, "company", c.getCompany());
        addChild(doc, el, "address", c.getAddress());
        addChild(doc, el, "zipCode", c.getZipCode());
        addChild(doc, el, "remark", c.getRemark());
        addChild(doc, el, "pinyin", c.getPinyin());
        addChild(doc, el, "pinyinInitials", c.getPinyinInitials());

        if (c.getGroups() != null) {
            Element gs = doc.createElement("groups");
            for (String g : c.getGroups()) {
                Element ge = doc.createElement("g");
                ge.setTextContent(g);
                gs.appendChild(ge);
            }
            el.appendChild(gs);
        }
        return el;
    }

    private Contact xmlToContact(Element el) {
        Contact c = new Contact();
        c.setId(getChildText(el, "id"));
        c.setName(getChildText(el, "name"));
        c.setPhone(getChildText(el, "phone"));
        c.setMobile(getChildText(el, "mobile"));
        c.setImTool(getChildText(el, "imTool"));
        c.setImNumber(getChildText(el, "imNumber"));
        c.setEmail(getChildText(el, "email"));
        c.setHomepage(getChildText(el, "homepage"));
        c.setBirthday(getChildText(el, "birthday"));
        c.setPhotoPath(getChildText(el, "photoPath"));
        c.setCompany(getChildText(el, "company"));
        c.setAddress(getChildText(el, "address"));
        c.setZipCode(getChildText(el, "zipCode"));
        c.setRemark(getChildText(el, "remark"));
        c.setPinyin(getChildText(el, "pinyin"));
        c.setPinyinInitials(getChildText(el, "pinyinInitials"));

        // 加载分组
        Set<String> groups = new HashSet<>();
        NodeList gs = el.getElementsByTagName("g");
        for (int i = 0; i < gs.getLength(); i++) {
            String g = gs.item(i).getTextContent().trim();
            if (!g.isEmpty()) groups.add(g);
        }
        c.setGroups(groups);

        // 补全拼音（兼容旧数据）
        if ((c.getPinyin() == null || c.getPinyin().isEmpty()) && c.getName() != null) {
            PinyinUtil.updatePinyin(c);
        }
        return c;
    }

    private void addChild(Document doc, Element parent, String tag, String text) {
        Element el = doc.createElement(tag);
        el.setTextContent(text != null ? text : "");
        parent.appendChild(el);
    }

    private String getChildText(Element parent, String tag) {
        NodeList nl = parent.getElementsByTagName(tag);
        if (nl.getLength() > 0) {
            String t = nl.item(0).getTextContent();
            return (t != null && !t.isEmpty()) ? t : null;
        }
        return null;
    }

    // ========== CSV 导入导出 ==========

    public void exportCSV(List<Contact> contacts, String filePath) throws IOException {
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(filePath), "UTF-8"))) {
            pw.println("\uFEFF姓名,电话,手机,即时通信工具,即时通信号码,电子邮箱,个人主页,生日,工作单位,家庭地址,邮编,分组,备注");
            for (Contact c : contacts) {
                pw.println(String.join(",",
                    csv(c.getName()), csv(c.getPhone()), csv(c.getMobile()),
                    csv(c.getImTool()), csv(c.getImNumber()), csv(c.getEmail()),
                    csv(c.getHomepage()), csv(c.getBirthday()), csv(c.getCompany()),
                    csv(c.getAddress()), csv(c.getZipCode()),
                    csv(c.getGroups() != null ? String.join("|", c.getGroups()) : ""),
                    csv(c.getRemark())
                ));
            }
        }
    }

    public List<Contact> importCSV(String filePath) throws IOException {
        List<Contact> result = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(filePath), "UTF-8"))) {
            String line = br.readLine(); // 跳过BOM和标题行
            if (line != null && line.startsWith("\uFEFF")) line = line.substring(1);
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",", -1);
                Contact c = new Contact();
                c.setName(safeGet(fields, 0));
                c.setPhone(safeGet(fields, 1));
                c.setMobile(safeGet(fields, 2));
                c.setImTool(safeGet(fields, 3));
                c.setImNumber(safeGet(fields, 4));
                c.setEmail(safeGet(fields, 5));
                c.setHomepage(safeGet(fields, 6));
                c.setBirthday(safeGet(fields, 7));
                c.setCompany(safeGet(fields, 8));
                c.setAddress(safeGet(fields, 9));
                c.setZipCode(safeGet(fields, 10));
                String groupStr = safeGet(fields, 11);
                if (groupStr != null && !groupStr.isEmpty()) {
                    for (String g : groupStr.split("\\|")) c.addGroup(g.trim());
                }
                c.setRemark(safeGet(fields, 12));
                PinyinUtil.updatePinyin(c);
                if (c.getName() != null && !c.getName().isEmpty()) result.add(c);
            }
        }
        return result;
    }

    // ========== vCard 导入导出 ==========

    public void exportVCard(List<Contact> contacts, String filePath) throws IOException {
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(filePath), "UTF-8"))) {
            for (Contact c : contacts) {
                pw.println("BEGIN:VCARD");
                pw.println("VERSION:3.0");
                pw.println("FN:" + safe(c.getName()));
                pw.println("N:" + safe(c.getName()) + ";;;;");
                if (c.getMobile() != null) pw.println("TEL;TYPE=CELL:" + c.getMobile());
                if (c.getPhone() != null) pw.println("TEL;TYPE=HOME:" + c.getPhone());
                if (c.getEmail() != null) pw.println("EMAIL:" + c.getEmail());
                if (c.getCompany() != null) pw.println("ORG:" + c.getCompany());
                if (c.getAddress() != null) pw.println("ADR;TYPE=HOME:;;" + c.getAddress() + ";;;;");
                if (c.getBirthday() != null) pw.println("BDAY:" + c.getBirthday().replace("-", ""));
                if (c.getHomepage() != null) pw.println("URL:" + c.getHomepage());
                if (c.getRemark() != null) pw.println("NOTE:" + c.getRemark());
                if (c.getGroups() != null) {
                    for (String g : c.getGroups()) pw.println("CATEGORIES:" + g);
                }
                pw.println("END:VCARD");
                pw.println();
            }
        }
    }

    public List<Contact> importVCard(String filePath) throws IOException {
        List<Contact> result = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(filePath), "UTF-8"))) {
            Contact current = null;
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.equalsIgnoreCase("BEGIN:VCARD")) {
                    current = new Contact();
                } else if (line.equalsIgnoreCase("END:VCARD")) {
                    if (current != null) {
                        PinyinUtil.updatePinyin(current);
                        result.add(current);
                        current = null;
                    }
                } else if (current != null) {
                    parseVCardLine(current, line);
                }
            }
        }
        return result;
    }

    private void parseVCardLine(Contact c, String line) {
        if (line.startsWith("FN:")) {
            c.setName(line.substring(3));
        } else if (line.startsWith("TEL;TYPE=CELL:") || line.startsWith("TEL;CELL:")) {
            c.setMobile(line.substring(line.indexOf(':') + 1));
        } else if (line.startsWith("TEL;TYPE=HOME:") || line.startsWith("TEL;HOME:")) {
            c.setPhone(line.substring(line.indexOf(':') + 1));
        } else if (line.startsWith("TEL:")) {
            // 通用电话
            if (c.getMobile() == null) c.setMobile(line.substring(4));
        } else if (line.startsWith("EMAIL")) {
            c.setEmail(line.substring(line.indexOf(':') + 1));
        } else if (line.startsWith("ORG:")) {
            c.setCompany(line.substring(4));
        } else if (line.startsWith("ADR")) {
            String adr = line.substring(line.indexOf(':') + 1);
            String[] parts = adr.split(";");
            if (parts.length > 2) c.setAddress(parts[2]);
        } else if (line.startsWith("BDAY:")) {
            String bd = line.substring(5);
            if (bd.length() == 8) {
                bd = bd.substring(0, 4) + "-" + bd.substring(4, 6) + "-" + bd.substring(6);
            }
            c.setBirthday(bd);
        } else if (line.startsWith("URL:")) {
            c.setHomepage(line.substring(4));
        } else if (line.startsWith("NOTE:")) {
            c.setRemark(line.substring(5));
        } else if (line.startsWith("CATEGORIES:")) {
            c.addGroup(line.substring(11).trim());
        }
    }

    // ========== 工具方法 ==========

    private String csv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private String safe(String s) { return s != null ? s : ""; }

    private String safeGet(String[] arr, int idx) {
        if (arr == null || idx >= arr.length) return null;
        String s = arr[idx].trim();
        if (s.startsWith("\"") && s.endsWith("\"")) s = s.substring(1, s.length() - 1).replace("\"\"", "\"");
        return s.isEmpty() ? null : s;
    }
}
