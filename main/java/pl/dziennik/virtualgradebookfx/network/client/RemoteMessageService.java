package pl.dziennik.virtualgradebookfx.network.client;

import pl.dziennik.virtualgradebookfx.model.communication.Message;
import pl.dziennik.virtualgradebookfx.network.common.Request;
import pl.dziennik.virtualgradebookfx.network.common.RequestType;
import pl.dziennik.virtualgradebookfx.network.common.Response;
import pl.dziennik.virtualgradebookfx.service.interfaces.MessageService;

import java.util.Collections;
import java.util.List;

public class RemoteMessageService implements MessageService {

    private final NetworkClient networkClient = new NetworkClient();

    @Override
    public List<Message> getMessagesForUser(String receiverLogin) {
        Request request = new Request(RequestType.GET_MESSAGES_FOR_USER)
                .add("receiverLogin", receiverLogin);

        Response response = networkClient.send(request);

        if (!response.isSuccess()) {
            return Collections.emptyList();
        }

        return (List<Message>) response.getData();
    }

    @Override
    public List<Message> getSentMessagesForUser(String senderLogin) {
        Request request = new Request(RequestType.GET_SENT_MESSAGES_FOR_USER)
                .add("senderLogin", senderLogin);

        Response response = networkClient.send(request);

        if (!response.isSuccess()) {
            return Collections.emptyList();
        }

        return (List<Message>) response.getData();
    }

    @Override
    public void markMessageAsRead(int messageId) {
        Request request = new Request(RequestType.MARK_MESSAGE_AS_READ)
                .add("messageId", messageId);

        networkClient.send(request);
    }

    @Override
    public void sendMessage(String senderLogin, String receiverLogin, String subject, String content, String sentDate) {
        Request request = new Request(RequestType.SEND_MESSAGE)
                .add("senderLogin", senderLogin)
                .add("receiverLogin", receiverLogin)
                .add("subject", subject)
                .add("content", content)
                .add("sentDate", sentDate);

        networkClient.send(request);
    }
}