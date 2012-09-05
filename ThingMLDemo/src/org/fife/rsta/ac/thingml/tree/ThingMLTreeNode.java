package org.fife.rsta.ac.thingml.tree;

import javax.swing.Icon;

import org.fife.rsta.ac.SourceTreeNode;

public class ThingMLTreeNode extends SourceTreeNode {

	private static final long serialVersionUID = -8551575054471449261L;
	private String name;
	private Icon icon;
	private int offset;
	private int line;
    /**
     * only to be used if you ever need to know where the weight is place...
     */
    public int weightOffset;
	
	public ThingMLTreeNode(String name) {
		super(name);
		this.name = name;
	}
	
	public String toString() {
		return name;
	}
	
	public int getLength() {
		return name.length();
	}

	public Icon getIcon() {
		return icon;
	}
	
	public int getOffset() {
		return offset;
	}
	
	public int getLine() {
		return line;
	}
	
	public void setLine(int line) {
		this.line = line;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public void setIcon(Icon icon) {
		this.icon = icon;
	}
}
