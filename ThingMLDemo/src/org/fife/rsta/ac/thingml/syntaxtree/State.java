package org.fife.rsta.ac.thingml.syntaxtree;

import org.fife.rsta.ac.thingml.tree.ThingMLTreeNode;

public class State extends States {

	String name;
	Transition transitions;
	States next;
	int offset;
	int line;

	public State(String name, Transition transitions, States next, int offset, int line) {
		this.name = name;
		this.transitions = transitions;
		this.next = next;
		this.offset = offset;
		this.line = line;
	}

	public String printAst() {
		String ast = "(State " + name + "\n\t";
		if (transitions != null) {
			ast += "\t";
			ast += transitions.printAst();
		}
		ast += ")\n";
		if (next != null) {
			ast += "\t";
			ast += next.printAst();
		}
		return ast;
	}

	public ThingMLTreeNode getTreeNode(ThingMLTreeNode root) {
		ThingMLTreeNode child = new ThingMLTreeNode(name);
		child.setOffset(this.offset);
		child.setLine(this.line);
		if(transitions != null)
			child.add(transitions.getTreeNode(child));
		if(next != null)
			root.add(next.getTreeNode(root));
		return child;
	}
}