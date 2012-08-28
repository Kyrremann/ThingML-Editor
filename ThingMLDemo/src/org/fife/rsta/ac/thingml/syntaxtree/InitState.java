package org.fife.rsta.ac.thingml.syntaxtree;

import org.fife.rsta.ac.thingml.tree.ThingMLTreeNode;

public class InitState extends States {

	String name;
	States next;
	int offset;

	public InitState(String name, States next, int offset) {
		this.name = name;
		this.next = next;
		this.offset = offset;
	}

	public String printAst() {
		String ast = "(Init " + name + ")\n\t";
		if (next != null)
			ast += next.printAst();
		return ast;
	}
	
	public ThingMLTreeNode getTreeNode(ThingMLTreeNode root) {
		ThingMLTreeNode child = new ThingMLTreeNode(name);
		child.setOffset(this.offset);
		if(next != null)
			root.add(next.getTreeNode(root));
		return child;
	}
}