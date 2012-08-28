package org.fife.rsta.ac.thingml.tree;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

public class ThingMLTreeCellRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = 7428444299283838560L;

	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf,
			int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
							row, hasFocus);
		if (value instanceof ThingMLTreeNode) { // Should always be true
			ThingMLTreeNode node = (ThingMLTreeNode) value;
			setText(node.toString());
		}
		return this;
	}
}
