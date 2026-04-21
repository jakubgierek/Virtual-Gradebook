package pl.dziennik.virtualgradebookfx.persistence;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseTest {
    public static void main(String[] args) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            System.out.println("Polaczenie z baza danych udane!");
        } catch (SQLException e) {
            System.out.println("Blad polaczenia z baza danych.");
            e.printStackTrace();
        }
    }
}