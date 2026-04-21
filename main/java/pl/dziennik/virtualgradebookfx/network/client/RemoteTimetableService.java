package pl.dziennik.virtualgradebookfx.network.client;

import pl.dziennik.virtualgradebookfx.model.school.TimetableEntry;
import pl.dziennik.virtualgradebookfx.network.common.Request;
import pl.dziennik.virtualgradebookfx.network.common.RequestType;
import pl.dziennik.virtualgradebookfx.network.common.Response;
import pl.dziennik.virtualgradebookfx.service.interfaces.TimetableService;

import java.util.Collections;
import java.util.List;

public class RemoteTimetableService implements TimetableService {

    private final NetworkClient networkClient = new NetworkClient();

    @Override
    public List<TimetableEntry> getTimetableForClass(String className) {
        Request request = new Request(RequestType.GET_TIMETABLE_FOR_CLASS)
                .add("className", className);

        Response response = networkClient.send(request);

        if (!response.isSuccess()) {
            return Collections.emptyList();
        }

        return (List<TimetableEntry>) response.getData();
    }
}