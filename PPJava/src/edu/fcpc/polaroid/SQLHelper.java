package edu.fcpc.polaroid;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SQLHelper {
	public static void prepareConnection(){
		try{
			writeDump("Starting to prepare SQL connection");
			Class.forName("org.sqlite.JDBC");
			Connection connection = DriverManager.getConnection("jdbc:sqlite:app_DigitalFrame.db");
			connection.setAutoCommit(false);
			Statement statement = connection.createStatement();
			
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS APP_CREDENTIALS2(usernumber INTEGER PRIMARY KEY AUTOINCREMENT, lastname TEXT, firstname TEXT, bdaym INTEGER, bdayd INTEGER, bdayy INTEGER, username TEXT, password TEXT);");
			
			statement.close();
			connection.commit();
			connection.close();
			writeDump("Closing to prepare SQL connection");
		}catch(ClassNotFoundException | SQLException sqle){
			sqle.printStackTrace();
		}
	}
	
	public static boolean hasUsername(String username){
		try{
			writeDump("Checking username instance");
			Class.forName("org.sqlite.JDBC");
			boolean hasRecord = false;
			
			Connection connection = DriverManager.getConnection("jdbc:sqlite:app_DigitalFrame.db");
			connection.setAutoCommit(false);
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery("SELECT username FROM APP_CREDENTIALS2 WHERE username = '" + username + "';");
			boolean hasValues = result.next();
			if (!hasValues){
				statement.close();
				connection.commit();
				connection.close();
				return false;
			}
			String returnUsername = result.getString(1);
			if(returnUsername.equals(username))
				hasRecord = true;
			statement.close();
			connection.commit();
			connection.close();

			writeDump("Done checking username instance");
			return hasRecord;
		}catch(ClassNotFoundException | SQLException sqle){
			writeDump("!!!!" + sqle.getMessage());
			return false;
		}
	}
	
	public static String createUser(String lastname, String firstname, int bdaym, int bdayd, int bdayy, String username, String password){
		try{
			writeDump("Creating a user");
			Class.forName("org.sqlite.JDBC");
			Connection connection = DriverManager.getConnection("jdbc:sqlite:app_DigitalFrame.db");
			connection.setAutoCommit(false);
			writeDump("|-> Connection open " + connection.toString());
			//Statement statement = connection.createStatement();
			PreparedStatement statement = connection.prepareStatement("INSERT INTO APP_CREDENTIALS2(lastname, firstname, bdaym, bdayd, bdayy, username, password) VALUES (?,?,?,?,?,?,?);");
			statement.setString(1, lastname);
			statement.setString(2, firstname);
			statement.setInt(3, bdaym);
			statement.setInt(4, bdayd);
			statement.setInt(5, bdayy);
			statement.setString(6, username);
			statement.setString(7, password);
			writeDump("|-> Statement open " + statement.toString());
			int returnValue = statement.executeUpdate();
			connection.commit();
			writeDump("|-> Statement open " + String.valueOf(returnValue));
			statement.close();
			writeDump("|-> Statement close");
			connection.commit();
			connection.close();
			writeDump("|-> Connection close");
			writeDump("Done creating user");
			return "PASS";
		}catch(ClassNotFoundException | SQLException sqle){
			return sqle.getMessage();
		}
	}
	
	public static boolean loginUser(String username, String password){
		try{
			writeDump("Logging in the user");
			Class.forName("org.sqlite.JDBC");
			boolean hasRecord = false;
			
			Connection connection = DriverManager.getConnection("jdbc:sqlite:app_DigitalFrame.db");
			connection.setAutoCommit(false);
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery("SELECT username, password FROM APP_CREDENTIALS2 WHERE username = '" + username + "';");
			boolean hasValues = result.next();
			if (!hasValues){
				statement.close();
				connection.commit();
				connection.close();
				return false;
			}
			String returnUsername = result.getString(1);
			String returnPassword = result.getString(2);
			if(returnUsername.equals(username) &&
			   returnPassword.equals(password))
				hasRecord = true;
			statement.close();
			connection.commit();
			connection.close();

			writeDump("Done logging in");
			return hasRecord;
		}catch(ClassNotFoundException | SQLException sqle){
			return false;
		}
	}
	
	public static void writeDump(String message){
		try{
			FileWriter fileWriter = new FileWriter("sqlDump.txt", true);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.append("[");
			bufferedWriter.append(new SimpleDateFormat("MMddyyyy HHmmss").format(Calendar.getInstance().getTime()));
			bufferedWriter.append("] "+ message + "\n");
			bufferedWriter.close();
			fileWriter.close();
		}catch(IOException e){
		
		}
	}
}
