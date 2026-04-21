package pl.dziennik.virtualgradebookfx.model.communication;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String senderLogin;
    private String receiverLogin;
    private String subject;
    private String content;
    private String sentDate;
    private boolean isRead;

    public Message() {
    }

    public Message(int id, String senderLogin, String receiverLogin, String subject, String content, String sentDate, boolean isRead) {
        this.id = id;
        this.senderLogin = senderLogin;
        this.receiverLogin = receiverLogin;
        this.subject = subject;
        this.content = content;
        this.sentDate = sentDate;
        this.isRead = isRead;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getSenderLogin() {
        return senderLogin;
    }

    public void setSenderLogin(String senderLogin) {
        this.senderLogin = senderLogin;
    }

    public String getReceiverLogin() {
        return receiverLogin;
    }

    public void setReceiverLogin(String receiverLogin) {
        this.receiverLogin = receiverLogin;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSentDate() {
        return sentDate;
    }

    public void setSentDate(String sentDate) {
        this.sentDate = sentDate;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    @Override
    public String toString() {
        return sentDate + " | " + senderLogin + " -> " + receiverLogin + " | " + subject;
    }
}