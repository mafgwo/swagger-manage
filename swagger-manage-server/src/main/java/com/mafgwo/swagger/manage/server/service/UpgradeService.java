//package com.mafgwo.swagger.manage.server.service;
//
//import com.mafgwo.swagger.manage.server.entity.ColumnInfo;
//import com.mafgwo.swagger.manage.server.mapper.UpgradeMapper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.stereotype.Service;
//import org.springframework.util.FileCopyUtils;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.util.List;
//import java.util.Objects;
//
///**
// * @author tanghc
// */
//@Service
//public class UpgradeService {
//
//    public static final String TABLE_PROJECT_INFO = "project_info";
//
//    @Autowired
//    private UpgradeMapper upgradeMapper;
//
//    @Value("${spring.datasource.driver-class-name}")
//    private String driverClassName;
//
//    public static void initDatabase() {
//        String filename = "swaggeradmin.db";
//        String filepath = System.getProperty("user.dir") + "/" + filename;
//        File dbFile = new File(filepath);
//        if (!dbFile.exists()) {
//            ClassPathResource resource = new ClassPathResource(filename);
//            try {
//                FileCopyUtils.copy(resource.getInputStream(), new FileOutputStream(dbFile));
//            } catch (IOException e) {
//                throw new RuntimeException("初始化数据库失败", e);
//            }
//        }
//    }
//
//    /**
//     * 升级
//     */
//    public void upgrade() {
//        upgradeV1_1_1();
//    }
//
//    /**
//     * 升级v1.1.1
//     */
//    private void upgradeV1_1_1() {
//        this.addColumn(TABLE_PROJECT_INFO, "basic_auth_username", "varchar(128)");
//        this.addColumn(TABLE_PROJECT_INFO, "basic_auth_password", "varchar(128)");
//    }
//
//    /**
//     * 添加表字段
//     * @param tableName 表名
//     * @param columnName 字段名
//     * @param type 字段类型，如：varchar(128),text,integer
//     */
//    public void addColumn(String tableName, String columnName, String type) {
//        if (!isColumnExist(tableName, columnName)) {
//            if (isMysql()) {
//                upgradeMapper.addColumnMysql(tableName, columnName, type);
//            } else {
//                upgradeMapper.addColumn(tableName, columnName, type);
//            }
//        }
//    }
//
//    /**
//     * 判断列是否存在
//     * @param tableName 表名
//     * @param columnName 列名
//     * @return true：存在
//     */
//    public boolean isColumnExist(String tableName, String columnName) {
//        List<ColumnInfo> columnInfoList = isMysql() ? upgradeMapper.listColumnInfoMysql(tableName) :
//                upgradeMapper.listColumnInfo(tableName);
//        return columnInfoList
//                .stream()
//                .anyMatch(columnInfo -> Objects.equals(columnInfo.getName(), columnName));
//    }
//
//    private boolean isMysql() {
//        return this.driverClassName.contains("mysql");
//    }
//}
