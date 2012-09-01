package org.fife.rsta.ac.thingml;

/*
 * 01/07/2009
 *
 * CCellRenderer.java - A cell renderer for C completions.
 * 
 * This library is distributed under a modified BSD license.  See the included
 * RSyntaxTextArea.License.txt file for details.
 */

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JList;

import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionCellRenderer;
import org.fife.ui.autocomplete.EmptyIcon;
import org.fife.ui.autocomplete.FunctionCompletion;
import org.fife.ui.autocomplete.VariableCompletion;

/**
 * The cell renderer used for the C programming language.
 * 
 * @author Robert Futrell
 * @version 1.0
 */
public class ThingMLCellRenderer extends CompletionCellRenderer {

	private static final long serialVersionUID = 3679075521194136131L;
	private Icon variableIcon;
	private Icon functionIcon;
	private Icon emptyIcon;

	/**
	 * Constructor.
	 */
	public ThingMLCellRenderer() {
		variableIcon = getIcon("images/var.png");
		functionIcon = getIcon("images/function.png");
		emptyIcon = new EmptyIcon(16);
	}

	/**
	 * Returns an icon.
	 * 
	 * @param resource
	 *            The icon to retrieve. This should either be a file, or a
	 *            resource loadable by the current ClassLoader.
	 * @return The icon.
	 */
	private Icon getIcon(String resource) {
		ClassLoader cl = getClass().getClassLoader();
		URL url = cl.getResource(resource);
		if (url == null) {
			File file = new File(resource);
			try {
				url = file.toURI().toURL();
			} catch (MalformedURLException mue) {
				mue.printStackTrace(); // Never happens
			}
		}
		return url != null ? new ImageIcon(url) : null;
	}

	/**
	 * {@inheritDoc}
	 */
	protected void prepareForOtherCompletion(JList list, Completion c,
			int index, boolean selected, boolean hasFocus) {
		super.prepareForOtherCompletion(list, c, index, selected, hasFocus);
		setIcon(emptyIcon);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void prepareForVariableCompletion(JList list,
			VariableCompletion vc, int index, boolean selected, boolean hasFocus) {
		super.prepareForVariableCompletion(list, vc, index, selected, hasFocus);
		setIcon(variableIcon);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void prepareForFunctionCompletion(JList list,
			FunctionCompletion fc, int index, boolean selected, boolean hasFocus) {
		super.prepareForFunctionCompletion(list, fc, index, selected, hasFocus);
		setIcon(functionIcon);
	}

}