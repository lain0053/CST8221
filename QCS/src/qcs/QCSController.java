package qcs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * @author Bianca & Davod
 * JAP CST8221 
 * Professor: Paulo Sousa
 * Program to create an interface for a quantum circuit simulator
 * 
 * Controller class for handling actions
 */

public class QCSController {

	TextArea textBox;
	TextArea codeBox;
	private String selectedGate;
	private String selectedGateColour;
	private Scene scene;
	private ResourceBundle bundle;
	private Button[][] gridButtons;
	private int gridRows;
	private int gridColumns;
	private QCSView view;
	int currentStepColumn = 0;
	private String multiGateInProgress = null;
	private int remainingMultiPlacements = 0;

	/**
	 * Constructor
	 * @param textBox
	 * @param bundle
	 */
	public QCSController(TextArea textBox, ResourceBundle bundle) {
		this.textBox = textBox;
		this.bundle = bundle;
	}

	/**
	 * setting view variable
	 * @param view
	 */
	public void setView(QCSView view) {
		this.view = view;
	}

	/**
	 * setting the variables for the grid spaces
	 * @param gridButtons
	 * @param rows
	 * @param columns
	 */
	public void setGridButtons(Button[][] gridButtons, int rows, int columns) {
		this.gridButtons = gridButtons;
		this.gridRows = rows;
		this.gridColumns = columns;
	}

	/**
	 * scene variable to be used in changing appearance, language, etc.
	 * @param scene
	 */
	public void setScene(Scene scene) {
		this.scene = scene;
	}

	/**
	 * setting the selected qubit name on the grid
	 * @param gate
	 * @param colour
	 */
	public void setSelectedGate(String gate, String colour) {
	    if (gate == null || colour == null) {
	        try {
	            throw new QCSException("Gate or colour cannot be null.");
	        } catch (QCSException e) {
	            textBox.appendText("Error: " + e.getMessage() + "\n");
	            return;
	        }
	    }
	    this.selectedGate = gate;
	    this.selectedGateColour = colour;

	    if (gate.equals("CX") || gate.equals("SWAP") || gate.equals("CU")) {
	        multiGateInProgress = gate;
	        remainingMultiPlacements = 2;
	    } else if (gate.equals("CCX")) {
	        multiGateInProgress = gate;
	        remainingMultiPlacements = 3;
	    } else {
	        multiGateInProgress = null;
	        remainingMultiPlacements = 0;
	    }
	}

	/**
	 * getting the selected qubit
	 * @return
	 */
	public String getSelectedGate() {
		return selectedGate;
	}

	/**
	 * getter for qubit gate colour
	 * @return
	 */
	public String getSelectedGateColour() {
		return selectedGateColour;
	}

	/**
	 * setting the selected quibit gate colour
	 * @param colour
	 */
	public void setSelectedGateColour(String colour) {
		this.selectedGateColour = colour;
	}

	/**
	 * clearing the gate after placing it
	 */
	public void clearSelectedGate() {
		selectedGate = null;
		selectedGateColour = null;
	}

	/**
	 * code area variable
	 * @param codeBox
	 */
	public void setCodeBox(TextArea codeBox) {
		this.codeBox = codeBox;
	}

