package org.fife.rsta.ac.thingml;

import org.fife.rsta.ac.AbstractLanguageSupport;
import org.fife.rsta.ac.thingml.ThingMLParser;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
 
public class ThingMLLanguageSupport extends AbstractLanguageSupport {

	private boolean showSyntaxErrors;

	public ThingMLLanguageSupport() {
		setAutoCompleteEnabled(true);
		//setParameterAssistanceEnabled(true);
		//setShowDescWindow(false);
		setShowSyntaxErrors(true);
	}

	public void install(RSyntaxTextArea textArea) {
		ThingMLParser mlParser = new ThingMLParser(this);
		textArea.addParser(mlParser);
		textArea.putClientProperty(PROPERTY_LANGUAGE_PARSER, mlParser);
	}

	public void uninstall(RSyntaxTextArea textArea) {
		uninstallImpl(textArea);

		ThingMLParser mlParser = getParser(textArea);
		if (mlParser != null) {
			textArea.removeParser(mlParser);
		}
	}
	
	/**
	 * Returns the ThingML parser running on a text area with this ThingML language
	 * support installed.
	 *
	 * @param textArea The text area.
	 * @return The ThingML parser.  This will be <code>null</code> if the text
	 *         area does not have this <tt>ThingMLLanguageSupport</tt> installed.
	 */
	public ThingMLParser getParser(RSyntaxTextArea textArea) {
		// Could be a parser for another language.
		Object mlParser = textArea.getClientProperty(PROPERTY_LANGUAGE_PARSER);
		if (mlParser instanceof ThingMLParser) {
			return (ThingMLParser) mlParser;
		}
		return null;
	}

/**
	 * Returns whether syntax errors are squiggle-underlined in the editor.
	 *
	 * @return Whether errors are squiggle-underlined.
	 * @see #setShowSyntaxErrors(boolean)
	 */
	public boolean getShowSyntaxErrors() {
		return showSyntaxErrors;
	}

	/**
	 * Sets whether syntax errors are squiggle-underlined in the editor.
	 * 
	 * @param show
	 *            Whether syntax errors are squiggle-underlined.
	 * @see #getShowSyntaxErrors()
	 */
	public void setShowSyntaxErrors(boolean show) {
		showSyntaxErrors = show;
	}

}
