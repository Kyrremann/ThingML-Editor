package org.fife.rsta.ac.thingml.syntaxtree;

import javax.swing.ImageIcon;

import org.fife.rsta.ac.thingml.tree.ThingMLTreeNode;

public class FinalState extends States {

	String name;
	States next;
	int offset;
	int line;

	public FinalState(String name, States next, int offset, int line) {
		this.name = name;
		this.next = next;
		this.offset = offset;
		this.line = line;
	}

	public String printAst() {
		String ast = "(Final " + name + ")\n\t";
		if (next != null)
			ast += next.printAst();
		return ast;
	}

	public ThingMLTreeNode getTreeNode(ThingMLTreeNode root) {
		ThingMLTreeNode child = new ThingMLTreeNode(name);
		child.setOffset(this.offset);
		child.setLine(line);
		child.setIcon(new ImageIcon(getClass().getResource("finalstate.png")));
		if (next != null)
			root.add(next.getTreeNode(root));
		return child;
	}
}