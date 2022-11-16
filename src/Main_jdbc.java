import java.sql.*;

public class Main_jdbc {
    static Connection connection = null;
    static Statement stmt = null;

    public static void main(String[] args) {

        try {
            //Установка соединения с БД с помощью JDBC драйвера
            new com.mysql.jdbc.Driver();
            connection = DriverManager.getConnection("jdbc:mysql://IP:3306/TABLE?user=USER&password=PASS&characterEncoding=UTF-8");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


        try {
            stmt = connection.createStatement();
        } catch (SQLException e) {
            //Закрытие соединения в случае ошибки
            closeConnection();
            throw new RuntimeException(e);
        }

        try {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS data " +
                    "(name VARCHAR(255), " +
                    " cash INTEGER, " +
                    " PRIMARY KEY ( name ))");
        } catch (SQLException e) {
            //Закрытие соединения в случае ошибки
            closeConnection();
            throw new RuntimeException(e);
        }

        //Вставка данных
        try {
            stmt.executeUpdate("INSERT IGNORE INTO data VALUES ('test', 25)");
            stmt.executeUpdate("INSERT IGNORE INTO data VALUES ('test2', 25)");
        } catch (SQLException e) {
            //Закрытие соединения в случае ошибки
            closeConnection();
            throw new RuntimeException(e);
        }

        //Обновление данных
        try {
            stmt.executeUpdate("UPDATE data SET `cash` = 100 WHERE `name` = 'test'");
        } catch (SQLException e) {
            //Закрытие соединения в случае ошибки
            closeConnection();
            throw new RuntimeException(e);
        }

        //Получение данных
        try (ResultSet rs = stmt.executeQuery("SELECT * from `data`")) {
            while (rs.next()) {
                System.out.println(rs.getString(1) + ":" + rs.getInt(2));
            }
        } catch (SQLException e) {
            //Закрытие соединения в случае ошибки
            closeConnection();
            throw new RuntimeException(e);
        }

        //Удаление данных
        try {
            stmt.executeUpdate("DELETE FROM data WHERE `name` = 'test2'");
        } catch (SQLException e) {
            //Закрытие соединения в случае ошибки
            closeConnection();
            throw new RuntimeException(e);
        }

        System.out.println("Отправляем в таблицу logs несколько запросов в транзакции, один с ошибкой");
        testTransaction(true);
        getDataFromLogsTable();
        System.out.println("Отправляем в таблицу logs несколько запросов в транзакции");
        testTransaction(false);
        getDataFromLogsTable();

        closeConnection();
    }

    private static void getDataFromLogsTable() {
        System.out.println("Получение данных из таблицы logs");
        //Получение данных
        try (ResultSet rs = stmt.executeQuery("SELECT * from `logs`")) {
            while (rs.next()) {
                System.out.println(rs.getInt(1) + ":" + rs.getString(2) + ":" + rs.getString(3));
            }
        } catch (SQLException e) {
            //Закрытие соединения в случае ошибки
            closeConnection();
            throw new RuntimeException(e);
        }
    }

    private static void testTransaction(boolean failure) {
        try {
            connection.setAutoCommit(false);

            stmt.executeUpdate("UPDATE data SET `cash` = 150 WHERE `name` = 'test'");
            if (failure) {
                //Ошибка для проверки отката транзакции
                stmt.executeUpdate("INSERT INTO logs( action) VALUES ('test', 'Зачислено 150')");
            }
            stmt.executeUpdate("INSERT INTO logs(name, action) VALUES ('test', 'Зачислено 150')");

            connection.commit();
        } catch (SQLException e) {
            try {
                //Откат транзакции
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        try {
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    static void closeConnection() {
        try {
            stmt.close();
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}