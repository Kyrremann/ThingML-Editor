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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.*;
import javax.swing.text.TextAction;
import javax.swing.tree.TreeNode;

import org.fife.rsta.ac.AbstractSourceTree;
import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.rsta.ac.thingml.ThingMLCellRenderer;
import org.fife.rsta.ac.thingml.tree.ThingMLOutlineTree;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.LanguageAwareCompletionProvider;
import org.fife.ui.autocomplete.ShorthandCompletion;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.FileLocation;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.ToolTipSupplier;

/**
 * The root pane used by the demos. This allows both the applet and the
 * stand-alone application to share the same UI.
 * 
 * @author Robert Futrell
 * @version 1.0
 */
class DemoRootPane extends JRootPane implements HyperlinkListener,
		SyntaxConstants, Actions {

	private static final long serialVersionUID = 6176273020828452215L;
	private JScrollPane treeSP;
	private AbstractSourceTree tree;
	private RTextScrollPane scrollPane;
	private RSyntaxTextArea textArea;
	private AutoCompletion autoCompletion;

	private JCheckBoxMenuItem cellRenderingItem;
	private JCheckBoxMenuItem showDescWindowItem;
	private JCheckBoxMenuItem paramAssistanceItem;

	public DemoRootPane() {
		this(null);
	}

	public DemoRootPane(CompletionProvider provider) {

		AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory
				.getDefaultInstance();
		atmf.putMapping("text/thingml",
				"org.fife.ui.rsyntaxtextarea.modes.ThingMLTokenMaker");
		TokenMakerFactory.setDefaultInstance(atmf);

		textArea = createTextArea();
		setText("ThingMLExample.txt", SYNTAX_STYLE_THINGML);
		scrollPane = new RTextScrollPane(textArea, true);
		scrollPane.setIconRowHeaderEnabled(true);
		scrollPane.getGutter().setBookmarkingEnabled(true);

		// Install auto-completion onto our text area
		if (provider == null)
			provider = createCompletionProvider();

		autoCompletion = new AutoCompletion(provider);
		autoCompletion.setListCellRenderer(new ThingMLCellRenderer());
		autoCompletion.setShowDescWindow(true);
		autoCompletion.setParameterAssistanceEnabled(true);
		autoCompletion.install(textArea);

		textArea.setToolTipSupplier((ToolTipSupplier) provider);
		ToolTipManager.sharedInstance().registerComponent(textArea);

		// Dummy tree keeps JViewport's "background" looking right initially
		JTree dummy = new JTree((TreeNode) null);
		treeSP = new JScrollPane(dummy);

		// JPanel cp = new JPanel(new BorderLayout());
		final JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeSP,
				scrollPane);
		SwingUtilities.invokeLater(new Runnable() {
			   public void run() {
			      sp.setDividerLocation(150);
			   }
			});
		sp.setContinuousLayout(true);
		setContentPane(sp);
		setJMenuBar(createMenuBar());

		refreshSourceTree();
	}

	private JMenuBar createMenuBar() {

		JMenuBar mb = new JMenuBar();
		ButtonGroup bg = new ButtonGroup();

		JMenu menu = new JMenu("File");
		menu.add(new JMenuItem(new NewFileAction(this)));
		menu.add(new JMenuItem(new OpenAction(this)));
		menu.add(new JMenuItem(new SaveAction(this)));
		menu.addSeparator();
		menu.add(new JMenuItem(new TextAction("New window") {

			private static final long serialVersionUID = 4353427735637904252L;

			public void actionPerformed(ActionEvent arg0) {
				DemoRootPane demoRootPane = new DemoRootPane(autoCompletion
						.getCompletionProvider());
				demoRootPane.setVisible(true);
			}
		}));
		menu.add(new JMenuItem(new ExitAction()));
		mb.add(menu);

		menu = new JMenu("View");
		Action renderAction = new FancyCellRenderingAction();
		cellRenderingItem = new JCheckBoxMenuItem(renderAction);
		cellRenderingItem.setSelected(true);
		menu.add(cellRenderingItem);
		Action descWindowAction = new ShowDescWindowAction();
		showDescWindowItem = new JCheckBoxMenuItem(descWindowAction);
		showDescWindowItem.setSelected(true);
		menu.add(showDescWindowItem);
		Action paramAssistanceAction = new ParameterAssistanceAction();
		paramAssistanceItem = new JCheckBoxMenuItem(paramAssistanceAction);
		paramAssistanceItem.setSelected(true);
		menu.add(paramAssistanceItem);
		mb.add(menu);

		menu = new JMenu("LookAndFeel");
		bg = new ButtonGroup();
		LookAndFeelInfo[] infos = UIManager.getInstalledLookAndFeels();
		for (int i = 0; i < infos.length; i++) {
			addItem(new LookAndFeelAction(this, infos[i]), bg, menu);
		}
		mb.add(menu);

		menu = new JMenu("Help");
		menu.add(new JMenuItem(new AboutAction(this)));
		mb.add(menu);

		return mb;

	}

	/**
	 * Creates the text area for this application.
	 * 
	 * @return The text area.
	 */
	private RSyntaxTextArea createTextArea() {
		RSyntaxTextArea textArea = new RSyntaxTextArea(30, 100);
		LanguageSupportFactory.get().register(textArea);
		textArea.setCaretPosition(0);
		textArea.addHyperlinkListener(this);
		textArea.requestFocusInWindow();
		textArea.setMarkOccurrences(true);
		textArea.setCodeFoldingEnabled(true);
		ToolTipManager.sharedInstance().registerComponent(textArea);
		return textArea;
	}

	/**
	 * Focuses the text area.
	 */
	void focusTextArea() {
		textArea.requestFocusInWindow();
	}

	/**
	 * Called when a hyperlink is clicked in the text area.
	 * 
	 * @param e
	 *            The event.
	 */
	public void hyperlinkUpdate(HyperlinkEvent e) {
		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			URL url = e.getURL();
			if (url == null) {
				UIManager.getLookAndFeel().provideErrorFeedback(null);
			} else {
				JOptionPane.showMessageDialog(this,
						"URL clicked:\n" + url.toString());
			}
		}
	}

	/**
	 * Opens a file in the editor (as opposed to one of the pre-defined code
	 * examples).
	 * 
	 * @param file
	 *            The file to open.
	 */
	public void openFile(File file) {
		try {
			BufferedReader r = new BufferedReader(new FileReader(file));
			textArea.read(r, null);
			textArea.setCaretPosition(0);
			r.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			UIManager.getLookAndFeel().provideErrorFeedback(this);
			return;
		}
	}

	/**
	 * TODO: create save file method
	 * 
	 * @param name
	 */
	public void saveFile(File file) {
		try {
			FileWriter fileWriter = new FileWriter(file);
			BufferedWriter writer = new BufferedWriter(fileWriter);
			writer.write(textArea.getText());
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Displays a tree view of the current source code, if available for the
	 * current programming language.
	 */
	private void refreshSourceTree() {

		if (tree != null) {
			tree.uninstall();
			treeSP.remove(tree);
		}

		String language = textArea.getSyntaxEditingStyle();
		if (SyntaxConstants.SYNTAX_STYLE_THINGML.equals(language)) {
			tree = new ThingMLOutlineTree();
		} else {
			tree = null;
		}

		if (tree != null) {
			tree.listenTo(textArea);
			treeSP.setViewportView(tree);
			treeSP.revalidate();
		}
	}

	/**
	 * Sets the content in the text area to that in the specified resource.
	 * 
	 * @param resource
	 *            The resource to load.
	 * @param style
	 *            The syntax style to use when highlighting the text.
	 */
	void setText(String resource, String style) {

		textArea.setSyntaxEditingStyle(style);

		ClassLoader cl = getClass().getClassLoader();
		BufferedReader r = null;
		try {
			r = new BufferedReader(new InputStreamReader(
					cl.getResourceAsStream("examples/" + resource), "UTF-8"));
			textArea.read(r, null);
			r.close();
			textArea.setCaretPosition(0);
			textArea.discardAllEdits();

			if (treeSP != null)
				refreshSourceTree();

		} catch (RuntimeException re) {
			throw re; // FindBugs
		} catch (Exception e) {
			textArea.setText("Type here to see syntax highlighting");
		}

	}

	/**
	 * Returns the provider to use when editing code.
	 * 
	 * @return The provider.
	 * @see #createCommentCompletionProvider()
	 * @see #createStringCompletionProvider()
	 */
	private CompletionProvider createCodeCompletionProvider() {

		DefaultCompletionProvider defaultCompletionProvider = new DefaultCompletionProvider();
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader
				.getResourceAsStream("data/thingml.xml");
		try {
			if (inputStream != null) {
				defaultCompletionProvider.loadFromXML(inputStream);
				inputStream.close();
			} else {
				defaultCompletionProvider.loadFromXML(new File(
						"data/thingml.xml"));
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		// defaultCompletionProvider.addCompletion(new ShorthandCompletion(
		// defaultCompletionProvider, "StateMachine",
		// "StateMachine statemachine {\n}", "Defines a Statechart."));
		// defaultCompletionProvider.addCompletion(new ShorthandCompletion(
		// defaultCompletionProvider, "State", "State state {}",
		// "efines a state inside a StateMachine"));

		return defaultCompletionProvider;
	}

	private void addItem(Action a, ButtonGroup bg, JMenu menu) {
		JRadioButtonMenuItem item = new JRadioButtonMenuItem(a);
		bg.add(item);
		menu.add(item);
	}

	/**
	 * Returns the provider to use when in a comment.
	 * 
	 * @return The provider.
	 * @see #createCodeCompletionProvider()
	 * @see #createStringCompletionProvider()
	 */
	private CompletionProvider createCommentCompletionProvider() {
		DefaultCompletionProvider cp = new DefaultCompletionProvider();
		cp.addCompletion(new BasicCompletion(cp, "TODO:", "A to-do reminder"));
		cp.addCompletion(new BasicCompletion(cp, "FIXME:",
				"A bug that needs to be fixed"));
		return cp;
	}

	/**
	 * Returns the completion provider to use when the caret is in a string.
	 * 
	 * @return The provider.
	 * @see #createCodeCompletionProvider()
	 * @see #createCommentCompletionProvider()
	 */
	private CompletionProvider createStringCompletionProvider() {
		DefaultCompletionProvider cp = new DefaultCompletionProvider();
		cp.addCompletion(new BasicCompletion(cp, "\\n", "Newline",
				"Prints a newline"));
		return cp;
	}

	/**
	 * Creates the completion provider for a C editor. This provider can be
	 * shared among multiple editors.
	 * 
	 * @return The provider.
	 */
	private CompletionProvider createCompletionProvider() {

		// Create the provider used when typing code.
		CompletionProvider codeCP = createCodeCompletionProvider();

		// The provider used when typing a string.
		CompletionProvider stringCP = createStringCompletionProvider();

		// The provider used when typing a comment.
		CompletionProvider commentCP = createCommentCompletionProvider();

		// Create the "parent" completion provider.
		LanguageAwareCompletionProvider provider = new LanguageAwareCompletionProvider(
				codeCP);
		provider.setStringCompletionProvider(stringCP);
		provider.setCommentCompletionProvider(commentCP);

		return provider;

	}

	/**
	 * Focuses the text area.
	 */
	public void focusEditor() {
		textArea.requestFocusInWindow();
	}

	/**
	 * Toggles whether the completion window uses "fancy" rendering.
	 */
	private class FancyCellRenderingAction extends AbstractAction {

		private static final long serialVersionUID = 4095659185154995286L;

		public FancyCellRenderingAction() {
			putValue(NAME, "Fancy Cell Rendering");
		}

		public void actionPerformed(ActionEvent e) {
			boolean fancy = cellRenderingItem.isSelected();
			autoCompletion
					.setListCellRenderer(fancy ? new ThingMLCellRenderer()
							: null);
		}

	}

	/**
	 * Toggles whether parameter assistance is enabled.
	 */
	private class ParameterAssistanceAction extends AbstractAction {

		private static final long serialVersionUID = 8083581458377031473L;

		public ParameterAssistanceAction() {
			putValue(NAME, "Function Parameter Assistance");
		}

		public void actionPerformed(ActionEvent e) {
			boolean enabled = paramAssistanceItem.isSelected();
			autoCompletion.setParameterAssistanceEnabled(enabled);
		}

	}

	/**
	 * Toggles whether the description window is visible.
	 */
	private class ShowDescWindowAction extends AbstractAction {
		private static final long serialVersionUID = -9093098557558432695L;

		public ShowDescWindowAction() {
			putValue(NAME, "Show Description Window");
		}

		public void actionPerformed(ActionEvent e) {
			boolean show = showDescWindowItem.isSelected();
			autoCompletion.setShowDescWindow(show);
		}

	}
}