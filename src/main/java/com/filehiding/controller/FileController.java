package com.filehiding.controller;

import com.filehiding.database.DatabaseConnection;
import com.filehiding.model.FileRecord;
import com.filehiding.view.FileOperationsView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FileOperationsController {
    private FileOperationsView view;

    public FileOperationsController(FileOperationsView view) {
        this.view = view;
        this.view.setHideFileListener(new HideFileListener());
        this.view.setShowHiddenFilesListener(new ShowHiddenFilesListener());
        this.view.setUnhideFileListener(new UnhideFileListener());
    }

    private class HideFileListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String filePath = view.selectFileToHide();
            if (filePath != null && !filePath.isEmpty()) {
                File fileToHide = new File(filePath);
                if (fileToHide.exists()) {
                    int confirmation = JOptionPane.showConfirmDialog(view,
                            "Are you sure you want to hide this file? This will delete it permanently.",
                            "Confirm Hide File",
                            JOptionPane.YES_NO_OPTION);
                    if (confirmation == JOptionPane.YES_OPTION) {
                        if (fileToHide.delete()) {
                            try (Connection connection = DatabaseConnection.getConnection()) {
                                String sql = "INSERT INTO files (file_path, is_hidden) VALUES (?, ?)";
                                PreparedStatement statement = connection.prepareStatement(sql);
                                statement.setString(1, filePath);
                                statement.setBoolean(2, true);
                                statement.executeUpdate();
                                view.showMessage("File hidden successfully.");
                            } catch (SQLException ex) {
                                view.showMessage("Error hiding file in database: " + ex.getMessage());
                            }
                        } else {
                            view.showMessage("Error deleting the file from the file system.");
                        }
                    }
                } else {
                    view.showMessage("File does not exist.");
                }
            }
        }
    }

    private class ShowHiddenFilesListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try (Connection connection = DatabaseConnection.getConnection()) {
                String sql = "SELECT file_path FROM files WHERE is_hidden = true";
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql);
                List<String> hiddenFiles = new ArrayList<>();
                while (resultSet.next()) {
                    hiddenFiles.add(resultSet.getString("file_path"));
                }
                view.showHiddenFiles(hiddenFiles.toArray(new String[0]));
            } catch (SQLException ex) {
                view.showMessage("Error retrieving hidden files: " + ex.getMessage());
            }
        }
    }

    private class UnhideFileListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try (Connection connection = DatabaseConnection.getConnection()) {
                String sql = "SELECT file_path FROM files WHERE is_hidden = true";
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql);
                List<String> hiddenFiles = new ArrayList<>();
                while (resultSet.next()) {
                    hiddenFiles.add(resultSet.getString("file_path"));
                }
                if (hiddenFiles.isEmpty()) {
                    view.showMessage("No hidden files to unhide.");
                    return;
                }
                String fileToUnhide = view.selectFileToUnhide(hiddenFiles.toArray(new String[0]));
                if (fileToUnhide != null) {
                    String updateSql = "UPDATE files SET is_hidden = false WHERE file_path = ?";
                    PreparedStatement updateStatement = connection.prepareStatement(updateSql);
                    updateStatement.setString(1, fileToUnhide);
                    updateStatement.executeUpdate();
                    view.showMessage("File unhidden successfully.");
                }
            } catch (SQLException ex) {
                view.showMessage("Error unhiding file: " + ex.getMessage());
            }
        }
    }
}