module pl.dziennik.virtualgradebookfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    exports pl.dziennik.virtualgradebookfx.app;
    exports pl.dziennik.virtualgradebookfx.controller;
    exports pl.dziennik.virtualgradebookfx.model.user;
    exports pl.dziennik.virtualgradebookfx.model.school;
    exports pl.dziennik.virtualgradebookfx.model.communication;
    exports pl.dziennik.virtualgradebookfx.service.interfaces;
    exports pl.dziennik.virtualgradebookfx.service.impl;
    exports pl.dziennik.virtualgradebookfx.persistence;
    exports pl.dziennik.virtualgradebookfx.util;
    exports pl.dziennik.virtualgradebookfx.network.common;
    exports pl.dziennik.virtualgradebookfx.network.client;
    exports pl.dziennik.virtualgradebookfx.network.server;

    opens pl.dziennik.virtualgradebookfx.app to javafx.fxml;
    opens pl.dziennik.virtualgradebookfx.controller to javafx.fxml;
}