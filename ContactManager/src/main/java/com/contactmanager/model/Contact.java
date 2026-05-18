package com.contactmanager.model;

import java.io.Serializable;
import java.util.*;

/**
 * 联系人实体类
 * 包含通讯录管理程序要求的所有字段
 */
public class Contact implements Serializable, Comparable<Contact> {
    private static final long serialVersionUID = 1L;

    private String id;           // 唯一标识
    private String name;         // 姓名
    private String phone;        // 电话（固话）
    private String mobile;       // 手机
    private String imTool;       // 即时通信工具（如QQ、微信）
    private String imNumber;     // 即时通信号码
    private String email;        // 电子邮箱
    private String homepage;     // 个人主页
    private String birthday;     // 生日 (yyyy-MM-dd)
    private String photoPath;    // 像片路径
    private String company;      // 工作单位
    private String address;      // 家庭地址
    private String zipCode;      // 邮编
    private Set<String> groups;  // 所属组（可多组）
    private String remark;       // 备注
    private String pinyin;       // 姓名拼音（搜索用）
    private String pinyinInitials; // 拼音声母

    public Contact() {
        this.id = UUID.randomUUID().toString();
        this.groups = new HashSet<>();
    }

    public Contact(String name) {
        this();
        this.name = name;
    }

    // ========== 字段访问器 ==========

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getImTool() { return imTool; }
    public void setImTool(String imTool) { this.imTool = imTool; }

    public String getImNumber() { return imNumber; }
    public void setImNumber(String imNumber) { this.imNumber = imNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getHomepage() { return homepage; }
    public void setHomepage(String homepage) { this.homepage = homepage; }

    public String getBirthday() { return birthday; }
    public void setBirthday(String birthday) { this.birthday = birthday; }

    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }

    public Set<String> getGroups() { return groups; }
    public void setGroups(Set<String> groups) { this.groups = groups; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public String getPinyin() { return pinyin; }
    public void setPinyin(String pinyin) { this.pinyin = pinyin; }

    public String getPinyinInitials() { return pinyinInitials; }
    public void setPinyinInitials(String pinyinInitials) { this.pinyinInitials = pinyinInitials; }

    // ========== 工具方法 ==========

    public void addGroup(String groupName) {
        this.groups.add(groupName);
    }

    public void removeGroup(String groupName) {
        this.groups.remove(groupName);
    }

    public boolean isInGroup(String groupName) {
        return this.groups.contains(groupName);
    }

    /**
     * 获取姓名首字母（用于分类显示）
     */
    public String getFirstLetter() {
        if (pinyin != null && !pinyin.isEmpty()) {
            return pinyin.substring(0, 1).toUpperCase();
        }
        if (name != null && !name.isEmpty()) {
            char first = name.charAt(0);
            if (first >= 'A' && first <= 'Z') return String.valueOf(first);
            if (first >= 'a' && first <= 'z') return String.valueOf((char)(first - 32));
        }
        return "#";
    }

    @Override
    public int compareTo(Contact other) {
        String a = this.pinyin != null ? this.pinyin : (this.name != null ? this.name : "");
        String b = other.pinyin != null ? other.pinyin : (other.name != null ? other.name : "");
        return a.compareToIgnoreCase(b);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Contact)) return false;
        Contact c = (Contact) o;
        return Objects.equals(id, c.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name != null ? name : "(无姓名)";
    }
}
