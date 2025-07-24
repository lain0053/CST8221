package qcs;

import java.util.Locale;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
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
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Class for defining initial layout of the application
 * 
 * @author Bianca
 * @author David
 */
public class LayoutClass extends Application {

	int gridColumns = 5;
	int gridRows = 3;
	private Stage mainStage;
	private int[] savedGridSize;
	private ResourceBundle bundle;

	/**
	 * main function to launch program
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	/**
	 * function to start the program
	 * @param primaryStage
	 */
	public void start(Stage primaryStage) {
		Locale locale = new Locale("en");
		bundle = ResourceBundle.getBundle("qcs.Messages", locale);

		mainStage = primaryStage;
		savedGridSize = promptForGridSize();
		if (savedGridSize == null) return;

		gridColumns = savedGridSize[0];
		gridRows = savedGridSize[1];

		buildUI();
		mainStage.show();
	}

	/**
	 * method that creates the main window
	 */
	public void buildUI() {
		mainStage.setTitle(bundle.getString("name"));

		/*Header Image*/
		Image img = new Image(getClass().getResourceAsStream("qcs.png"));
		ImageView imgv = new ImageView(img);
		imgv.setFitWidth(300);
		imgv.setPreserveRatio(true);


		VBox messageBox = messageBar();
		TextArea messageArea = (TextArea) messageBox.getChildren().get(1);
		ActionsClass actions = new ActionsClass(messageArea);

		/*calling the functions to create each section*/
		VBox qubitControls = qubitButtons(actions);
		qubitControls.setMinWidth(120);
		VBox.setVgrow(qubitControls, Priority.NEVER);
		MenuBar menuBar = createMenuBar(actions);

		VBox.setVgrow(messageBox, Priority.ALWAYS);

		/*Main layout*/
		BorderPane mainLayout = new BorderPane();  
		VBox grid = createGrid(actions);
		VBox.setVgrow(grid, Priority.ALWAYS);
		HBox centerSection = new HBox(qubitControls, grid);
		HBox.setHgrow(qubitControls, Priority.NEVER);
		HBox.setHgrow(grid, Priority.ALWAYS);

		/*Control Buttons*/
		HBox controlButtons = new HBox(2);
		controlButtons.setAlignment(Pos.CENTER);
		Button newCircuitBtn = new Button(bundle.getString("newCircuit"));
		Button stepBtn = new Button(bundle.getString("step"));
		Button resetBtn = new Button(bundle.getString("reset"));
		Label stepLabel = new Label(bundle.getString("steps") + 5);

		controlButtons.getChildren().addAll(newCircuitBtn, stepBtn, resetBtn, stepLabel);

		VBox topSection = new VBox(menuBar, imgv);
		topSection.setAlignment(Pos.TOP_CENTER);
		VBox centerSectionWrapper = new VBox(2, centerSection, controlButtons);
		VBox.setVgrow(centerSection, Priority.ALWAYS);
		VBox.setVgrow(controlButtons, Priority.NEVER);

		mainLayout.setTop(topSection);
		mainLayout.setCenter(centerSectionWrapper);
		mainLayout.setBottom(messageBox);

		ScrollPane scrollPane = new ScrollPane(mainLayout);
		scrollPane.setFitToWidth(true);
		scrollPane.setFitToHeight(true);

		/* main application frame */
		Scene scene = new Scene(scrollPane, 900, 67);
		actions.scene = scene;
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

		grid.add(new Label("Columns:"), 0, 0);
		grid.add(colField, 1, 0);
		grid.add(new Label("Rows:"), 0, 1);
		grid.add(rowField, 1, 1);

		dialog.getDialogPane().setContent(grid);

		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == okButtonType) {
				try {
					int cols = Integer.parseInt(colField.getText());
					int rows = Integer.parseInt(rowField.getText());
					if (cols <= 0 || rows <= 0) throw new NumberFormatException();
					return new int[]{cols, rows};
				} catch (NumberFormatException e) {
					return null;
				}
			}
			return null;
		});

		return dialog.showAndWait().orElse(null);
	}

	/**
	 * method to create the menu options in the top left
	 * @return
	 */
	MenuBar createMenuBar(ActionsClass action) {

		MenuBar menuBar = new MenuBar();
		Menu file = new Menu(bundle.getString("menu.file"));
		Menu settings = new Menu(bundle.getString("menu.settings"));
		Menu help = new Menu(bundle.getString("menu.help"));

		MenuItem newItem = new MenuItem(bundle.getString("menu.new"));
		MenuItem openItem = new MenuItem(bundle.getString("menu.open"));
		MenuItem saveItem = new MenuItem(bundle.getString("menu.save"));
		MenuItem exitItem = new MenuItem(bundle.getString("menu.exit"));
		Menu langItem = new Menu(bundle.getString("menu.language"));
		Menu appearItem = new Menu(bundle.getString("menu.appearance"));
		MenuItem colsItem = new MenuItem(bundle.getString("menu.colours"));
		MenuItem readItem = new MenuItem(bundle.getString("menu.readme"));
		MenuItem aboutItem = new MenuItem(bundle.getString("menu.about"));
		MenuItem lang1 = new MenuItem(bundle.getString("menu.language.english"));
		MenuItem lang2 = new MenuItem(bundle.getString("menu.language.spanish"));
		MenuItem lang3 = new MenuItem(bundle.getString("menu.language.portuguese"));
		MenuItem laf1 = new MenuItem(bundle.getString("menu.appearance.metal"));
		MenuItem laf2 = new MenuItem(bundle.getString("menu.appearance.nimbus"));
		MenuItem laf3 = new MenuItem(bundle.getString("menu.appearance.windows"));
		MenuItem laf4 = new MenuItem(bundle.getString("menu.appearance.dark"));
		MenuItem laf5 = new MenuItem(bundle.getString("menu.appearance.default"));

		lang1.setOnAction(e -> switchLanguage(new Locale("en")));
		lang2.setOnAction(e -> switchLanguage(new Locale("es")));
		lang3.setOnAction(e -> switchLanguage(new Locale("pt")));

		aboutItem.setOnAction(e -> action.showAbout());
		laf1.setOnAction(e -> action.changeAppearance("Metal"));
		laf2.setOnAction(e -> action.changeAppearance("Nimbus"));
		laf3.setOnAction(e -> action.changeAppearance("Windows"));
		laf4.setOnAction(e -> action.changeAppearance("Dark"));
		laf5.setOnAction(e -> action.changeAppearance(""));

		appearItem.getItems().addAll(laf1, laf2, laf3, laf4, laf5);
		langItem.getItems().addAll(lang1, lang2, lang3);
		file.getItems().addAll(newItem, openItem, saveItem, exitItem);
		settings.getItems().addAll(langItem, appearItem, colsItem);
		help.getItems().addAll(readItem, aboutItem);
		menuBar.getMenus().addAll(file, settings, help);

		return menuBar;
	}

	/**
	 * function for switching the application language
	 */
	private void switchLanguage(Locale locale) {
		bundle = ResourceBundle.getBundle("qcs.Messages", locale);
		buildUI();  // rebuild UI with current grid size and new language
		mainStage.show();
	}

	/**
	 * method for creating the message Bar at the bottom
	 * @return
	 */
	VBox messageBar() {

		Label msgLabel = new Label(bundle.getString("messages"));
		TextArea msgArea = new TextArea();
		msgArea.setPrefRowCount(4);
		msgArea.setWrapText(true);
		msgArea.setMaxWidth(Double.MAX_VALUE);
		msgArea.setMaxHeight(Double.MAX_VALUE);
		msgArea.setMinHeight(75);
		msgArea.setPrefHeight(100);
		VBox.setVgrow(msgArea, Priority.ALWAYS);
		VBox messageBox = new VBox(5, msgLabel, msgArea);
		messageBox.setPadding(new Insets(0, 10, 15, 10));

		return messageBox;
	}

	/**
	 * method for creating the main grid
	 * @param action
	 * @return
	 */
	private VBox createGrid(ActionsClass action) {

		VBox grid = new VBox(10);
		grid.setPadding(new Insets(10));
		grid.setFillWidth(true);

		Label circuitLabel = new Label(bundle.getString("label"));

		GridPane circuitGrid = new GridPane();
		circuitGrid.setStyle("-fx-border-color: black; -fx-border-width: 2;");
		VBox.setVgrow(circuitGrid, Priority.ALWAYS);

		for (int i = 0; i < gridRows; i++) {
			RowConstraints rc = new RowConstraints();
			rc.setPercentHeight(100.0 / gridRows);
			rc.setVgrow(Priority.ALWAYS);
			circuitGrid.getRowConstraints().add(rc);
		}

		for (int i = 0; i < gridColumns; i++) {
			ColumnConstraints cc = new ColumnConstraints();
			cc.setPercentWidth(100.0 / gridColumns);
			cc.setHgrow(Priority.ALWAYS);
			circuitGrid.getColumnConstraints().add(cc);
		}

		for (int row = 0; row < gridRows; row++) {
			for (int col = 0; col < gridColumns; col++) {
				Button cellButton = new Button();
				cellButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
				cellButton.setStyle(
						"-fx-border-color: black;" +
								"-fx-border-width: 1;" +
								"-fx-padding: 0;"
						);
				GridPane.setHgrow(cellButton, Priority.NEVER);
				GridPane.setVgrow(cellButton, Priority.NEVER);

				cellButton.setOnAction(e -> {
					String gate = action.getSelectedGate();
					String color = action.getSelectedGateColor();

					if (gate != null && color != null) {
						cellButton.setText(gate);
						cellButton.setStyle("-fx-background-color: " + color + "; -fx-border-color: black; -fx-border-width: 1px;");
						action.clearSelectedGate();
					} else {
						action.textBox.appendText(bundle.getString("noGate") + "\n");
					}
				});
				circuitGrid.add(cellButton, col, row);
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
	VBox qubitButtons(ActionsClass action) {

		Label gatesLabel = new Label(bundle.getString("quantumGates"));
		Label singlequbit = new Label(bundle.getString("singleQubits"));

		GridPane qubitButtons = new GridPane();
		qubitButtons.setHgap(2);
		qubitButtons.setVgap(2);

		String[] qubitColours = {
				"#ffadad", "#ffd6a5", "#fdffb6", "#caffbf",
				"#9bf6ff", "#a0c4ff", "#bdb2ff", "#ffc6ff"
		};
		String[] qubitNames = {"I", "X", "Y", "Z", "H", "S", "T", "U"};

		for (int i = 0; i < 8; i++) {
			Button qubit = new Button(qubitNames[i]);
			qubit.setMaxWidth(Double.MAX_VALUE);
			qubit.setPrefHeight(Region.USE_COMPUTED_SIZE);
			qubit.setStyle("-fx-background-color: " + qubitColours[i] + "; -fx-border-color: black; -fx-border-width: 1px; -fx-font-size: 10px;");
			String colour = qubitColours[i];

			qubit.setOnMouseEntered(e -> qubit.setStyle("-fx-border-width: 1px; -fx-font-size: 10px; -fx-border-color: white; -fx-background-color: " + colour));
			qubit.setOnMouseExited(e -> qubit.setStyle("-fx-background-color: " + colour + "; -fx-border-color: black; -fx-font-size: 10px; -fx-border-width: 1px;"));

			qubitButtons.add(qubit, i % 2, i / 2);
			GridPane.setHgrow(qubit, Priority.ALWAYS);
			GridPane.setVgrow(qubit, Priority.ALWAYS);

			String singleName = qubitNames[i];
			qubit.setOnAction(e -> action.setSelectedGate(singleName, colour));        
		}

		ColumnConstraints col = new ColumnConstraints();
		col.setHgrow(Priority.ALWAYS);
		qubitButtons.getColumnConstraints().addAll(col, col);

		Label multiqubit = new Label(bundle.getString("multiQubits"));
		multiqubit.setAlignment(Pos.CENTER);

		VBox multiQubitBox = new VBox(2);
		multiQubitBox.setAlignment(Pos.TOP_CENTER);
		VBox.setVgrow(multiQubitBox, Priority.ALWAYS);

		String[] multiColours = { "#d0f4de", "#fef9c7", "#fcd5ce", "#cdb4db" };
		String[] multiNames = {"CX", "SWAP", "CU", "CCX"};

		for (int i = 0; i < 4; i++) {
			Button multiQubit = new Button(multiNames[i]);
			multiQubit.setMaxWidth(Double.MAX_VALUE);
			multiQubit.setPrefHeight(Region.USE_COMPUTED_SIZE);
			VBox.setVgrow(multiQubit, Priority.ALWAYS);
			multiQubitBox.getChildren().add(multiQubit);
			String colour = multiColours[i];
			multiQubit.setStyle("-fx-background-color: " + colour + "; -fx-border-color: black; -fx-border-width: 1px; -fx-font-size: 10px;");
			multiQubit.setOnMouseEntered(e -> multiQubit.setStyle("-fx-background-color: " + colour + "; -fx-border-color: white; -fx-border-width: 1px; -fx-font-size: 10px;"));
			multiQubit.setOnMouseExited(e -> multiQubit.setStyle("-fx-background-color: " + colour + "; -fx-border-color: black; -fx-border-width: 1px; -fx-font-size: 10px;"));
			String multiName = multiNames[i];
			multiQubit.setOnAction(e -> action.setSelectedGate(multiName, colour));
		}

		/* Barrier actions */
		Label operations = new Label(bundle.getString("operations"));
		Button barrier = new Button("BARRIER");
		barrier.setMaxWidth(Double.MAX_VALUE);
		barrier.setPrefHeight(Region.USE_COMPUTED_SIZE);
		barrier.setStyle("-fx-background-color: lightgray; -fx-border-color: black; -fx-border-width: 1px; -fx-font-size: 10px;");
		barrier.setOnMouseEntered(e -> barrier.setStyle("-fx-background-color: lightgray; -fx-border-color: white; -fx-border-width: 1px; -fx-font-size: 10px;"));
		barrier.setOnMouseExited(e -> barrier.setStyle("-fx-background-color: lightgray; -fx-border-color: black; -fx-border-width: 1px; -fx-font-size: 10px;"));
		String barrName = "BARRIER";
		String barrColour = "#cacccb";
		barrier.setOnAction(e -> action.setSelectedGate(barrName, barrColour));

		/* Creating phase parameters section */
		Label phaseLabel = new Label(bundle.getString("phaseParam"));

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
}
