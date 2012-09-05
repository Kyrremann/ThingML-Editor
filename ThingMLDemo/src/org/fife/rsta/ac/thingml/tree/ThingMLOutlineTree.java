package org.fife.rsta.ac.thingml.tree;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.fife.rsta.ac.AbstractSourceTree;
import org.fife.rsta.ac.LanguageSupport;
import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.rsta.ac.thingml.ThingMLLanguageSupport;
import org.fife.rsta.ac.thingml.ThingMLParser;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

public class ThingMLOutlineTree extends AbstractSourceTree {

	private static final long serialVersionUID = 8873888980306173596L;
	private ThingMLParser parser;
	private RSyntaxTextArea textArea;
	private ThingMLEditorListener listener;
	private DefaultTreeModel model;

	public ThingMLOutlineTree() {
		// setSorted(false);
		setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 8));
		setRootVisible(false);
		setCellRenderer(new ThingMLTreeCellRenderer());
		model = new DefaultTreeModel(new DefaultMutableTreeNode("Nothing"));
		setModel(model);
		listener = new ThingMLEditorListener();
		addTreeSelectionListener(listener);
	}

	public void expandInitialNodes() {		
		for (int i = 0; i < getRowCount(); i++)
			collapseRow(i);

		expandRow(0);
	}

	public boolean gotoSelectedElement() {
		TreePath path = getLeadSelectionPath();
		if (path != null) {
			gotoElementAtPath(path);
			return true;
		}

		return false;
	}

	private void gotoElementAtPath(TreePath path) {
		Object lastNode = path.getLastPathComponent();
		if (lastNode instanceof ThingMLTreeNode) {
			ThingMLTreeNode node = (ThingMLTreeNode) lastNode;
			int len = node.getLength();
			if (len > -1) { // Should always be true
				int offs = node.getOffset();
				int line = node.getLine();
				offs += parser.getLineOffset(line); // + line;
				textArea.select(offs, offs + len);
			}
		}
	}

	public void listenTo(RSyntaxTextArea textArea) {

		if (this.textArea != null) {
			uninstall();
		}

		if (textArea == null) {
			return;
		}

		this.textArea = textArea;
		textArea.addPropertyChangeListener(
				RSyntaxTextArea.SYNTAX_STYLE_PROPERTY, listener);

		checkForThingMLParsing();
	}

	private void checkForThingMLParsing() {
		if (parser != null) {
			parser.removePropertyChangeListener(ThingMLParser.PROPERTY_AST,
					listener);
			parser = null;
		}

		LanguageSupportFactory lsf = LanguageSupportFactory.get();
		LanguageSupport support = lsf
				.getSupportFor(SyntaxConstants.SYNTAX_STYLE_THINGML);
		ThingMLLanguageSupport languageSupport = (ThingMLLanguageSupport) support;

		parser = languageSupport.getParser(textArea);

		if (parser != null) { // Should always be true
			parser.addPropertyChangeListener(ThingMLParser.PROPERTY_AST,
					listener);
			ThingMLTreeNode root = parser.getAst();
			update(root);
		} else {
			update((ThingMLTreeNode) null); // Clear the tree
		}
	}

	private void update(ThingMLTreeNode ast) {
		ThingMLTreeNode root = new ThingMLTreeNode("root");
		
		if (ast == null) {
			model.setRoot(root);
			return;
		}
		
		model.setRoot(ast);
		refresh();
	}

	public void uninstall() {

		if (parser != null) {
			parser.removePropertyChangeListener(ThingMLParser.PROPERTY_AST,
					listener);
			parser = null;
		}

		if (textArea != null) {
			textArea.removePropertyChangeListener(
					RSyntaxTextArea.SYNTAX_STYLE_PROPERTY, listener);
			textArea = null;
		}
	}

	/**
	 * Overridden to also update the UI of the child cell renderer.
	 */
	public void updateUI() {
		super.updateUI();
		setCellRenderer(new ThingMLTreeCellRenderer());
	}

	/**
	 * Listens for events this tree is interested in (events in the associated
	 * editor, for example), as well as events in this tree.
	 */
	private class ThingMLEditorListener implements PropertyChangeListener,
			TreeSelectionListener {

		/**
		 * Called whenever the text area's syntax style changes, as well as when
		 * it is re-parsed.
		 */
		public void propertyChange(PropertyChangeEvent e) {

			String name = e.getPropertyName();

			// If the text area is changing the syntax style it is editing
			if (RSyntaxTextArea.SYNTAX_STYLE_PROPERTY.equals(name)) {
				checkForThingMLParsing();
			}

			else if (ThingMLParser.PROPERTY_AST.equals(name)) {
				ThingMLTreeNode root = (ThingMLTreeNode) e.getNewValue();
				update(root);
			}

		}

		/**
		 * Selects the corresponding element in the text editor when a user
		 * clicks on a node in this tree.
		 */
		public void valueChanged(TreeSelectionEvent e) {
			if (getGotoSelectedElementOnClick()) {
				gotoSelectedElement();
				TreePath newPath = e.getNewLeadSelectionPath();
				if (newPath != null) {
					gotoElementAtPath(newPath);
				}
			}
		}
	}
}
