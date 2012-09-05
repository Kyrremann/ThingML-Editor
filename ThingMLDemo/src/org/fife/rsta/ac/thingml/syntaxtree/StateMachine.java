package org.fife.rsta.ac.thingml.syntaxtree;

import javax.swing.tree.MutableTreeNode;

import org.fife.rsta.ac.thingml.tree.ThingMLTreeNode;

public class StateMachine {

	String name;
	States states;
	int offset;
	int line;

	public StateMachine(String name, States states, int offset, int line) {
		this.name = name;
		this.states = states;
		this.offset = offset;
		this.line = line;
	}

	public String printAst() {
		String ast = "(StateMachine " + name + "\n\t";
		if (states != null)
			ast += states.printAst();
		ast += ")";
		return ast;
	}

	public MutableTreeNode getTreeNode(ThingMLTreeNode root) {
		ThingMLTreeNode child = new ThingMLTreeNode(name);
		child.setOffset(offset);
		child.setLine(line);
        if(states != null)
                child.add(states.getTreeNode(child));
        return child;
	}
	
	public String toString() {
		return name + " - offset: " + offset;
	}
}