package org.fife.rsta.ac.thingml.syntaxtree;

import javax.swing.ImageIcon;

import org.fife.rsta.ac.thingml.tree.ThingMLTreeNode;

public class Transition {

	String name;
	String weight;
	Transition next;
	int offset;
	int weightOffset;
	int line;

	public Transition(String weight, String name, Transition next, int offset,
			int weightOffset, int line) {
		this.name = name;
		this.weight = weight;
		this.next = next;
		this.offset = offset;
		this.weightOffset = weightOffset;
		this.line = line;
	}

	public String printAst() {
		String ast = "(Transition \n\t\t\t(Weight " + weight
				+ ")\n\t\t\t(Target " + name + "))\n\t";
		if (next != null) {
			ast += "\t";
			ast += next.printAst();
		}
		return ast;
	}

	public ThingMLTreeNode getTreeNode(ThingMLTreeNode root) {
		ThingMLTreeNode child = new ThingMLTreeNode(name);
		child.setOffset(this.offset);
		child.weightOffset = weightOffset;
		child.setLine(line);
		child.setIcon(new ImageIcon(getClass().getResource("transition.png")));
		if (next != null)
			root.add(next.getTreeNode(root));
		return child;
	}
}