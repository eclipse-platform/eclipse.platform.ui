/**********************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tools.runtime;

import java.util.*;
import org.eclipse.core.tools.Policy;
import org.eclipse.osgi.service.resolver.BundleDescription;

/**
 *  This class is used to build up a dependency graph.  The full dependency
 *  graph is just a hash table containing PluginDependencyGraphNode's.  Each
 *  node represents one plug-in.  They are retrievable based on the plug-in
 *  id.  Each node has a list of all the plug-in ids for plug-ins that this
 *  plug-in requires (children) and another list of all the plug-in ids that
 *  require this particular plug-in (ancestors).
 */
public class PluginDependencyGraphNode {

	private BundleDescription descriptor = null;
	private Set children = new HashSet();
	private Set ancestors = new HashSet();

	/**
	 * Constructor for this class. Each node is associated with a plug-in so 
	 * we accept the plug-in descriptor here and keep it around for later use.
	 */
	public PluginDependencyGraphNode(BundleDescription descriptor) {
		this.descriptor = descriptor;
	}

	/**
	 * Add the given node to this node's set of ancestors.
	 */
	public void addAncestor(PluginDependencyGraphNode ancestor) {
		ancestors.add(ancestor);
	}

	/**
	 * Add the given node to this node's set of children.
	 */
	public void addChild(PluginDependencyGraphNode child) {
		children.add(child);
	}

	/**
	 * Return the identifier for this node. It is the unique plug-in identifier
	 * for this object's plug-in descriptor.
	 * 
	 * @return the plug-in id
	 */
	public String getId() {
		return descriptor.getSymbolicName();
	}

	/**
	 * Return a string representation of this object. It should be nicely formated
	 * and include the list of children and ancestor nodes.
	 */
	public String toDeepString() {
		StringBuffer buffer = new StringBuffer();

		// write ID
		writeln(buffer, 0, Policy.bind("stats.pluginid", descriptor.getSymbolicName())); //$NON-NLS-1$

		// write ancestors
		if (ancestors.size() == 0) {
			writeln(buffer, 1, Policy.bind("depend.noParentPlugins")); //$NON-NLS-1$
		} else {
			writeln(buffer, 1, Policy.bind("depend.requiredBy")); //$NON-NLS-1$
			for (Iterator i = ancestors.iterator(); i.hasNext();) {
				PluginDependencyGraphNode ancestor = (PluginDependencyGraphNode) i.next();
				writeln(buffer, 2, ancestor.getId());
			}
		}

		// write children
		if (children.size() == 0) {
			writeln(buffer, 1, Policy.bind("depend.noChildrenPlugins")); //$NON-NLS-1$
		} else {
			writeln(buffer, 1, Policy.bind("depend.requires")); //$NON-NLS-1$
			for (Iterator i = children.iterator(); i.hasNext();) {
				PluginDependencyGraphNode child = (PluginDependencyGraphNode) i.next();
				writeln(buffer, 2, child.getId());
			}
		}
		return buffer.toString();
	}

	/**
	 * Ultility method to write a string and cr to the given buffer. Indent the
	 * text the given number of tabs.
	 */
	private void writeln(StringBuffer buffer, int indent, String text) {
		for (int i = 0; i < indent; i++)
			buffer.append('\t');
		buffer.append(text);
		buffer.append('\n');
	}

	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof PluginDependencyGraphNode))
			return false;
		PluginDependencyGraphNode other = (PluginDependencyGraphNode) obj;
		return this.getId().equals(other.getId());
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return getId().hashCode();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("PluginDependencyGraphNode("); //$NON-NLS-1$
		buffer.append(descriptor.getSymbolicName());
		buffer.append(')');
		return buffer.toString();
	}
}