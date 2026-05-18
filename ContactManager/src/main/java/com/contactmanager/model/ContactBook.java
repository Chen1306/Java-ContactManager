package com.contactmanager.model;

import java.io.Serializable;
import java.util.*;

/**
 * 通讯录数据模型
 * 管理所有联系人和分组
 */
public class ContactBook implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Contact> contacts;
    private List<String> groups;

    public ContactBook() {
        this.contacts = new ArrayList<>();
        this.groups = new ArrayList<>();
        // 默认分组
        this.groups.add("朋友");
        this.groups.add("家人");
        this.groups.add("同事");
        this.groups.add("同学");
    }

    public List<Contact> getContacts() { return contacts; }
    public void setContacts(List<Contact> contacts) { this.contacts = contacts; }

    public List<String> getGroups() { return groups; }
    public void setGroups(List<String> groups) { this.groups = groups; }

    public void addContact(Contact c) {
        contacts.add(c);
    }

    public void removeContact(Contact c) {
        contacts.remove(c);
    }

    public void updateContact(Contact updated) {
        for (int i = 0; i < contacts.size(); i++) {
            if (contacts.get(i).getId().equals(updated.getId())) {
                contacts.set(i, updated);
                return;
            }
        }
    }

    public Optional<Contact> findById(String id) {
        return contacts.stream().filter(c -> c.getId().equals(id)).findFirst();
    }

    public void addGroup(String groupName) {
        if (!groups.contains(groupName)) {
            groups.add(groupName);
        }
    }

    /**
     * 删除分组，但不删除联系人，只是将联系人移出该组
     */
    public void removeGroup(String groupName) {
        groups.remove(groupName);
        for (Contact c : contacts) {
            c.removeGroup(groupName);
        }
    }

    public List<Contact> getContactsByGroup(String groupName) {
        if (groupName == null || groupName.equals("全部联系人")) {
            List<Contact> all = new ArrayList<>(contacts);
            Collections.sort(all);
            return all;
        }
        if (groupName.equals("未分组")) {
            List<Contact> ungrouped = new ArrayList<>();
            for (Contact c : contacts) {
                if (c.getGroups() == null || c.getGroups().isEmpty()) {
                    ungrouped.add(c);
                }
            }
            Collections.sort(ungrouped);
            return ungrouped;
        }
        List<Contact> result = new ArrayList<>();
        for (Contact c : contacts) {
            if (c.isInGroup(groupName)) result.add(c);
        }
        Collections.sort(result);
        return result;
    }

    /**
     * 搜索联系人：支持姓名、电话、手机、拼音全拼、声母模糊搜索
     */
    public List<Contact> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return getContactsByGroup("全部联系人");
        String kw = keyword.trim().toLowerCase();
        List<Contact> result = new ArrayList<>();
        for (Contact c : contacts) {
            if (matches(c, kw)) result.add(c);
        }
        Collections.sort(result);
        return result;
    }

    private boolean matches(Contact c, String kw) {
        // 姓名
        if (c.getName() != null && c.getName().toLowerCase().contains(kw)) return true;
        // 手机
        if (c.getMobile() != null && c.getMobile().contains(kw)) return true;
        // 固话
        if (c.getPhone() != null && c.getPhone().contains(kw)) return true;
        // 拼音全拼
        if (c.getPinyin() != null && c.getPinyin().toLowerCase().contains(kw)) return true;
        // 声母
        if (c.getPinyinInitials() != null && c.getPinyinInitials().toLowerCase().contains(kw)) return true;
        // 邮箱
        if (c.getEmail() != null && c.getEmail().toLowerCase().contains(kw)) return true;
        // 单位
        if (c.getCompany() != null && c.getCompany().toLowerCase().contains(kw)) return true;
        return false;
    }

    public int getTotalCount() {
        return contacts.size();
    }

    public int getGroupCount(String groupName) {
        return getContactsByGroup(groupName).size();
    }
}
