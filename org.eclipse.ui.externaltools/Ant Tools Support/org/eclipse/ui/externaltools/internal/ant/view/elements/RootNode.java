/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.externaltools.internal.ant.view.elements;

import java.util.ArrayList;
import java.util.List;

/**
 * The root node of an ant node tree
 */
public class RootNode extends AntNode {

	private List projects= new ArrayList();
	
	public RootNode() {
		super(null);
	}
	
	/**
	 * Creates a new root node containing the given projects
	 * 
	 * @param projects the projects to add to this node
	 */
	public RootNode(ProjectNode[] projects) {
		super(null);
		for (int i = 0; i < projects.length; i++) {
			this.projects.add(projects[i]);
		}
	}
	
	/**
	 * Returns the list of projects stored in this root node
	 * 
	 * @return ProjectNode[] the projects in this node
	 */
	public ProjectNode[] getProjects() {
		return (ProjectNode[])projects.toArray(new ProjectNode[projects.size()]);
	}
	
	/**
	 * Returns whether this root node contains any projects
	 * 
	 * @return boolean Whether there are any projects
	 */
	public boolean hasProjects() {
		return !projects.isEmpty();
	}
	
	/**
	 * Adds the given project to this root node
	 * 
	 * @param project the project to add
	 */
	public void addProject(ProjectNode project) {
		projects.add(project);
	}
	
	/**
	 * Removes the given project from this root node. Has no effect if the given
	 * project is not a child of this root
	 * 
	 * @param project the project to remove
	 */
	public void removeProject(ProjectNode project) {
		projects.remove(project);
	}
	
	/**
	 * Removes all projects from this root node. Has no effect if this node has
	 * no projects.
	 */
	public void removeAllProjects() {
		projects.clear();
	}

}
