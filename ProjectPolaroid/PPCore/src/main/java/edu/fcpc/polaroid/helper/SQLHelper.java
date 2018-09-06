package edu.fcpc.polaroid.helper;

import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLHelper {
    public static void prepareConnection() {
        try {
            LoggerFactory.getLogger(SQLHelper.class).info("Starting to prepare SQL connection");
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:app_DigitalFrame.db");
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS APP_CREDENTIALS2(usernumber INTEGER PRIMARY KEY AUTOINCREMENT, lastname TEXT, firstname TEXT, bdaym INTEGER, bdayd INTEGER, bdayy INTEGER, username TEXT, password TEXT);");

            statement.close();
            connection.commit();
            connection.close();
            LoggerFactory.getLogger(SQLHelper.class).info("Closing to prepare SQL connection");
        } catch (ClassNotFoundException | SQLException sqle) {
            LoggerFactory.getLogger(SQLHelper.class).warn(sqle.getMessage());
            sqle.printStackTrace();
        }
    }

    public static boolean hasUsername(String username) {
        try {
            LoggerFactory.getLogger(SQLHelper.class).info("Checking username instance");
            Class.forName("org.sqlite.JDBC");
            boolean hasRecord = false;

            Connection connection = DriverManager.getConnection("jdbc:sqlite:app_DigitalFrame.db");
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT username FROM APP_CREDENTIALS2 WHERE username = '" + username + "';");
            boolean hasValues = result.next();
            if (!hasValues) {
                statement.close();
                connection.commit();
                connection.close();
                return false;
            }
            String returnUsername = result.getString(1);
            if (returnUsername.equals(username))
                hasRecord = true;
            statement.close();
            connection.commit();
            connection.close();

            LoggerFactory.getLogger(SQLHelper.class).info("Done checking username instance");
            return hasRecord;
        } catch (ClassNotFoundException | SQLException sqle) {
            LoggerFactory.getLogger(SQLHelper.class).warn(sqle.getMessage());
            return false;
        }
    }

    public static String createUser(String lastname, String firstname, int bdaym, int bdayd, int bdayy, String username, String password) {
        try {
            LoggerFactory.getLogger(SQLHelper.class).info("Creating a user");
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:app_DigitalFrame.db");
            connection.setAutoCommit(false);
            LoggerFactory.getLogger(SQLHelper.class).info("|-> Connection open " + connection.toString());
            //Statement statement = connection.createStatement();
            PreparedStatement statement = connection.prepareStatement("INSERT INTO APP_CREDENTIALS2(lastname, firstname, bdaym, bdayd, bdayy, username, password) VALUES (?,?,?,?,?,?,?);");
            statement.setString(1, lastname);
            statement.setString(2, firstname);
            statement.setInt(3, bdaym);
            statement.setInt(4, bdayd);
            statement.setInt(5, bdayy);
            statement.setString(6, username);
            statement.setString(7, password);
            LoggerFactory.getLogger(SQLHelper.class).info("|-> Statement open " + statement.toString());
            int returnValue = statement.executeUpdate();
            connection.commit();
            LoggerFactory.getLogger(SQLHelper.class).info("|-> Statement open " + String.valueOf(returnValue));
            statement.close();
            LoggerFactory.getLogger(SQLHelper.class).info("|-> Statement close");
            connection.commit();
            connection.close();
            LoggerFactory.getLogger(SQLHelper.class).info("|-> Connection close");
            LoggerFactory.getLogger(SQLHelper.class).info("Done creating user");
            return "PASS";
        } catch (ClassNotFoundException | SQLException sqle) {
            LoggerFactory.getLogger(SQLHelper.class).warn(sqle.getMessage());
            return sqle.getMessage();
        }
    }

    public static boolean loginUser(String username, String password) {
        try {
            LoggerFactory.getLogger(SQLHelper.class).info("Logging in the user");
            Class.forName("org.sqlite.JDBC");
            boolean hasRecord = false;

            Connection connection = DriverManager.getConnection("jdbc:sqlite:app_DigitalFrame.db");
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT username, password FROM APP_CREDENTIALS2 WHERE username = '" + username + "';");
            boolean hasValues = result.next();
            if (!hasValues) {
                statement.close();
                connection.commit();
                connection.close();
                return false;
            }
            String returnUsername = result.getString(1);
            String returnPassword = result.getString(2);
            if (returnUsername.equals(username) &&
                    returnPassword.equals(password))
                hasRecord = true;
            statement.close();
            connection.commit();
            connection.close();

            LoggerFactory.getLogger(SQLHelper.class).info("Done logging in");
            return hasRecord;
        } catch (ClassNotFoundException | SQLException sqle) {
            LoggerFactory.getLogger(SQLHelper.class).warn(sqle.getMessage());
            return false;
        }
    }
}
