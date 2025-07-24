package qcs;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

/**
 * Class to execute the actions needed from layout class
 * 
 * @author Bianca
 * @author David
 */
public class ActionsClass {

	TextArea textBox;
	String selectedGate;
	String selectedGateColor;
	GridPane circuitGrid;
	Scene scene;
	
	/**
	 * constructor
	 * @param text
	 */
	ActionsClass(TextArea text) {
		this.textBox = text;
	}

	/**
	 * setter for placing the qubits on the grid
	 * @param gate
	 * @param color
	 */
	public void setSelectedGate(String gate, String color) {
        this.selectedGate = gate;
        this.selectedGateColor = color;
    }

	/**
	 * getter for selected qubit
	 * @return
	 */
    public String getSelectedGate() {
        return selectedGate;
    }

    /**
     * getter for qubit colour
     * @return
     */
    public String getSelectedGateColor() {
        return selectedGateColor;
    }
    
    /**
     * function to clear gate choice after its been used
     */
    public void clearSelectedGate() {
        selectedGate = null;
        selectedGateColor = null;
    }
    
    /**
     * function for displaying the about info for the application
     */
    public void showAbout() {
    	Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Quantum Circuit Simulator");
        alert.setHeaderText("About");
        
        Image img = new Image(getClass().getResourceAsStream("qcs.png"));
        ImageView logo = new ImageView(img);
        logo.setFitHeight(50);
        logo.setPreserveRatio(true);
        alert.setGraphic(logo);
        
        alert.setContentText(
            "Version: 1.0\n" +
            "Created by: David Lainez and Bianca Reaney Ibarra\n\n" +
            "This program allows users to design and simulate quantum circuits using basic and advanced quantum gates.\n\n" +
            "This is a JavaFX-based interface with click and paste support, live gate editing, and state visualization.\n\n" +
            "For more information, refer to the ReadMe or visit our GitHub at https://github.com/lain0053/CST8221."
            
        );
        alert.setResizable(true);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }
    
    /**
     * function for changing the look and feel
     * @param LookAndFeel
     */
    public void changeAppearance(String LookAndFeel) {
    	
    	 String cssFile = switch (LookAndFeel) {
         case "Dark" -> "dark.css";
         case "Nimbus" -> "nimbus.css";
         case "Windows" -> "windows.css";
         case "Metal" -> "metal.css";
         default -> null;
     };
     scene.getStylesheets().clear();
     
     if (cssFile != null) {
         URL url = getClass().getResource(cssFile);
         if (url == null) {
             textBox.appendText("Failed to load CSS file: " + cssFile + "\n");
         } else {
             scene.getStylesheets().add(url.toExternalForm());
             textBox.appendText("Appearance changed to " + LookAndFeel + "\n");
         }
     } else {
         textBox.appendText("Reverted to default appearance\n");
     }
    }
	
}
