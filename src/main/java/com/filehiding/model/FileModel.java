package com.filehiding.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FileModel {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/file_hiding_db";
    private static final String USER = "your_username"; // replace with your MySQL username
    private static final String PASSWORD = "your_password"; // replace with your MySQL password

    private Connection connectToDatabase() {
        try {
            return DriverManager.getConnection(DB_URL, USER, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void hideFileInDatabase(String filePath) {
        String sql = "UPDATE files SET is_hidden = ? WHERE file_path = ?";
        try (Connection connection = connectToDatabase();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setBoolean(1, true);
            pstmt.setString(2, filePath);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getHiddenFilesFromDatabase() {
        List<String> hiddenFiles = new ArrayList<>();
        String sql = "SELECT file_path FROM files WHERE is_hidden = ?";
        try (Connection connection = connectToDatabase();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setBoolean(1, true);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                hiddenFiles.add(rs.getString("file_path"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hiddenFiles;
    }

    public void unhideFileInDatabase(String filePath) {
        String sql = "UPDATE files SET is_hidden = ? WHERE file_path = ?";
        try (Connection connection = connectToDatabase();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setBoolean(1, false);
            pstmt.setString(2, filePath);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}