package com.contactmanager;

import com.contactmanager.model.Contact;
import com.contactmanager.model.ContactBook;
import com.contactmanager.service.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 通讯录管理系统核心功能测试类
 */
public class ContactManagerTest {

    private ContactBook contactBook;
    private StorageService storageService;

    @TempDir
    Path tempDir; // JUnit 5 提供的临时目录，测试完自动删除

    @BeforeEach
    void setUp() {
        contactBook = new ContactBook();
        storageService = new StorageService() {
            // 重写目录获取，使测试不影响用户真实数据
            @Override
            public String getDataDir() {
                return tempDir.toString();
            }
        };
    }

    /**
     * 测试 1：新增和删除联系人
     */
    @Test
    void testAddAndRemoveContact() {
        Contact contact = new Contact("测试用户");
        contact.setMobile("13344445555");

        // 测试新增
        contactBook.addContact(contact);
        assertEquals(1, contactBook.getTotalCount(), "新增后联系人总数应为 1");
        assertTrue(contactBook.getContacts().contains(contact));

        // 测试删除
        contactBook.removeContact(contact);
        assertEquals(0, contactBook.getTotalCount(), "删除后联系人总数应为 0");
    }

    /**
     * 测试 2：新增和删除分组
     */
    @Test
    void testAddAndRemoveGroup() {
        String groupName = "新测试组";

        // 测试新增分组
        contactBook.addGroup(groupName);
        assertTrue(contactBook.getGroups().contains(groupName), "分组列表应包含新分组");

        // 测试删除分组
        contactBook.removeGroup(groupName);
        assertFalse(contactBook.getGroups().contains(groupName), "删除后分组列表不应包含该分组");
    }

    /**
     * 测试 3：联系人加入和移出分组
     */
    @Test
    void testContactGroupMembership() {
        Contact contact = new Contact("组员A");
        String groupName = "项目组";
        
        contactBook.addContact(contact);
        contactBook.addGroup(groupName);

        // 测试加入分组
        contact.addGroup(groupName);
        assertTrue(contact.isInGroup(groupName), "联系人应在该分组内");
        assertEquals(1, contactBook.getContactsByGroup(groupName).size(), "该组联系人数量应为 1");

        // 测试移出分组
        contact.removeGroup(groupName);
        assertFalse(contact.isInGroup(groupName), "联系人应已移出该分组");
        assertEquals(0, contactBook.getContactsByGroup(groupName).size(), "该组联系人数量应为 0");
    }

    /**
     * 测试 4：保存数据
     */
    @Test
    void testSaveData() throws Exception {
        Contact contact = new Contact("持久化测试");
        contact.setMobile("123456");
        contactBook.addContact(contact);
        contactBook.addGroup("持久化组");

        // 执行保存
        storageService.save(contactBook);

        // 验证文件是否生成（StorageService 内部硬编码了文件名 contacts.xml）
        File savedFile = new File(tempDir.toFile(), "contacts.xml");
        assertTrue(savedFile.exists(), "保存后应生成 XML 文件");
        assertTrue(savedFile.length() > 0, "生成的 XML 文件不应为空");
    }
}
