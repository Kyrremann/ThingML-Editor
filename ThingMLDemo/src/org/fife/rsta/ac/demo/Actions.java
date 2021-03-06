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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

/**
 * Container for all actions used by the demo.
 * 
 * @author Robert Futrell
 * @version 1.0
 */
interface Actions {

	/**
	 * Displays an "About" dialog.
	 */
	static class AboutAction extends AbstractAction {

		private static final long serialVersionUID = 1L;
		private DemoRootPane demo;

		public AboutAction(DemoRootPane demo) {
			this.demo = demo;
			putValue(NAME, "About RSTALanguageSupport...");
		}

		public void actionPerformed(ActionEvent e) {
			AboutDialog ad = new AboutDialog(
					(DemoApp) SwingUtilities.getWindowAncestor(demo));
			ad.setLocationRelativeTo(demo);
			ad.setVisible(true);
		}

	}

	/**
	 * Exits the application.
	 */
	static class ExitAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public ExitAction() {
			putValue(NAME, "Exit");
			putValue(MNEMONIC_KEY, new Integer('x'));
		}

		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}

	}

	/**
	 * Lets the user open a file.
	 */
	static class OpenAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		private DemoRootPane demo;
		private JFileChooser chooser;

		public OpenAction(DemoRootPane demo, ImageIcon icon) {
			super(null, icon);
			this.demo = demo;
			if (icon == null)
				putValue(NAME, "Open...");
			putValue(MNEMONIC_KEY, new Integer('O'));
			int mods = demo.getToolkit().getMenuShortcutKeyMask();
			KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_O, mods);
			putValue(ACCELERATOR_KEY, ks);
		}

		public void actionPerformed(ActionEvent e) {
			if (chooser == null) {
				chooser = new JFileChooser();
				chooser.setFileFilter(new ExtensionFileFilter(
						"ThingML Source Files", "thingml"));
			}
			int rc = chooser.showOpenDialog(demo);
			if (rc == JFileChooser.APPROVE_OPTION) {
				demo.openFile(chooser.getSelectedFile());
			}
		}

	}

	/**
	 * Lets the user open a file.
	 */
	static class NewFileAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		private DemoRootPane demo;
		private JFileChooser chooser;

		public NewFileAction(DemoRootPane demo, ImageIcon icon) {
			super(null, icon);
			this.demo = demo;
			if (icon == null)
				putValue(NAME, "New...");
			putValue(MNEMONIC_KEY, new Integer('N'));
			int mods = demo.getToolkit().getMenuShortcutKeyMask();
			KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_N, mods);
			putValue(ACCELERATOR_KEY, ks);
		}

		public void actionPerformed(ActionEvent e) {
			if (chooser == null) {
				chooser = new JFileChooser();
				chooser.setFileFilter(new ExtensionFileFilter(
						"Java Source Files", "java"));
			}
			int rc = chooser.showOpenDialog(demo);
			if (rc == JFileChooser.APPROVE_OPTION) {
				demo.openFile(chooser.getSelectedFile());
			}
		}

	}

	/**
	 * Lets the user open a file.
	 */
	static class SaveAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		private DemoRootPane demo;
		private JFileChooser chooser;

		public SaveAction(DemoRootPane demo, ImageIcon icon) {
			super(null, icon);
			this.demo = demo;
			if (icon == null)
				putValue(NAME, "Save...");
			putValue(MNEMONIC_KEY, new Integer('S'));
			int mods = demo.getToolkit().getMenuShortcutKeyMask();
			KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_S, mods);
			putValue(ACCELERATOR_KEY, ks);
		}

		public void actionPerformed(ActionEvent e) {
			if (chooser == null) {
				chooser = new JFileChooser();
				chooser.setFileFilter(new ExtensionFileFilter(
						"ThingML Source Files", "thingml"));
			}

			int rc = chooser.showSaveDialog(demo);
			// TODO: Fix this
			// if (rc == JFileChooser.APPROVE_OPTION) {
			// String name = chooser.getName(chooser.get);
			// System.out.println(name);
			// if (!name.endsWith(".thingml")) {
			// name.concat(".thingml");
			// chooser.setName(name)e(name);
			// }
			// demo.saveFile(chooser.getSelectedFile());
			// }
		}

	}

	/**
	 * Changes the look and feel of the demo application.
	 */
	static class LookAndFeelAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private LookAndFeelInfo info;
		private DemoRootPane demo;

		public LookAndFeelAction(DemoRootPane demo, LookAndFeelInfo info) {
			putValue(NAME, info.getName());
			this.demo = demo;
			this.info = info;
		}

		public void actionPerformed(ActionEvent e) {
			try {
				UIManager.setLookAndFeel(info.getClassName());
				SwingUtilities.updateComponentTreeUI(demo);
			} catch (RuntimeException re) {
				throw re; // FindBugs
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Changes the language being edited and installs appropriate language
	 * support.
	 */
	static class StyleAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private DemoRootPane demo;
		private String res;
		private String style;

		public StyleAction(DemoRootPane demo, String name, String res,
				String style) {
			putValue(NAME, name);
			this.demo = demo;
			this.res = res;
			this.style = style;
		}

		public void actionPerformed(ActionEvent e) {
			demo.setText(res, style);
		}

	}

}