	/**
	 * saving the circuit to a Qiskit file
	 */
	public void saveCircuit() {
	    try {
	        if (gridButtons == null) {
	            throw new QCSException("Grid not set. Cannot save circuit.");
	        }

	        Window window = codeBox.getScene().getWindow();
	        FileChooser fileChooser = new FileChooser();
	        fileChooser.setTitle("Save Quantum Circuit");
	        fileChooser.getExtensionFilters().add(
	            new FileChooser.ExtensionFilter("Qiskit Circuit Files (*.qc)", "*.qc")
	        );

	        File file = fileChooser.showSaveDialog(window);
	        if (file == null) return;

	        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
	            writer.write("# Circuit grid gates\n");
	            for (int row = 0; row < gridRows; row++) {
	                for (int col = 0; col < gridColumns; col++) {
	                    Button btn = gridButtons[row][col];
	                    String gate = btn.getText();
	                    if (gate != null && !gate.isEmpty()) {
	                        String qiskitCommand = gateToQiskitCommand(gate, row, col);
	                        if (qiskitCommand != null)
	                            writer.write(qiskitCommand + "\n");
	                    }
	                }
	            }

	            writer.write("\n# Code Box\n");
	            writer.write(codeBox.getText());

	            textBox.appendText("Circuit saved to " + file.getName() + "\n");

	        } catch (IOException e) {
	            throw new QCSException("Error saving file: " + e.getMessage());
	        }

	    } catch (QCSException e) {
	        textBox.appendText("Error: " + e.getMessage() + "\n");
	    }
	}

	/**
	 * loading Qiskit files into the circuit
	 */
	public void loadCircuit() {
	    try {
	        if (gridButtons == null) {
	            throw new QCSException("Grid not set. Cannot load circuit.");
	        }

	        Window window = codeBox.getScene().getWindow();

	        FileChooser fileChooser = new FileChooser();
	        fileChooser.setTitle("Load Quantum Circuit");
	        fileChooser.getExtensionFilters().add(
	            new FileChooser.ExtensionFilter("Qiskit Circuit Files (*.qc)", "*.qc")
	        );

	        File file = fileChooser.showOpenDialog(window);
	        if (file == null) return;

	        try {
	            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
	            String[] parts = content.split("(?m)^# Code Box\\s*$");
	            if (parts.length < 2) {
	                throw new QCSException("Invalid file format.");
	            }

	            String gridSection = parts[0];
	            String codeSection = parts[1];

	            for (int r = 0; r < gridRows; r++) {
	                for (int c = 0; c < gridColumns; c++) {
	                    Button btn = gridButtons[r][c];
	                    btn.setText("");
	                    btn.setStyle("-fx-border-colour: black; -fx-border-width: 1;");
	                }
	            }

	            BufferedReader reader = new BufferedReader(new StringReader(gridSection));
	            String line;
	            while ((line = reader.readLine()) != null) {
	                line = line.trim();
	                if (line.isEmpty() || line.startsWith("#")) continue;
	                parseAndApplyGate(line);
	            }

	            codeBox.setText(codeSection.trim());
	            textBox.appendText("Circuit loaded from " + file.getName() + "\n");

	        } catch (IOException e) {
	            throw new QCSException("Error loading file: " + e.getMessage());
	        }

	    } catch (QCSException e) {
	        textBox.appendText("Error: " + e.getMessage() + "\n");
	    }
	}

	/**
	 * pop up for the about screen
	 */
	public void showAbout() {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(bundle.getString("menu.about"));
		alert.setHeaderText(bundle.getString("menu.about"));

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
	 * pop up for the readme screen
	 */
	public void showReadme() {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(bundle.getString("menu.readme"));
		alert.setHeaderText(bundle.getString("menu.readme"));
		alert.setContentText("ReadMe etc.\n\nBianca & David");
		alert.setResizable(true);
		alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
		alert.showAndWait();
	}
	
	/**
	 * Converts a gate and its position to a Qiskit-compatible command.
	 * @param gate
	 * @param row
	 * @param col
	 * @return
	 */
	private String gateToQiskitCommand(String gate, int row, int col) {
		try {
			switch (gate.toUpperCase()) {
				case "I": return "qc.id(" + row + ", " + col + ")";
				case "X": return "qc.x(" + row + ", " + col + ")";
				case "Y": return "qc.y(" + row + ", " + col + ")";
				case "Z": return "qc.z(" + row + ", " + col + ")";
				case "H": return "qc.h(" + row + ", " + col + ")";
				case "S": return "qc.s(" + row + ", " + col + ")";
				case "T": return "qc.t(" + row + ", " + col + ")";
				case "U": return "qc.u(" + row + ", " + col + ", 0)";
				case "CX": return "qc.cx(" + row + ", " + (col + 1) + ")";
				case "SWAP": return "qc.swap(" + row + ", " + (col + 1) + ")";
				case "CU": return "qc.cu(" + row + ", " + (col + 1) + ")";
				case "CCX": return "qc.ccx(" + row + ", " + (col + 1) + ", " + (col + 2) + ")";
				case "BARRIER": return "qc.barrier(" + row + ", " + col + ")";
				default: throw new QCSException("Unrecognized gate: " + gate);
			}
		// Throws a QCSException if the gate is unrecognized.
		} catch (QCSException e) {
			textBox.appendText("Error: " + e.getMessage() + "\n");
			return null;
		}
	}

	/**
	 * Parses a line of Qiskit code and applies the gate to the appropriate button on the grid.
	 * @param line
	 */
	private void parseAndApplyGate(String line) {
		if (!line.startsWith("qc.")) return;

		try {
			line = line.substring(3); // Remove 'qc.'

			int openParen = line.indexOf('(');
			int closeParen = line.indexOf(')');
			if (openParen < 0 || closeParen < 0)
				throw new QCSException("Malformed Qiskit command: " + line);

			String gateName = line.substring(0, openParen).toUpperCase();
			String args = line.substring(openParen + 1, closeParen);
			String[] tokens = args.split(",");

			int[] qubits = new int[tokens.length];
			for (int i = 0; i < tokens.length; i++) {
				qubits[i] = Integer.parseInt(tokens[i].trim());
			}

			int row = qubits[0];
			int col = 0;

			switch (gateName) {
				case "ID", "U", "X", "Y", "Z", "H", "S", "T":
					col = (qubits.length >= 2) ? qubits[1] : 0;
					break;
				case "CX", "SWAP", "CU":
					if (qubits.length >= 2) {
						col = qubits[1] - 1;
						if (col < 0) col = 0;
					}
					break;
				case "CCX":
					if (qubits.length >= 3) {
						col = qubits[1] - 1;
						if (col < 0) col = 0;
					}
					break;
				case "BARRIER":
					col = (qubits.length >= 2) ? qubits[1] : 0;
					break;
				default:
					throw new QCSException("Unsupported gate type in file: " + gateName);
			}

			if (row < 0 || row >= gridRows || col < 0 || col >= gridColumns)
				throw new QCSException("Gate position out of bounds (row: " + row + ", col: " + col + ")");

			Button btn = gridButtons[row][col];
			if (btn == null) throw new QCSException("Grid button is null at (" + row + "," + col + ")");

			String btnText = switch (gateName) {
				case "ID" -> "I";
				case "U" -> "U";
				case "CCX" -> "CCX";
				case "CX" -> "CX";
				case "SWAP" -> "SWAP";
				case "CU" -> "CU";
				default -> gateName;
			};

			btn.setText(btnText);
			btn.setStyle("-fx-background-colour: " + gatecolourFromName(btnText) +
			             "; -fx-border-colour: black; -fx-border-width: 1px;");

		// Throws QCSException for malformed or invalid commands.
		} catch (NumberFormatException e) {
			textBox.appendText("Error parsing integers in line: " + line + "\n");
		} catch (QCSException e) {
			textBox.appendText("Error: " + e.getMessage() + "\n");
		}
	}

	/**
	 * getting the default gate colours
	 * @param gate
	 * @return
	 */
	private String gatecolourFromName(String gate) {
		return switch (gate.toUpperCase()) {
		case "I" -> "#ffadad";
		case "X" -> "#ffd6a5";
		case "Y" -> "#fdffb6";
		case "Z" -> "#caffbf";
		case "H" -> "#9bf6ff";
		case "S" -> "#a0c4ff";
		case "T" -> "#bdb2ff";
		case "U" -> "#ffc6ff";
		case "CX" -> "#d0f4de";
		case "SWAP" -> "#fef9c7";
		case "CU" -> "#fcd5ce";
		case "CCX" -> "#cdb4db";
		case "BARRIER" -> "#cacccb";
		default -> "lightgray";
		};
	}

	/**
	 * calling the css files and changing the look and feel
	 * @param lookAndFeel
	 */
	public void changeAppearance(String lookAndFeel) {
		String cssFile = switch (lookAndFeel) {
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
				textBox.appendText("Appearance changed to " + lookAndFeel + "\n");
			}
		} else {
			textBox.appendText("Reverted to default appearance\n");
		}
	}

	/**
	 * appending the grid selections to the code area on the right
	 */
	public void updateCodeBoxFromGrid() {
		if (gridButtons == null) return;

		StringBuilder code = new StringBuilder();

		for (int r = 0; r < gridRows; r++) {
			for (int c = 0; c < gridColumns; c++) {
				Button btn = gridButtons[r][c];
				String gate = btn.getText();
				if (gate != null && !gate.isEmpty()) {
					String cmd = gateToQiskitCommand(gate, r, c);
					if (cmd != null) {
						code.append(cmd).append("\n");
					}
				}
			}
		}

		codeBox.setText(code.toString());
	}

	/**
	 * pop up for the qubit colour picker
	 */
	public void showColourCustomizationWindow() {
		Stage popupStage = new Stage();
		popupStage.setTitle("Customize Gate Button colours");

		VBox box = new VBox(10);
		box.setPadding(new Insets(10));
		box.setAlignment(Pos.CENTER_LEFT);

		Map<String, String> gateColours = view.getGateColours();

		for (Map.Entry<String, Button> entry : view.getGateButtons().entrySet()) {
			String gate = entry.getKey();
			Button gateButton = entry.getValue();

			Button clone = new Button(gate);
			clone.setStyle(gateButton.getStyle());
			clone.setPrefWidth(100);

			clone.setOnAction(e -> {
				ColorPicker picker = new ColorPicker();
				Stage colourStage = new Stage();
				VBox colourBox = new VBox(10, new Label("Select colour for " + gate), picker);
				colourBox.setAlignment(Pos.CENTER);
				colourBox.setPadding(new Insets(10));

				picker.setOnAction(ev -> {
					String fxColour = toHex(picker.getValue());

					// Save new colour
					gateColours.put(gate, fxColour);

					// Update the real gate button style
					gateButton.setStyle("-fx-background-colour: " + fxColour + "; -fx-border-colour: black; -fx-border-width: 1px; -fx-font-size: 10px;");
					gateButton.setOnMouseEntered(evt -> gateButton.setStyle("-fx-border-width: 1px; -fx-font-size: 10px; -fx-border-colour: white; -fx-background-colour: " + fxColour));
					gateButton.setOnMouseExited(evt -> gateButton.setStyle("-fx-background-colour: " + fxColour + "; -fx-border-colour: black; -fx-font-size: 10px; -fx-border-width: 1px;"));

					colourStage.close();
				});

				colourStage.setScene(new Scene(colourBox, 250, 150));
				colourStage.initOwner(popupStage);
				colourStage.show();
			});

			box.getChildren().add(clone);
		}

		ScrollPane scrollPane = new ScrollPane(box);
		popupStage.setScene(new Scene(scrollPane, 300, 400));
		popupStage.show();
	}

	/**
	 * changing the RBG colours to hexadecimal
	 * @param colour
	 * @return
	 */
	private String toHex(Color colour) {
		return String.format("#%02X%02X%02X",
				(int)(colour.getRed() * 255),
				(int)(colour.getGreen() * 255),
				(int)(colour.getBlue() * 255));
	}

	/**
	 * Clears and resets the entire circuit and interface to default state.
	 */
	public void handleNewCircuit() {
		try {
			if (gridButtons == null) {
				throw new QCSException("Cannot reset: grid not initialized.");
			}

			// Reset gate selection
			selectedGate = null;
			selectedGateColour = null;

			// Clear the grid
			for (int row = 0; row < gridRows; row++) {
				for (int col = 0; col < gridColumns; col++) {
					Button btn = gridButtons[row][col];
					btn.setText("");
					btn.setStyle("-fx-border-colour: black; -fx-border-width: 1px;");
				}
			}

			// Reset graph, messages, and gate button colours
			if (view != null) view.resetBarGraph();
			if (textBox != null) textBox.clear();
			if (codeBox != null) codeBox.clear();
			view.resetGateColoursToDefault();

			currentStepColumn = 0;
			view.updateStepLabel(currentStepColumn); // reset label
			
			// Throws exception if grid is not initialized.
		} catch (QCSException e) {
			textBox.appendText("Error: " + e.getMessage() + "\n");
		}
	}

	/**
	 * Advances simulation by one column step. Highlights column and updates tensor bar.
	 */
	public void stepSimulation() {
		try {
			if (gridButtons == null) throw new QCSException("Grid not initialized.");

			if (currentStepColumn >= gridColumns) return;

			// Remove red border from previous column
			if (currentStepColumn > 0) {
				for (int row = 0; row < gridRows; row++) {
					Button btn = gridButtons[row][currentStepColumn - 1];
					btn.setStyle(btn.getStyle() + "; -fx-border-color: black; -fx-border-width: 1px;");
				}
			}

			// Highlight current column
			for (int row = 0; row < gridRows; row++) {
				Button btn = gridButtons[row][currentStepColumn];
				btn.setStyle(btn.getStyle() + "; -fx-border-color: red; -fx-border-width: 3px;");
			}

			// Build tensor product info
			StringBuilder tensorInfo = new StringBuilder("Tensor Product: ");
			for (int row = 0; row < gridRows; row++) {
				String text = gridButtons[row][currentStepColumn].getText();
				if (text != null && !text.isBlank()) {
					tensorInfo.append(text).append("(q").append(row).append(") | ");
					textBox.appendText(text + "(q" + row + ") | ");
				}
			}
			textBox.appendText("\n");
			view.updateTensorBar(tensorInfo.toString().trim());

			currentStepColumn++;
			view.updateStepLabel(currentStepColumn);

		// Throws if grid is not initialized.
		} catch (QCSException e) {
			textBox.appendText("Error: " + e.getMessage() + "\n");
		}
	}

	/**
	 * Resets simulation to first column and removes visual highlights.
	 */
	public void resetStepSimulation() {
		try {
			if (gridButtons == null) throw new QCSException("Grid not initialized.");

			currentStepColumn = 0;
			view.updateStepLabel(currentStepColumn); // reset label

			// Reset all styles
			for (int row = 0; row < gridRows; row++) {
				for (int col = 0; col < gridColumns; col++) {
					Button btn = gridButtons[row][col];
					String gate = btn.getText();
					String colour = view.getGateColours().get(gate);

					// Restore style based on saved colour or default
					if (colour != null) {
						btn.setStyle("-fx-background-color: " + colour + "; -fx-border-color: black; -fx-border-width: 1px;");
					} else {
						btn.setStyle("-fx-border-color: black; -fx-border-width: 1px;");
					}
				}
			}

			view.updateTensorBar("Tensor Product:");

		} catch (QCSException e) {
			textBox.appendText("Error: " + e.getMessage() + "\n");
		}
	}

	/**
	 * determining if there are remaining multiqubit gates to be placed
	 * @return
	 */
	public boolean isMultiGateInProgress() {
	    return multiGateInProgress != null && remainingMultiPlacements > 0;
	}

	/**
	 * decreasing the amount of gates left to be placed for the multiqubits
	 */
	public void decrementMultiPlacement() {
	    if (remainingMultiPlacements > 0) remainingMultiPlacements--;
	    if (remainingMultiPlacements == 0) {
	        multiGateInProgress = null;
	    }
	}

	/**
	 * getting number of remaining gates to be placed
	 * @return
	 */
	public int getRemainingMultiPlacements() {
	    return remainingMultiPlacements;
	}

	/**
	 * Custom exception inner class for QCS errors.
	 */
	public static class QCSException extends RuntimeException  {
		/**
		 * to display error message
		 * @param message
		 */
	    public QCSException(String message) {
	        super(message);
	    }
	}

}