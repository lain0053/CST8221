package qcs;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * @author Bianca & Davod
 * JAP CST8221 
 * Professor: Paulo Sousa
 * Program to create an interface for a quantum circuit simulator
 * 
 * View class for building the UI
 */

public class QCSView extends Application {
	
    private QCSModel model;
    QCSController controller;
    private Button[][] gridButtons;
	private final Map<String, Button> gateButtons = new HashMap<>();
	private final Map<String, String> gateColours = new HashMap<>();
	private BarChart<String, Number> barChart;
	private final Map<String, XYChart.Data<String, Number>> barData = new HashMap<>();
	private final Map<String, Label> percentageLabels = new HashMap<>();
	private final Map<String, Integer> gateCounts = new HashMap<>();
	private int totalGateCount = 0;
	private Label tensorBarLabel;
	private Label stepLabel;
    private Stage mainStage;
    private int[] savedGridSize;
    private TextArea messageArea;

    /**
     * start method to launch main application
     */
    @Override
    public void start(Stage primaryStage) {
        this.model = new QCSModel();
        this.mainStage = primaryStage;

        this.savedGridSize = promptForGridSize();
        if (savedGridSize != null) {
            model.setGridSize(savedGridSize[1], savedGridSize[0]); // row, col
        }

        buildUI();
        mainStage.show();
    }

	/**
	 * method that creates the ui for the main window
	 */
	private void buildUI() {
		
        mainStage.setTitle(model.bundle.getString("name"));

        Image img = new Image(getClass().getResourceAsStream("qcs.png"));
        ImageView imgv = new ImageView(img);
        imgv.setFitWidth(300);
        imgv.setPreserveRatio(true);

        tensorBarLabel = new Label("Tensor Product:");
        tensorBarLabel.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: black; "
        		+ "-fx-border-width: 1px; -fx-padding: 0 10 0 10;");
        VBox tensorBarBox = new VBox(tensorBarLabel);
        tensorBarBox.setAlignment(Pos.CENTER_LEFT);
        tensorBarBox.setPadding(new Insets(0, 10, 0, 10));

        VBox messageBox = messageBar();
        messageArea = (TextArea) messageBox.getChildren().get(0);

        controller = new QCSController(messageArea, model.bundle);
        controller.setView(this);

        VBox qubitControls = qubitButtons();
        qubitControls.setMinWidth(120);
        VBox.setVgrow(qubitControls, Priority.NEVER);
        MenuBar menuBar = createMenuBar();

        VBox.setVgrow(messageBox, Priority.ALWAYS);

        BorderPane mainLayout = new BorderPane();
        VBox grid = createGrid(model.gridSize[1], model.gridSize[0]);
        controller.setGridButtons(gridButtons, model.gridSize[1], model.gridSize[0]);
        VBox codeBox = codeBar();
		TextArea codeArea = (TextArea) codeBox.getChildren().get(1);
		controller.setCodeBox(codeArea);
        VBox.setVgrow(grid, Priority.ALWAYS);
        VBox.setVgrow(codeBox, Priority.ALWAYS);
        HBox centerSection = new HBox(qubitControls, grid, codeBox);
        HBox.setHgrow(qubitControls, Priority.NEVER);
        HBox.setHgrow(grid, Priority.ALWAYS);

        HBox controlButtons = new HBox(2);
        controlButtons.setAlignment(Pos.CENTER);
        Button newCircuitBtn = new Button(model.bundle.getString("newCircuit"));
        newCircuitBtn.setOnAction(e -> controller.handleNewCircuit());

        Button stepBtn = new Button(model.bundle.getString("step"));
        stepBtn.setOnAction(e -> controller.stepSimulation());

        Button resetBtn = new Button(model.bundle.getString("reset"));
        resetBtn.setOnAction(e -> controller.resetStepSimulation());

        stepLabel = new Label(model.bundle.getString("steps") + " " + 
        controller.currentStepColumn + "/" + model.gridSize[0]);

        controlButtons.getChildren().addAll(newCircuitBtn, stepBtn, resetBtn, stepLabel);

