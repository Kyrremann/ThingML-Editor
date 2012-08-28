/*
 * 04/07/2012
 *
 * Copyright (C) 2012 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com/rsyntaxtextarea
 *
 * This library is distributed under a modified BSD license.  See the included
 * RSTALanguageSupport.License.txt file for details.
 */
package org.fife.rsta.ac.thingml;

import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.parser.AbstractParser;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Parses ThingML code in an <code>RSyntaxTextArea</code>.
 * <p>
 * 
 * Like all RSTA <code>Parser</code>s, an <code>XmlParser</code> instance is
 * notified when the RSTA's text content changes. After a small delay, it will
 * parse the content as ThingML, building an AST and looking for any errors.
 * When parsing is complete, a property change event of type
 * {@link #PROPERTY_AST} is fired. Listeners can check the new value of the
 * property for an {@link ThingMLTreeNode} that represents the root of a tree
 * structure modeling the ThingML content in the text area. Note that the
 * <code>XmlTreeNode</code> may be incomplete if there were parsing/syntax
 * errors (it will usually be complete "up to" the error in the content).
 * <p>
 * 
 * This parser cannot be shared amongst multiple instances of
 * <code>RSyntaxTextArea</code>.
 * <p>
 * 
 * @author Kyrre Havik Eriksen
 * @version 0.1
 */
public class ThingMLParser extends AbstractParser {

	public static final String PROPERTY_AST = "ThingMLAST";

	private ThingMLLanguageSupport tls;
	private PropertyChangeSupport support;

	ThingMLParser(ThingMLLanguageSupport tls) {
		this.tls = tls;
		this.support = new PropertyChangeSupport(this);
	}

	// not sure if needed
	public void addPropertyChangeListener(String prop, PropertyChangeListener l) {
		System.out.println("Changing Property Listener");
		support.addPropertyChangeListener(prop, l);
	}

	public ParseResult parse(RSyntaxDocument document, String arg1) {
		System.out.println("Parsing " + document + " and " + arg1);
		return null;
	}

}
