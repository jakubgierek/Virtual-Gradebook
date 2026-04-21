package pl.dziennik.virtualgradebookfx.network.common;

public enum RequestType {
    LOGIN,
    GET_STUDENT_SUBJECTS,
    GET_STUDENT_GRADES,
    GET_TIMETABLE_FOR_CLASS,

    GET_MESSAGES_FOR_USER,
    GET_SENT_MESSAGES_FOR_USER,
    SEND_MESSAGE,
    MARK_MESSAGE_AS_READ,
    GET_ALL_USERS_EXCEPT
}