package org.eclipse.jface.examples.databinding.contentprovider.test;

import org.eclipse.jface.databinding.IReadableSet;

/**
 * This object will be given randomly-generated children
 *  
 * @since 3.2
 */
public class SimpleNode {
	private String nodeName;
	private IReadableSet children;

	public SimpleNode(String nodeName, IReadableSet children) {
		super();
		this.nodeName = nodeName;
		this.children = children;
	}

	public String getNodeName() {
		return nodeName;
	}

	public IReadableSet getChildren() {
		return children;
	}
	
}
