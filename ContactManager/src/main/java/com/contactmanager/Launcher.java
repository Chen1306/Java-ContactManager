package com.contactmanager;

/**
 * 启动类包装器
 * 用于解决 JavaFX 11+ 直接运行 JAR 时报“缺少运行时组件”的问题
 */
public class Launcher {
    public static void main(String[] args) {
        App.main(args);
    }
}