        VBox topSection = new VBox(menuBar, imgv);
        topSection.setAlignment(Pos.TOP_CENTER);
        VBox centerSectionWrapper = new VBox(2, centerSection, controlButtons);
        VBox.setVgrow(centerSection, Priority.ALWAYS);
        VBox.setVgrow(controlButtons, Priority.NEVER);

        mainLayout.setTop(topSection);
        mainLayout.setCenter(centerSectionWrapper);
        
        VBox leftBottom = new VBox(tensorBarBox, messageBox);
        HBox bottomRight = new HBox(10, leftBottom, createDynamicBarGraph());

        bottomRight.setPadding(new Insets(5));
        bottomRight.setAlignment(Pos.CENTER_LEFT);
        mainLayout.setBottom(bottomRight);


        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        Scene scene = new Scene(scrollPane, 900, 675);
        controller.setScene(scene);

        mainStage.setScene(scene);
        mainStage.setResizable(true);
        mainStage.show();
    }
	
	/**
	 * prompting the user for the grid dimentions
	 * @return
	 */
	private int[] promptForGridSize() {
	    Dialog<int[]> dialog = new Dialog<>();
	    dialog.setTitle("Set Grid Size"); 
	    dialog.setHeaderText("Enter the number of columns and rows for the quantum circuit grid:");

	    ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
	    dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

	    GridPane grid = new GridPane();
	    grid.setHgap(10);
	    grid.setVgap(10);
	    grid.setPadding(new Insets(20, 150, 10, 0));

	    TextField colField = new TextField("5");
	    TextField rowField = new TextField("3");

	    grid.add(new Label("Columns:"), 1, 0);
	    grid.add(colField, 2, 0);
	    grid.add(new Label("Rows:"), 1, 1);
	    grid.add(rowField, 2, 1);

	    dialog.getDialogPane().setContent(grid);

	    dialog.setResultConverter(dialogButton -> {
	        if (dialogButton == okButtonType) {
	            try {
	                int gridColumns = Integer.parseInt(colField.getText().trim());
	                int gridRows = Integer.parseInt(rowField.getText().trim());

	                if (gridColumns <= 0 || gridRows <= 0) {
	                    throw new QCSController.QCSException("Grid size must be positive integers.");
	                }

	                return new int[] { gridColumns, gridRows };

	            } catch (NumberFormatException e) {
	                throw new QCSController.QCSException("Please enter valid integers.");
	            }
	        }
	        // Cancel or close = return null, which caller must handle
	        return null;
	    });

	    try {
	        return dialog.showAndWait().orElse(null);
	    } catch (QCSController.QCSException e) {
	        messageArea.appendText("Error: " + e.getMessage() + "\n");
	        return null;
	    }
	}

	/**
	 * method to create the menu options in the top left
	 * @return
	 */
	private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        Menu file = new Menu(model.bundle.getString("menu.file"));
        Menu settings = new Menu(model.bundle.getString("menu.settings"));
        Menu help = new Menu(model.bundle.getString("menu.help"));

        MenuItem newItem = new MenuItem(model.bundle.getString("menu.new"));
        MenuItem openItem = new MenuItem(model.bundle.getString("menu.open"));
        MenuItem saveItem = new MenuItem(model.bundle.getString("menu.save"));
        MenuItem exitItem = new MenuItem(model.bundle.getString("menu.exit"));

        Menu langItem = new Menu(model.bundle.getString("menu.language"));
        Menu appearItem = new Menu(model.bundle.getString("menu.appearance"));
        MenuItem coloursItem = new MenuItem(model.bundle.getString("menu.colours"));
        MenuItem readItem = new MenuItem(model.bundle.getString("menu.readme"));
        MenuItem aboutItem = new MenuItem(model.bundle.getString("menu.about"));

        MenuItem lang1 = new MenuItem(model.bundle.getString("menu.language.english"));
        MenuItem lang2 = new MenuItem(model.bundle.getString("menu.language.spanish"));
        MenuItem lang3 = new MenuItem(model.bundle.getString("menu.language.portuguese"));

        MenuItem laf1 = new MenuItem(model.bundle.getString("menu.appearance.metal"));
        MenuItem laf2 = new MenuItem(model.bundle.getString("menu.appearance.nimbus"));
        MenuItem laf3 = new MenuItem(model.bundle.getString("menu.appearance.windows"));
        MenuItem laf4 = new MenuItem(model.bundle.getString("menu.appearance.dark"));
        MenuItem laf5 = new MenuItem(model.bundle.getString("menu.appearance.default"));

        lang1.setOnAction(e -> switchLanguage("en"));
        lang2.setOnAction(e -> switchLanguage("es"));
        lang3.setOnAction(e -> switchLanguage("pt"));

        aboutItem.setOnAction(e -> controller.showAbout());
        readItem.setOnAction(e -> controller.showReadme());
        openItem.setOnAction(e -> controller.loadCircuit());
        saveItem.setOnAction(e -> controller.saveCircuit());
        coloursItem.setOnAction(e -> controller.showColourCustomizationWindow());

        laf1.setOnAction(e -> controller.changeAppearance("Metal"));
        laf2.setOnAction(e -> controller.changeAppearance("Nimbus"));
        laf3.setOnAction(e -> controller.changeAppearance("Windows"));
        laf4.setOnAction(e -> controller.changeAppearance("Dark"));
        laf5.setOnAction(e -> controller.changeAppearance(""));

        appearItem.getItems().addAll(laf1, laf2, laf3, laf4, laf5);
        langItem.getItems().addAll(lang1, lang2, lang3);
        file.getItems().addAll(newItem, openItem, saveItem, exitItem);
        settings.getItems().addAll(langItem, appearItem, coloursItem);
        help.getItems().addAll(readItem, aboutItem);
        menuBar.getMenus().addAll(file, settings, help);

        return menuBar;
    }
	
	/**
	 * function for switching the application language
	 * @param locale
	 */
	private void switchLanguage(String langCode) {
        model.bundle = ResourceBundle.getBundle("qcs.Messages", new java.util.Locale(langCode));
        buildUI(); // rebuild UI with new language
    }

	/**
	 * method for creating the message Bar at the bottom
	 * @return
	 */
	private VBox messageBar() {
        TextArea msgArea = new TextArea();
        msgArea.setPrefRowCount(4);
        msgArea.setWrapText(true);
        msgArea.setMaxWidth(Double.MAX_VALUE);
        msgArea.setMinHeight(75);
        msgArea.setPrefHeight(100);
        VBox.setVgrow(msgArea, Priority.ALWAYS);
        VBox messageBox = new VBox(5, msgArea);
        messageBox.setPadding(new Insets(0, 10, 0, 10));
        return messageBox;
    }
	
	/**
	 * method for creating dynamic graphic
	 * @return stackpane
	 */
	public StackPane createDynamicBarGraph() {
	    CategoryAxis xAxis = new CategoryAxis();
	    NumberAxis yAxis = new NumberAxis(0, 100, 10);

	    xAxis.setTickLabelsVisible(false);
	    xAxis.setTickMarkVisible(false);
	    yAxis.setTickLabelsVisible(false);
	    yAxis.setTickMarkVisible(false);
	    xAxis.setOpacity(0);
	    yAxis.setOpacity(0);

	    barChart = new BarChart<>(xAxis, yAxis);
	    barChart.setLegendVisible(false);
	    barChart.setAnimated(false);
	    barChart.setHorizontalGridLinesVisible(false);
	    barChart.setVerticalGridLinesVisible(false);
	    barChart.setPrefSize(400, 75);
	    barChart.setCategoryGap(10);
	    barChart.setBarGap(2);
	    barChart.setVerticalZeroLineVisible(false);

	    XYChart.Series<String, Number> series = new XYChart.Series<>();

	    // Only show the 8 single-qubit gates
	    String[] gates = {"I", "X", "Y", "Z", "H", "S", "T", "U"};
	    for (String gate : gates) {
	        XYChart.Data<String, Number> data = new XYChart.Data<>(gate, 0);
	        barData.put(gate, data);
	        series.getData().add(data);
	    }

	    barChart.getData().add(series);
	    StackPane chartPane = new StackPane(barChart);
	    
	    chartPane.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
	        for (String gate : gates) {
	            XYChart.Data<String, Number> data = barData.get(gate);
	            Node barNode = data.getNode();
	            Label label = percentageLabels.get(gate);
	            if (barNode != null && label != null) {
	                Bounds bounds = barNode.localToParent(barNode.getBoundsInLocal());
	                double x = bounds.getMinX() + bounds.getWidth() / 2 - label.prefWidth(-1) / 2;
	                double y = bounds.getMinY() - 12;
	                label.setLayoutX(x);
	                label.setLayoutY(y);
	            }
	        }
	    });

	    // Overlay % labels
	    chartPane.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: black;");
	    Pane labelLayer = new Pane();
	    labelLayer.setMouseTransparent(true); // so labels don't interfere with mouse clicks
	    chartPane.getChildren().add(labelLayer);

	    Platform.runLater(() -> {
	        for (String gate : gates) {
	            XYChart.Data<String, Number> data = barData.get(gate);
	            Label label = new Label("0%");
	            label.setStyle("-fx-font-size: 10px;");
	            percentageLabels.put(gate, label);
	            labelLayer.getChildren().add(label);

	            // Bind label to data node
	            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
	                if (newNode != null) {
	                    newNode.boundsInParentProperty().addListener((o, oldBounds, newBounds) -> {
	                        double x = newBounds.getMinX() + newBounds.getWidth() / 2 - 10;
	                        double y = newBounds.getMinY() - 15;
	                        label.setLayoutX(x);
	                        label.setLayoutY(y);
	                    });
	                }
	            });
	        }
	    });

	    return chartPane;
	}
	
	/**
	 * to edit percentage on the graphs
	 * @param gateName
	 */
	public void incrementGateCount(String gateName) {
	    if (!barData.containsKey(gateName)) return;

	    // Update raw count
	    gateCounts.put(gateName, gateCounts.getOrDefault(gateName, 0) + 1);

	    // Recalculate total
	    totalGateCount = gateCounts.values().stream().mapToInt(Integer::intValue).sum();

	    // Update bars and labels
	    for (String gate : barData.keySet()) {
	        int count = gateCounts.getOrDefault(gate, 0);
	        double percent = (totalGateCount == 0) ? 0 : (count * 100.0 / totalGateCount);

	        XYChart.Data<String, Number> data = barData.get(gate);
	        data.setYValue(percent);

	        Label label = percentageLabels.get(gate);
	        if (label != null) {
	            label.setText(String.format("%.0f%%", percent));
	        }
	    }
	}
	
	/**
	 * method to reset bar graph after new circuit is pressed
	 */
	public void resetBarGraph() {
		
	    totalGateCount = 0;
	    gateCounts.clear();
	    
	    for (String gate : barData.keySet()) {
	        barData.get(gate).setYValue(0);
	        Label label = percentageLabels.get(gate);
	        if (label != null) label.setText("0%");
	    }
	}
	
	/**
	 * method to create new text area at the right side
	 * @return
	 */
	VBox codeBar() {

		Label codeLabel = new Label(model.bundle.getString("code"));
		TextArea codeArea = new TextArea();
		codeArea.setPrefRowCount(2);
		codeArea.setWrapText(true);
		codeArea.setMaxWidth(Double.MAX_VALUE);
		codeArea.setMaxHeight(Double.MAX_VALUE);
		codeArea.setPrefWidth(125);
		VBox.setVgrow(codeArea, Priority.ALWAYS);
		VBox codeBox = new VBox(5, codeLabel, codeArea);
		codeBox.setPadding(new Insets(10, 10, 10, 10));

		return codeBox;
	}

	/**
	 * method for creating the main grid
	 * @param action
	 * @return
	 */
	private VBox createGrid(int rows, int cols) {

		VBox grid = new VBox(10);
		grid.setPadding(new Insets(10));
		grid.setFillWidth(true);

		Label circuitLabel = new Label(model.bundle.getString("label"));

		GridPane circuitGrid = new GridPane();
		circuitGrid.setStyle("-fx-border-color: black; -fx-border-width: 2;");
		VBox.setVgrow(circuitGrid, Priority.ALWAYS);

		for (int i = 0; i < model.gridSize[1]; i++) {
			RowConstraints rc = new RowConstraints();
			rc.setPercentHeight(100.0 / model.gridSize[1]);
			rc.setVgrow(Priority.ALWAYS);
			circuitGrid.getRowConstraints().add(rc);
		}

		for (int i = 0; i < model.gridSize[0]; i++) {
			ColumnConstraints cc = new ColumnConstraints();
			cc.setPercentWidth(100.0 / model.gridSize[0]);
			cc.setHgrow(Priority.ALWAYS);
			circuitGrid.getColumnConstraints().add(cc);
		}

		// Initialize gridButtons array
		gridButtons = new Button[rows][cols];
		
		for (int row = 0; row < model.gridSize[1]; row++) {
		    for (int col = 0; col < model.gridSize[0]; col++) {
		        Button cellButton = new Button();
		        cellButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		        cellButton.setStyle(
		            "-fx-border-color: black;" +
		            "-fx-border-width: 1;" +
		            "-fx-padding: 0;"
		        );
		        GridPane.setHgrow(cellButton, Priority.NEVER);
		        GridPane.setVgrow(cellButton, Priority.NEVER);

		        int r = row;
		        int c = col;
		        cellButton.setOnAction(e -> {
		            String gate = controller.getSelectedGate();
		            String color = controller.getSelectedGateColour();

		            if (gate != null && color != null) {
		            	if (gate.equals("BARRIER")) {
		                    // Place barrier in entire column
		                    for (int i = 0; i < model.gridSize[1]; i++) {
		                        Button b = gridButtons[i][c];
		                        b.setText("BARRIER");
		                        b.setStyle("-fx-background-color: " + color + "; -fx-border-color: black; -fx-border-width: 1px;");
		                        model.setGate(i, c, "BARRIER");
		                        incrementGateCount("BARRIER");
		                    }
		                } else {
		                    cellButton.setText(gate);
		                    cellButton.setStyle("-fx-background-color: " + color + "; -fx-border-color: black; -fx-border-width: 1px;");
		                    model.setGate(r, c, gate);
		                    incrementGateCount(gate);
		                }

		                controller.updateCodeBoxFromGrid();
		                controller.setGridButtons(gridButtons, rows, cols);

		                if (controller.isMultiGateInProgress()) {
		                    controller.decrementMultiPlacement();
		                    if (controller.getRemainingMultiPlacements() > 0) {
		                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
		                        alert.setTitle("Multi-Qubit Gate Placement");
		                        alert.setHeaderText(null);
		                        alert.setContentText("Place the next part of the " + gate + " gate in the same column.");
		                        alert.showAndWait();
		                    } else {
		                        controller.clearSelectedGate();
		                    }
		                } else {
		                    controller.clearSelectedGate();
		                }
		            } else {
		                controller.textBox.appendText(model.bundle.getString("noGate") + "\n");
		            }
		        });


		        circuitGrid.add(cellButton, col, row);
		        gridButtons[row][col] = cellButton;  // store button ref here
		    }
		}

		VBox gridContainer = new VBox(circuitGrid);
		VBox.setVgrow(gridContainer, Priority.ALWAYS);
		grid.getChildren().addAll(circuitLabel, gridContainer);
		VBox.setVgrow(grid, Priority.ALWAYS);

		return grid;
	}
	
	/**
	 * method to create the single and multi qubit buttons
	 * @return
	 */
	private VBox qubitButtons() {

		Label gatesLabel = new Label(model.bundle.getString("quantumGates"));
		Label singlequbit = new Label(model.bundle.getString("singleQubits"));

		GridPane qubitButtons = new GridPane();
		qubitButtons.setHgap(2);
		qubitButtons.setVgap(2);

		String[] qubitColours = {
			"#ffadad", "#ffd6a5", "#fdffb6", "#caffbf",
			"#9bf6ff", "#a0c4ff", "#bdb2ff", "#ffc6ff"
		};
		String[] qubitNames = {"I", "X", "Y", "Z", "H", "S", "T", "U"};
		
		for (int i = 0; i < qubitColours.length; i++) {
		    gateColours.put(qubitNames[i], qubitColours[i]);
		}

		for (int i = 0; i < 8; i++) {
		    String name = qubitNames[i];
		    String colour = gateColours.get(name);
		    Button qubit = new Button(name);
		    gateButtons.put(name, qubit);

		    qubit.setMaxWidth(Double.MAX_VALUE);
		    qubit.setPrefHeight(Region.USE_COMPUTED_SIZE);
		    qubit.setStyle("-fx-background-color: " + colour + "; -fx-border-color: black; -fx-border-width: 1px; -fx-font-size: 10px;");

		    qubit.setOnMouseEntered(e -> qubit.setStyle("-fx-border-width: 1px; -fx-font-size: 10px; -fx-border-color: white; -fx-background-color: " + gateColours.get(name)));
		    qubit.setOnMouseExited(e -> qubit.setStyle("-fx-background-color: " + gateColours.get(name) + "; -fx-border-color: black; -fx-font-size: 10px; -fx-border-width: 1px;"));

		    qubitButtons.add(qubit, i % 2, i / 2);
		    GridPane.setHgrow(qubit, Priority.ALWAYS);
		    GridPane.setVgrow(qubit, Priority.ALWAYS);

		    qubit.setOnAction(e -> controller.setSelectedGate(name, gateColours.get(name)));
		}


		ColumnConstraints col = new ColumnConstraints();
		col.setHgrow(Priority.ALWAYS);
		qubitButtons.getColumnConstraints().addAll(col, col);

		Label multiqubit = new Label(model.bundle.getString("multiQubits"));
		multiqubit.setAlignment(Pos.CENTER);

		VBox multiQubitBox = new VBox(2);
		multiQubitBox.setAlignment(Pos.TOP_CENTER);
		VBox.setVgrow(multiQubitBox, Priority.ALWAYS);

		String[] multiColours = { "#d0f4de", "#fef9c7", "#fcd5ce", "#cdb4db" };
		String[] multiNames = {"CX", "SWAP", "CU", "CCX"};
		
		for (int i = 0; i < multiColours.length; i++) {
		    gateColours.put(multiNames[i], multiColours[i]);
		}

		for (int i = 0; i < 4; i++) {
			Button multiQubit = new Button(multiNames[i]);
			gateButtons.put(multiNames[i], multiQubit);
			multiQubit.setMaxWidth(Double.MAX_VALUE);
			multiQubit.setPrefHeight(Region.USE_COMPUTED_SIZE);
			VBox.setVgrow(multiQubit, Priority.ALWAYS);
			multiQubitBox.getChildren().add(multiQubit);
			String colour = multiColours[i];
			multiQubit.setStyle("-fx-background-color: " + colour + "; -fx-border-color: black; -fx-border-width: 1px; -fx-font-size: 10px;");
			multiQubit.setOnMouseEntered(e -> multiQubit.setStyle("-fx-background-color: " + colour + "; -fx-border-color: white; -fx-border-width: 1px; -fx-font-size: 10px;"));
			multiQubit.setOnMouseExited(e -> multiQubit.setStyle("-fx-background-color: " + colour + "; -fx-border-color: black; -fx-border-width: 1px; -fx-font-size: 10px;"));
			String multiName = multiNames[i];
			multiQubit.setOnAction(e -> controller.setSelectedGate(multiName, gateColours.get(multiName)));
		}

		Label operations = new Label(model.bundle.getString("operations"));
		Button barrier = new Button("BARRIER");
		gateButtons.put("BARRIER", barrier);
		gateColours.put("BARRIER", "#cacccb");
		
		barrier.setMaxWidth(Double.MAX_VALUE);
		barrier.setPrefHeight(Region.USE_COMPUTED_SIZE);
		barrier.setStyle("-fx-background-color: lightgray; -fx-border-color: black; -fx-border-width: 1px; -fx-font-size: 10px;");
		barrier.setOnMouseEntered(e -> barrier.setStyle("-fx-background-color: lightgray; -fx-border-color: white; -fx-border-width: 1px; -fx-font-size: 10px;"));
		barrier.setOnMouseExited(e -> barrier.setStyle("-fx-background-color: lightgray; -fx-border-color: black; -fx-border-width: 1px; -fx-font-size: 10px;"));
		String barrName = "BARRIER";
		barrier.setOnAction(e -> controller.setSelectedGate(barrName, gateColours.get(barrName)));

		Label phaseLabel = new Label(model.bundle.getString("phaseParam"));

		GridPane phaseGrid = new GridPane();
		phaseGrid.setHgap(3);
		phaseGrid.setVgap(3);

		Label aLabel = new Label("a:");
		TextField aField = new TextField("0.0");

		Label bLabel = new Label("b:");
		TextField bField = new TextField("0.0");

		Label cLabel = new Label("c:");
		TextField cField = new TextField("0.0");

		phaseGrid.add(aLabel, 0, 0);
		phaseGrid.add(aField, 1, 0);
		phaseGrid.add(bLabel, 0, 1);
		phaseGrid.add(bField, 1, 1);
		phaseGrid.add(cLabel, 0, 2);
		phaseGrid.add(cField, 1, 2);

		VBox qubitControls = new VBox(2);
		qubitControls.setPadding(new Insets(2));
		qubitControls.setFillWidth(true);
		VBox.setVgrow(qubitButtons, Priority.NEVER);
		VBox.setVgrow(multiQubitBox, Priority.ALWAYS);
		VBox.setVgrow(phaseGrid, Priority.ALWAYS);

		qubitControls.getChildren().addAll(
			gatesLabel, new Separator(),
			singlequbit, qubitButtons,
			multiqubit, multiQubitBox,
			operations, barrier, phaseLabel, phaseGrid
		);
		qubitControls.setPadding(new Insets(0, 10, 0, 10));

		return qubitControls;
	}
	
	/**
	 * accessing the qubit buttons
	 * @return
	 */
	public Map<String, Button> getGateButtons() {
	    return gateButtons;
	}
	
	/**
	 * accessing the qubit colours
	 * @return
	 */
	public Map<String, String> getGateColours() {
	    return gateColours;
	}
	
	/**
	 * resetting qubtit colours after new circuit
	 */
	public void resetGateColoursToDefault() {
	    // Default colors
	    String[] qubitNames = {"I", "X", "Y", "Z", "H", "S", "T", "U"};
	    String[] qubitColours = {
	        "#ffadad", "#ffd6a5", "#fdffb6", "#caffbf",
	        "#9bf6ff", "#a0c4ff", "#bdb2ff", "#ffc6ff"
	    };

	    for (int i = 0; i < qubitNames.length; i++) {
	        gateColours.put(qubitNames[i], qubitColours[i]);
	        Button btn = gateButtons.get(qubitNames[i]);
	        if (btn != null) {
	            String color = qubitColours[i];
	            btn.setStyle("-fx-background-color: " + color + "; -fx-border-color: black; -fx-font-size: 10px;");
	        }
	    }

	    String[] multiNames = {"CX", "SWAP", "CU", "CCX"};
	    String[] multiColours = { "#d0f4de", "#fef9c7", "#fcd5ce", "#cdb4db" };

	    for (int i = 0; i < multiNames.length; i++) {
	        gateColours.put(multiNames[i], multiColours[i]);
	        Button btn = gateButtons.get(multiNames[i]);
	        if (btn != null) {
	            String color = multiColours[i];
	            btn.setStyle("-fx-background-color: " + color + "; -fx-border-color: black; -fx-font-size: 10px;");
	        }
	    }

	    gateColours.put("BARRIER", "#cacccb");
	    Button barrier = gateButtons.get("BARRIER");
	    if (barrier != null) {
	        barrier.setStyle("-fx-background-color: lightgray; -fx-border-color: black; -fx-font-size: 10px;");
	    }
	}

	/**
	 * appending to teh tensor bar
	 * @param text
	 */
	public void updateTensorBar(String text) {
	    if (tensorBarLabel != null) {
	        tensorBarLabel.setText(text);
	    }
	}
	
	/**
	 * updating the number of steps as you go through the columns
	 * @param currentStep
	 */
	public void updateStepLabel(int currentStep) {
	    stepLabel.setText(model.bundle.getString("steps") + " " + currentStep + "/" + model.gridSize[0]);
	}
}