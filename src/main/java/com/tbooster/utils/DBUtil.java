package com.tbooster.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
  * @Author xxx
  * @Date 2020/4/21 12:53 AM
  */
public class DBUtil {

    // search corpus database
    public static final String URL = "jdbc:mysql://localhost:3306/tcdb2?serverTimezone=Asia/Shanghai";
    // query database
//    public static final String URL = "jdbc:mysql://localhost:3306/tcdb2019?serverTimezone=Asia/Shanghai";
    public static final String USER = "root";
    public static final String PASSWORD = "xxx";
    private static Connection conn = null;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        return conn;
    }

    public static void closeConnection() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
