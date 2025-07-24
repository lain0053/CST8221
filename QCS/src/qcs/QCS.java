package qcs;

import javafx.application.Application;

/**
 * @author Bianca & Davod
 * JAP CST8221 
 * Professor: Paulo Sousa
 * Program to create an interface for a quantum circuit simulator
 * 
 * Main class for splash screen and launching MVC
 */

public class QCS {
	
    public static void main(String[] args) {
    	
    	QCSSplash.showSplash();         // Show splash before launching JavaFX
		Application.launch(QCSView.class, args);  // Launch the JavaFX app
    }
    
    /**
	 * Inner class for displaying the splash screen.
	 */
	private static class QCSSplash {

		public static void showSplash() {
			javax.swing.JWindow splash = new javax.swing.JWindow();
			javax.swing.JLabel splashLabel = new javax.swing.JLabel();

			// Load splash image
			java.net.URL imageURL = QCS.class.getResource("splash.png");
			if (imageURL != null) {
				javax.swing.ImageIcon icon = new javax.swing.ImageIcon(imageURL);
				splashLabel.setIcon(icon);
			} else {
				splashLabel.setText("Quantum Circuit Simulator");
			}
			
			// Caption for splash screen
			javax.swing.JLabel captionLabel = new javax.swing.JLabel("[ Team: Bianca / David ]", javax.swing.SwingConstants.CENTER);
	        captionLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
	        captionLabel.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14));
	        
	        // Panel to hold image and caption
	        javax.swing.JPanel panel = new javax.swing.JPanel(new java.awt.BorderLayout());
	        panel.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.pink, 2)); // Optional frame
	        panel.add(splashLabel, java.awt.BorderLayout.CENTER);
	        panel.add(captionLabel, java.awt.BorderLayout.SOUTH);

			splash.getContentPane().add(panel);
			splash.setSize(500, 259); // Adjust based on image size
			splash.setLocationRelativeTo(null); // Center on screen
			splash.setVisible(true);

			try {
				Thread.sleep(3000); // Display for 3 seconds
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}

			splash.setVisible(false);
			splash.dispose();
		}
	}
	
}
