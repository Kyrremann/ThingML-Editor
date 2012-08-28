package org.fife.rsta.ac.thingml.syntaxtree;

import org.fife.rsta.ac.thingml.tree.ThingMLTreeNode;

public class Program {

	StateMachine stateMachine;

	public Program(StateMachine stateMachine) {
		this.stateMachine = stateMachine;
	}

	public String printAst() {
		return stateMachine.printAst();
	}

	public ThingMLTreeNode createAst() {
		ThingMLTreeNode root = new ThingMLTreeNode("root");
		root.add(stateMachine.getTreeNode(root));
		return root;
	}
}