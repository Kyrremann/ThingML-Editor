/*
 * 03/21/2010
 *
 * Copyright (C) 2010 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com/rsyntaxtextarea
 *
 * This library is distributed under a modified BSD license.  See the included
 * RSTALanguageSupport.License.txt file for details.
 */
package org.fife.rsta.ac.demo;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.*;
import javax.swing.border.BevelBorder;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

/**
 * Stand-alone version of the demo.
 * 
 * @author Robert Futrell
 * @version 1.0
 */
public class DemoApp extends JFrame {

	private static final long serialVersionUID = -3237525402614050486L;

	public DemoApp() {

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("RSTA Language Support ThingML Demo Application");		
		setRootPane(new DemoRootPane());

//		GraphicsEnvironment env = GraphicsEnvironment
//				.getLocalGraphicsEnvironment();
//		GraphicsDevice dev = env.getDefaultScreenDevice();
//		setResizable(true);
//		setUndecorated(true);
//		dev.setFullScreenWindow(this);

		pack();
	}

	/**
	 * Called when we are made visible. Here we request that the
	 * {@link RSyntaxTextArea} is given focus.
	 * 
	 * @param visible
	 *            Whether this frame should be visible.
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			((DemoRootPane) getRootPane()).focusTextArea();
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager
							.getSystemLookAndFeelClassName());
					// UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
				} catch (Exception e) {
					e.printStackTrace(); // Never happens
				}
				Toolkit.getDefaultToolkit().setDynamicLayout(true);
				new DemoApp().setVisible(true);
			}
		});
	}

}