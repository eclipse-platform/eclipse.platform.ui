/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.quicksearch.internal.core.priority;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

/**
 * A PrioriTree is an implementation of PriorityFunction that is based on assigning specific priorities
 * to a finite set of paths. The paths are kept in a tree-like structure internally so that
 * assigning a priority to a given path also implicitly forces all the children leading to that
 * path to have a priority that is at least as high as that of the path itself.
 *
 * TODO: The priority function produced by this tree would probably be better if it also raised/affected
 * the priority of nodes in a subtree of a node, but not by as much as the node itself.
 *
 * @author Kris De Volder
 */
public class PrioriTree extends DefaultPriorityFunction {

	private static final boolean DEBUG =  false; //(""+Platform.getLocation()).contains("kdvolder");

	/**
	 * Creates an empty PrioriTree. This tree assigns provided default priority to any path.
	 */
	public static PrioriTree create() {
		return new PrioriTree(0, PRIORITY_DEFAULT);
	}

	private PrioriTree(int level, double defaultPriority) {
		this.level = level;
		this.priority = defaultPriority;
		this.childPriority = defaultPriority;
	}

	private static void debug(String string) {
		if (DEBUG) {
			System.out.println(string);
		}
	}

	/**
	 * Level of this node in the tree. This corresponds to the length of path ending in this node. I.e. 0 for the root node,
	 * 1 for children of root, 2 for children of level 1 nodes etc.
	 * <p>
	 * Level is used to easily check whether a node lookup returned an exact matching node or an ancestor.
	 */
	private int level;

	/**
	 * Priority assigned to any path lookup that ends here.
	 */
	private double priority = PRIORITY_IGNORE;
		//Must start out as low as possible because putting stuff into the tree will raise the priorities monotonically.
		//Note that normally we will not create a PrioriTree node unless there is some reason to
	    // assign a priority. So this value is expected to be always overwritten shortly after
	    // a node is created.

	/**
	 * Default priority assigned to all children of this node
	 */
	private double childPriority = PRIORITY_IGNORE;

	/**
	 * Children indexed by first segment in the path. This is only initialised if there's at least one
	 * child.
	 */
	private Map<String, PrioriTree> children = null;

	/**
	 * Set the priority for a given path. Also forces an update of all 'ancestor' nodes in the
	 * tree leading to this path to ensure that a parent node always has a priority at least as high as any
	 * of its children.
	 * <p>
	 * Also assigns the same priority to any descendants of the target path (but not the descendants of
	 * implicitly set parent nodes.
	 * <p>
	 * THe picture below illustrates: # marks a target node * marks nodes that implicitly
	 * also get set. And '-' marks nodes that are unaffected.
	 * <p>
	 * <pre>
	 *                                       *
	 *                                     /   \
	 *                                    -     *
	 *                                         / \
	 *                                        -  #
	 *                                          /  \
	 *                                         *    *
	 *                                        /\    /\
	 *                                       *  *  *  *
	 * </pre>
	 * <p>
	 * Note: this operation never reduces the priority of any path already in the tree.
	 * Thus if the same path gets assigned a priority more than once, only the highest priority will
	 * be retained in the tree node for that path.
	 */
	public void setPriority(IPath path, double priority) {
		this.priority = Math.max(this.priority, priority); //Use Math.max, never reduce priorities!
		if (path.segmentCount()>0) {
			// path leads to a child node
			PrioriTree child = ensureChild(path.segment(0));
			child.setPriority(path.removeFirstSegments(1), priority);
		} else {
			// path ends here
			setChildPriority(priority);
		}
	}

	private void setChildPriority(double priority) {
		double newChildPriority = Math.max(priority, childPriority);
		if (newChildPriority!=childPriority) {
			//Must update default child priority as well check if already created children priorities need
			// to be raised.
			this.childPriority = newChildPriority;
			if (children!=null) {
				for (PrioriTree child : children.values()) {
					//TODO: remove children that became redundant because the increase of childPriority in parent
					// makes them have no observable effect.
					//Currently this cleanup is not happening. This is inefficient but not incorrect.
					child.priority = Math.max(child.priority, newChildPriority);
					child.setChildPriority(newChildPriority);
				}
			}
		}
	}

	/**
	 * Ensure that this node has a child for a given segment string. If no node exists yet, create it.
	 * @param segment
	 * @return {@link PrioriTree} the existing or newly created child, never null.
	 */
	private PrioriTree ensureChild(String segment) {
		if (children==null) {
			children = new HashMap<>();
		}
		PrioriTree child = children.get(segment);
		if (child==null) {
			child = new PrioriTree(level+1, childPriority);
			children.put(segment, child);
		}
		return child;
	}

	@Override
	public double priority(IResource r) {
		double result = super.priority(r);
		if (result==PRIORITY_IGNORE) {
			//Ignored paths shouldn't be changed ... ever.
			return PRIORITY_IGNORE;
		}
		IPath path = r.getFullPath();
		PrioriTree node = this.lookup(path);
		if (node.level == path.segmentCount()) {
			//exact node found
			result = node.priority;
		} else {
			//ancestor node found
			result = node.childPriority;
		}
		debug("Priority for "+r.getFullPath() + " = " + result); //$NON-NLS-1$ //$NON-NLS-2$
		return result;
	}


	/**
	 * Locate tree node corresponding to a given path.
	 * @param path
	 * @return The node or null if no corresponding node exists in the tree.
	 */
	private PrioriTree lookup(IPath path) {
		PrioriTree found = null;
		if (path.segmentCount()>0) {
			PrioriTree child = getChild(path.segment(0));
			if (child!=null) {
				found = child.lookup(path.removeFirstSegments(1));
			}
		}
		return found==null?this:found;
	}

	/**
	 * Fetch the child for the corresponding segment String.
	 * @param segment
	 * @return The child or null if there is no such child.
	 */
	private PrioriTree getChild(String segment) {
		if (children!=null) {
			return children.get(segment);
		}
		return null;
	}

	/**
	 * For debugging purposes. Dumps tree data onto System.out
	 */
	public void dump() {
		dump("/", 0); //$NON-NLS-1$
	}

	private void dump(String name, int indent) {
		indent(indent);
		System.out.println(name + " : " +priority); //$NON-NLS-1$
		if (children!=null) {
			for (Entry<String, PrioriTree> c : children.entrySet()) {
				c.getValue().dump(c.getKey(), indent+1);
			}
		}
	}

	private void indent(int i) {
		for (int j = 0; j < i; j++) {
			System.out.print("  "); //$NON-NLS-1$
		}
	}

}
