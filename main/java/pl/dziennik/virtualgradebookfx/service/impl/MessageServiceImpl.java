package pl.dziennik.virtualgradebookfx.service.impl;

import pl.dziennik.virtualgradebookfx.model.communication.Message;
import pl.dziennik.virtualgradebookfx.persistence.DatabaseConnection;
import pl.dziennik.virtualgradebookfx.service.interfaces.MessageService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MessageServiceImpl implements MessageService {

    private Message mapMessage(ResultSet rs) throws SQLException {
        return new Message(
                rs.getInt("id"),
                rs.getString("sender_login"),
                rs.getString("receiver_login"),
                rs.getString("subject"),
                rs.getString("content"),
                rs.getString("sent_date"),
                rs.getBoolean("is_read")
        );
    }
    @Override
    public List<Message> getMessagesForUser(String receiverLogin) {
        String sql = "SELECT * FROM messages WHERE receiver_login = ? ORDER BY sent_date DESC";
        List<Message> messages = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, receiverLogin);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapMessage(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return messages;
    }

    @Override
    public List<Message> getSentMessagesForUser(String senderLogin) {
        String sql = "SELECT * FROM messages WHERE sender_login = ? ORDER BY sent_date DESC";
        List<Message> messages = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, senderLogin);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapMessage(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return messages;
    }

    @Override
    public void markMessageAsRead(int messageId) {
        String sql = "UPDATE messages SET is_read = TRUE WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, messageId);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendMessage(String senderLogin, String receiverLogin, String subject, String content, String sentDate) {
        String sql = "INSERT INTO messages (sender_login, receiver_login, subject, content, sent_date, is_read) VALUES (?, ?, ?, ?, ?, FALSE)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, senderLogin);
            statement.setString(2, receiverLogin);
            statement.setString(3, subject);
            statement.setString(4, content);
            statement.setString(5, sentDate);

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
