/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.topics;
import java.util.List;
/**
 * INavigationNode interface.
 */
interface INavigationNode {
	/**
	 * Returns child nodes
	 * @return List of INavigationNode
	 */
	List getChildren();
	
	/**
	 * Adds another element as child of this element
	 * Modifies parents of a child as well
	 * @param child node to add as child
	 */
	void addChild(INavigationNode child);
	
	/**
	 * Removes specified child.
	 * @param child child to remove
	 */
	void removeChild(INavigationNode child);
	
	/**
	 * When a builder builds the navigation, each node
	 * must "accomodate" the builder by responding to the build() 
	 * command.
	 */
	void build(NavigationBuilder builder);
}