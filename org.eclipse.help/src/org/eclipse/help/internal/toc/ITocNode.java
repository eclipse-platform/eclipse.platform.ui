/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.toc;
import java.util.List;
/**
 * ITocNode interface.
 */
interface ITocNode {
	/**
	 * Returns child nodes
	 * @return List of ITocNode
	 */
	List getChildren();
	
	/**
	 * Adds another element as child of this element
	 * Modifies parents of a child as well
	 * @param child node to add as child
	 */
	void addChild(ITocNode child);
	
	/**
	 * Removes specified child.
	 * @param child child to remove
	 */
	void removeChild(ITocNode child);
	
	/**
	 * When a builder builds the navigation, each node
	 * must "accomodate" the builder by responding to the build() 
	 * command.
	 */
	void build(TocBuilder builder);
}