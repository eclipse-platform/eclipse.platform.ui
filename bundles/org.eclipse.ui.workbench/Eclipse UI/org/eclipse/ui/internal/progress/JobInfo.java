/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import java.util.ArrayList;

/**
 * JobInfo is the class that keeps track of the tree structure 
 * for objects that display job status in a tree.
 */
class JobInfo {
	String name;
	ArrayList children = new ArrayList();
	JobInfo parent;
	JobInfo currentChild;

	/**
	 * Create a new instance of the receiver with the supplied 
	 * task name as a child of parentObject,
	 * @param taskName
	 * @param parentObject
	 */
	JobInfo(String taskName, JobInfo parentObject) {
		this(taskName);
		parent = parentObject;
		parent.addChild(this);
	}

	/**
	 * Create a top level JobInfo.
	 * @param taskName
	 */
	JobInfo(String taskName) {
		name = taskName;
	}

	/**
	 * Return the name of the receiver.
	 * @return String
	 */
	String getName() {
		return name;
	}

	/**
	 * Return the displayString for the receiver.
	 * @return
	 */
	String getDisplayString() {
		return getName();
	}
	
	/**
	 * Return whether or not there is a parent for the receiver.
	 * @return boolean
	 */
	boolean hasParent() {
		return parent != null;
	}

	/**
	 * Return whether or not the receiver has children.
	 * @return boolean
	 */
	boolean hasChildren() {
		return children.size() > 0;
	}

	/**
	 * Return the children of the receiver.
	 * @return Object[]
	 */
	Object[] getChildren() {
		return children.toArray();
	}

	/**
	 * Return the parent of the receiver.
	 * @return JobInfo or <code>null</code>.
	 */
	JobInfo getParent() {
		return parent;
	}

	/**
	 * Add the supplied child to the receiver.
	 * @param child
	 */
	void addChild(JobInfo child) {
		children.add(child);
		currentChild = child;
	}

	/**
	 * Add the new job info to the lowest level child you
	 * currently have. If there is a currentChild add it to them,
	 * if not add it to the receiver.
	 * @param JobInfo
	 * @return JobInfo the job info this gets added to
	 */
	JobInfo addToLeafChild(JobInfo child) {
		if (currentChild == null) {
			addChild(child);
			return this;
		} else
			return currentChild.addToLeafChild(child);
	}

	/**
	 * Add the amount of work to the job info.
	 * @param workIncrement
	 */
	void addWork(double workIncrement) {
		//No work on a simple label- pass it down
		if (currentChild != null)
			currentChild.addWork(workIncrement);
	}
	/**
	 * Clear the collection of children.
	 */
	void clearChildren(){
		children.clear();
	}

}
