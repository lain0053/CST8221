package qcs;

import java.io.*;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Bianca & Davod
 * JAP CST8221 
 * Professor: Paulo Sousa
 * Program to create an interface for a quantum circuit simulator
 * 
 * Model class for holding data
 */

public class QCSModel {
	
    private String[][] gateGrid;
    public int[] gridSize;
    public ResourceBundle bundle;

    /**
     * constructor
     */
    public QCSModel() {
        Locale locale = new Locale("en");
        bundle = ResourceBundle.getBundle("qcs.Messages", locale);
        setGridSize(3, 5); // default: 3 rows (qubits), 5 columns (steps)
    }

    /**
     * setting default grid size for application
     * @param rows
     * @param cols
     */
    public void setGridSize(int rows, int cols) {
        this.gridSize = new int[]{cols, rows};
        this.gateGrid = new String[rows][cols];
    }

    /**
     * 
     * @param row
     * @param col
     * @return
     */
    public String getGate(int row, int col) {
        return gateGrid[row][col];
    }

    public void setGate(int row, int col, String gate) {
        if (row >= 0 && row < gateGrid.length && col >= 0 && col < gateGrid[0].length) {
            gateGrid[row][col] = gate;
        } else {
            System.err.println("Attempted to access out-of-bounds grid index: row=" + row + ", col=" + col);
        }
    }

    public void clearGate(int row, int col) {
        gateGrid[row][col] = null;
    }

    public void resetGrid() {
        for (int row = 0; row < gridSize[0]; row++) {
            for (int col = 0; col < gridSize[1]; col++) {
                gateGrid[row][col] = null;
            }
        }
    }

    // Save format: one gate per line in row,col,gate format
    public void saveToFile(File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(file)) {
            for (int row = 0; row < gridSize[0]; row++) {
                for (int col = 0; col < gridSize[1]; col++) {
                    String gate = gateGrid[row][col];
                    if (gate != null && !gate.isBlank()) {
                        writer.println(row + "," + col + "," + gate);
                    }
                }
            }
        }
    }

    public void loadFromFile(File file) throws IOException {
        resetGrid(); // clear current grid
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    int row = Integer.parseInt(parts[0]);
                    int col = Integer.parseInt(parts[1]);
                    String gate = parts[2];
                    setGate(row, col, gate);
                }
            }
        }
    }
}
