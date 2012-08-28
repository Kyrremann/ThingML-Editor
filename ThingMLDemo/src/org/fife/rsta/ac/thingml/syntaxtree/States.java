package org.fife.rsta.ac.thingml.syntaxtree;

import org.fife.rsta.ac.thingml.tree.ThingMLTreeNode;

public abstract class States {

	String name;
	States next;
	int offset;

	public abstract String printAst();
	public abstract ThingMLTreeNode getTreeNode(ThingMLTreeNode root);
}