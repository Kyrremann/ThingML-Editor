/*
 * 11/14/2003
 *
 * RTextScrollPane.java - A JScrollPane that will only accept RTextAreas
 * so that it can display line numbers.
 * Copyright (C) 2003 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com/rsyntaxtextarea
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA.
 */
package org.fife.ui.rtextarea;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JScrollPane;


/**
 * An extension of <code>javax.swing.JScrollPane</code> that will only take
 * <code>RTextArea</code>s for its view.  This class has the ability to show
 * line numbers for its text component view, as well as per-line icons (for
 * bookmarks, debugging breakpoints, error markers, etc.).<p>
 *
 * The actual "meat" of these extras is contained in the {@link Gutter} class.
 * Each <code>RTextScrollPane</code> has a <code>Gutter</code> instance that
 * it uses as its row header.  The gutter is only made visible when one of its
 * features is being used (line numbering and/or icons).
 *
 * @author Robert Futrell
 * @version 0.9
 */
public class RTextScrollPane extends JScrollPane {

	private RTextArea textArea;
	private Gutter gutter;


	/**
	 * Constructor.  If you use this constructor, you must call
	 * {@link #setViewportView(Component)} and pass in an {@link RTextArea}
	 * for this scroll pane to render line numbers properly.
	 */
	public RTextScrollPane() {
		this(null, true);
	}


	/**
	 * Creates a scroll pane.  A default value will be used for line number
	 * color (gray), and the current line's line number will be highlighted.
	 *
	 * @param textArea The text area this scroll pane will contain.
	 */
	public RTextScrollPane(RTextArea textArea) {
		this(textArea, true);
	}


	/**
	 * Creates a scroll pane.  A default value will be used for line number
	 * color (gray), and the current line's line number will be highlighted.
	 *
	 * @param textArea The text area this scroll pane will contain.  If this is
	 *        <code>null</code>, you must call
	 *        {@link #setViewportView(Component)}, passing in an
	 *        {@link RTextArea}.
	 * @param lineNumbers Whether line numbers should be enabled.
	 */
	public RTextScrollPane(RTextArea textArea, boolean lineNumbers) {
		this(textArea, lineNumbers, Color.GRAY);
	}


	/**
	 * Creates a scroll pane with preferred size (width, height).
	 *
	 * @param area The text area this scroll pane will contain.  If this is
	 *        <code>null</code>, you must call
	 *        {@link #setViewportView(Component)}, passing in an
	 *        {@link RTextArea}.
	 * @param lineNumbers Whether line numbers are initially enabled.
	 * @param lineNumberColor The color to use for line numbers.
	 */
	public RTextScrollPane(RTextArea area, boolean lineNumbers,
							Color lineNumberColor) {

		super(area);

		// Create the text area and set it inside this scroll bar area.
		textArea = area;

		// Create the gutter for this document.
		Font defaultFont = new Font("Monospaced", Font.PLAIN, 12);
		gutter = new Gutter(textArea);
		gutter.setLineNumberFont(defaultFont);
		gutter.setLineNumberColor(lineNumberColor);
		setLineNumbersEnabled(lineNumbers);

		// Set miscellaneous properties.
		setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
		setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_AS_NEEDED);

	}


	/**
	 * Ensures the gutter is visible if it's showing anything.
	 */
	private void checkGutterVisibility() {
		int count = gutter.getComponentCount();
		if (count==0) {
			if (getRowHeader()!=null && getRowHeader().getView()==gutter) {
				setRowHeaderView(null);
			}
		}
		else {
			if (getRowHeader()==null || getRowHeader().getView()==null) {
				setRowHeaderView(gutter);
			}
		}
	}


	/**
	 * Returns the gutter.
	 *
	 * @return The gutter.
	 */
	public Gutter getGutter() {
		return gutter;
	}


	/**
	 * Returns <code>true</code> if the line numbers are enabled and visible.
	 *
	 * @return Whether or not line numbers are visible.
	 * @see #setLineNumbersEnabled(boolean)
	 */
	public boolean getLineNumbersEnabled() {
		return gutter.getLineNumbersEnabled();
	}


	/**
	 * Returns the text area being displayed.
	 *
	 * @return The text area.
	 * @see #setViewportView(Component)
	 */
	public RTextArea getTextArea() {
		return (RTextArea)getViewport().getView();
	}


	/**
	 * Returns whether the icon row header is enabled.
	 *
	 * @return Whether the icon row header is enabled.
	 * @see #setIconRowHeaderEnabled(boolean)
	 */
	public boolean isIconRowHeaderEnabled() {
		return gutter.isIconRowHeaderEnabled();
	}


	/**
	 * Toggles whether the icon row header (used for breakpoints, bookmarks,
	 * etc.) is enabled.
	 *
	 * @param enabled Whether the icon row header is enabled.
	 * @see #isIconRowHeaderEnabled()
	 */
	public void setIconRowHeaderEnabled(boolean enabled) {
		gutter.setIconRowHeaderEnabled(enabled);
		checkGutterVisibility();
	}


	/**
	 * Toggles whether or not line numbers are visible.
	 *
	 * @param enabled Whether or not line numbers should be visible.
	 * @see #getLineNumbersEnabled()
	 */
	public void setLineNumbersEnabled(boolean enabled) {
		gutter.setLineNumbersEnabled(enabled);
		checkGutterVisibility();
	}


	/**
	 * Sets the view for this scroll pane.  This must be an {@link RTextArea}.
	 *
	 * @param view The new view.
	 * @see #getTextArea()
	 */
	public void setViewportView(Component view) {
		if (!(view instanceof RTextArea)) {
			throw new IllegalArgumentException("view must be an RTextArea");
		}
		super.setViewportView(view);
		textArea = (RTextArea)view;
		if (gutter!=null) {
			gutter.setTextArea(textArea);
		}
	}


}