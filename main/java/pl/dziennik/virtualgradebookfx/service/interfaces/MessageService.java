package pl.dziennik.virtualgradebookfx.service.interfaces;

import pl.dziennik.virtualgradebookfx.model.communication.Message;

import java.util.List;

public interface MessageService {
    List<Message> getMessagesForUser(String receiverLogin);
    List<Message> getSentMessagesForUser(String senderLogin);
    void markMessageAsRead(int messageId);
    void sendMessage(String senderLogin, String receiverLogin, String subject, String content, String sentDate);
}