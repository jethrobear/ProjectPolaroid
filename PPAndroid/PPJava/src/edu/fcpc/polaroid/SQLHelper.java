package edu.fcpc.polaroid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLHelper {
    public static void prepareConnection() {
        Logger logger = LoggerFactory.getLogger(SQLHelper.class);
        try {
            logger.info("Starting to prepare SQL connection");
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:app_DigitalFrame.db");
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS APP_CREDENTIALS2(usernumber INTEGER PRIMARY KEY AUTOINCREMENT, lastname TEXT, firstname TEXT, bdaym INTEGER, bdayd INTEGER, bdayy INTEGER, username TEXT, password TEXT);");

            statement.close();
            connection.commit();
            connection.close();
            logger.info("Closing to prepare SQL connection");
        } catch (ClassNotFoundException | SQLException sqle) {
            logger.warn(sqle.getMessage());
            sqle.printStackTrace();
        }
    }

    public static boolean hasUsername(String username) {
        Logger logger = LoggerFactory.getLogger(SQLHelper.class);
        try {
            logger.info("Checking username instance");
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

            logger.info("Done checking username instance");
            return hasRecord;
        } catch (ClassNotFoundException | SQLException sqle) {
            logger.warn(sqle.getMessage());
            return false;
        }
    }

    public static String createUser(String lastname, String firstname, int bdaym, int bdayd, int bdayy, String username, String password) {
        Logger logger = LoggerFactory.getLogger(SQLHelper.class);
        try {
            logger.info("Creating a user");
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:app_DigitalFrame.db");
            connection.setAutoCommit(false);
            logger.info("|-> Connection open " + connection.toString());
            //Statement statement = connection.createStatement();
            PreparedStatement statement = connection.prepareStatement("INSERT INTO APP_CREDENTIALS2(lastname, firstname, bdaym, bdayd, bdayy, username, password) VALUES (?,?,?,?,?,?,?);");
            statement.setString(1, lastname);
            statement.setString(2, firstname);
            statement.setInt(3, bdaym);
            statement.setInt(4, bdayd);
            statement.setInt(5, bdayy);
            statement.setString(6, username);
            statement.setString(7, password);
            logger.info("|-> Statement open " + statement.toString());
            int returnValue = statement.executeUpdate();
            connection.commit();
            logger.info("|-> Statement open " + String.valueOf(returnValue));
            statement.close();
            logger.info("|-> Statement close");
            connection.commit();
            connection.close();
            logger.info("|-> Connection close");
            logger.info("Done creating user");
            return "PASS";
        } catch (ClassNotFoundException | SQLException sqle) {
            logger.warn(sqle.getMessage());
            return sqle.getMessage();
        }
    }

    public static boolean loginUser(String username, String password) {
        Logger logger = LoggerFactory.getLogger(SQLHelper.class);
        try {
            logger.info("Logging in the user");
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

            logger.info("Done logging in");
            return hasRecord;
        } catch (ClassNotFoundException | SQLException sqle) {
            logger.warn(sqle.getMessage());
            return false;
        }
    }
}
