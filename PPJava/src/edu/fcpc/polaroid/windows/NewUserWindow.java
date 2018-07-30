package edu.fcpc.polaroid.windows;

import java.util.Optional;

import edu.fcpc.polaroid.SQLHelper;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

public class NewUserWindow extends Dialog<Pair<String, String>>{
	public NewUserWindow(){
		// Set string to be displayed in the screen 
		setTitle("New user");
		setHeaderText("Create a new user for the Digital Frame");
		
		// Set the buttons (Custom button and an Cancel button)
		ButtonType createButtonType = new ButtonType("Create User", ButtonData.OK_DONE);
		getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);
		
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		TextField username = new TextField();
		username.setPromptText("Username");
		PasswordField password = new PasswordField();
		password.setPromptText("Password");
		PasswordField repeatPassword = new PasswordField();
		repeatPassword.setPromptText("Repeat Password");

		grid.add(new Label("Username:"), 0, 0);
		grid.add(username, 1, 0);
		grid.add(new Label("Password:"), 0, 1);
		grid.add(password, 1, 1);
		grid.add(new Label("Repeat Password:"), 0, 2);
		grid.add(repeatPassword, 1, 2);
		
		Node createButton = getDialogPane().lookupButton(createButtonType);
		createButton.setDisable(true);
		password.setDisable(true);
		repeatPassword.setDisable(true);
		
		// Check if the Username field is available
		username.textProperty().addListener((observable, oldValue, newValue) -> {
			boolean hasNoValue = newValue.trim().isEmpty();
			boolean hasUsername = SQLHelper.hasUsername(newValue);
			password.setDisable(hasNoValue || hasUsername);
		});
		password.textProperty().addListener((observable, oldValue, newValue) -> {
			repeatPassword.setDisable(newValue.trim().isEmpty());
			if(newValue.trim().isEmpty())
				repeatPassword.setText("");
		});
		repeatPassword.textProperty().addListener((observable, oldValue, newValue) -> {
			if(newValue.equals((password.getText())))
				createButton.setDisable(false);
		});
		
		getDialogPane().setContent(grid);
		
		Platform.runLater(() -> username.requestFocus());
		setResultConverter(dialogButton -> {
		    if (dialogButton == createButtonType) {
		        return new Pair<>(username.getText(), password.getText());
		    }
		    return null;
		});
		Optional<Pair<String, String>> result = showAndWait();

		result.ifPresent(usernamePassword -> {
		    String userCreated = SQLHelper.createUser("","",1,1,1,usernamePassword.getKey(),
		    		                                   usernamePassword.getValue());
		    
		    Alert userCreatedPrompt;
		    if(userCreated.toUpperCase().equals("PASS")){
		    	userCreatedPrompt = new Alert(AlertType.INFORMATION);
			    userCreatedPrompt.setTitle("New User");
		    	userCreatedPrompt.setContentText("New user is now saved in the database");
		    	userCreatedPrompt.showAndWait();
		    }else{
		    	userCreatedPrompt = new Alert(AlertType.ERROR);
			    userCreatedPrompt.setTitle("New User");
			    userCreatedPrompt.setHeaderText("An error occured, refer to the message below");
		    	userCreatedPrompt.setContentText(userCreated);
		    	userCreatedPrompt.showAndWait();
		    }
		    
		});
		
	}
}
