package org.eclipse.ui.externaltools.internal.ant.view.elements;
/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

/**
 * Representation of an ant target
 */
public class TargetNode extends AntNode {
	private DependencyNode dependencies;
	private ExecutionPathNode executionPath;
	private String description;
	private boolean isErrorNode= false;
	
	/**
	 * Creates a new target node with the given parent node, name, and target
	 * dependencies
	 * 
	 * @param parent the new node's parent
	 * @param name the new node's name
	 * @param description the target description or <code>null</code> if the
	 * target has no description
	 */
	public TargetNode(String name, String description) {
		super(name);
		this.dependencies= new DependencyNode(this);
		this.executionPath= new ExecutionPathNode(this);
		addToExecutionPath(getName());
		this.description= description;
	}
	
	/**
	 * Adds the given dependency to the list of this target's dependencies
	 * 
	 * @param dependency the dependency to add
	 */
	public void addDependency(String dependency) {
		dependencies.add(dependency);
	}
	
	/**
	 * Adds the given target to the list of targets that will be executed when
	 * this target is run. Targets should be added in the order that they will
	 * be executed. This target will always be the last target in the list and
	 * does not need to be added.
	 * 
	 * @param target the target to add
	 */
	public void addToExecutionPath(String target) {
		executionPath.addTarget(target);
	}

	/**
	 * Returns the dependency node containing the names of the targets on which
	 * this target depends
	 * 
	 * @return DependencyNode the node containing the names of this target's
	 * dependencies
	 */
	public DependencyNode getDependencies() {
		return dependencies;
	}
	
	/**
	 * Returns the execution path node containing the names of targets which
	 * will be executed when this target is run in the order that they will be
	 * run.
	 * 
	 * @return ExecutionPathNode the node containing the execution path of this
	 * target
	 */
	public ExecutionPathNode getExecutionPath() {
		return executionPath;
	}
	
	/**
	 * Returns the ProjectNode containing this target. This method is equivalent
	 * to calling getParent() and casting the result to a ProjectNode.
	 * 
	 * @return ProjectNode the project containing this target
	 */
	public ProjectNode getProject() {
		return (ProjectNode) getParent();
	}
	
	/**
	 * Returns this target's description
	 * 
	 * @return String this target's description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets this target's error node state
	 *
	 * @param boolean whether or not an error occurred while parsing this node
	 */
	public void setIsErrorNode(boolean isErrorNode) {
		this.isErrorNode= isErrorNode;
	}

	/**
	 * Returns whether an error occurred while parsing this Ant node
	 *
	 * @return whether an error occurred while parsing this Ant node
	 */
	public boolean isErrorNode() {
		return isErrorNode;
	}

}
