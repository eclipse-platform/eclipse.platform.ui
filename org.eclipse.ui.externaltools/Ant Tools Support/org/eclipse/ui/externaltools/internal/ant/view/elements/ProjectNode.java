/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
package org.eclipse.ui.externaltools.internal.ant.view.elements;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.eclipse.ant.core.ProjectInfo;
import org.eclipse.ant.core.TargetInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.externaltools.internal.ant.model.AntUtil;

/**
 * Representation of an ant build project.
 */
public class ProjectNode extends AntNode {

	private List targets= null;
	private TargetNode defaultTarget= null;
	private String buildFileName;
	private boolean isErrorNode= false;
	
	/**
	 * Creates a new project node with the given name and the given build file
	 * name.
	 * 
	 * @param name the project's name or <code>null</code> if the project's
	 * name is not known. If this value is <code>null</code>, the file will be
	 * parsed the first time a value is requested that requires it.
	 * @param buildFileName
	 */
	public ProjectNode(String name, String buildFileName) {
		super(name);
		this.buildFileName= buildFileName;
	}
	
	/**
	 * Creates a new project node on the given build file.
	 */
	public ProjectNode(String buildFileName) {
		this(null, buildFileName);
	}

	/**
	 * Returns the targets in this project
	 * 
	 * @return TargetNode[] the targets in this project
	 */
	public TargetNode[] getTargets() {
		if (targets == null) {
			// Lazily parse the file to populate the targets
			parseBuildFile();
		}
		return (TargetNode[])targets.toArray(new TargetNode[targets.size()]);
	}
	
	/**
	 * Adds the given target to this project
	 * 
	 * @param target the target to add
	 */
	private void addTarget(TargetNode target) {
		targets.add(target);
		target.setParent(this);
	}
	
	/**
	 * Sets this project's default target to the given target
	 * 
	 * @param target this project's default target
	 */
	public void setDefaultTarget(TargetNode target) {
		defaultTarget= target;
	}
	
	/**
	 * Returns the name of the build file containing this project
	 * 
	 * @return String the name of this project's build file
	 */
	public String getBuildFileName() {
		return buildFileName;
	}
	
	/**
	 * Returns the default target in this project or <code>null</code> if none
	 * has been set
	 * 
	 * @return TargetNode the default target or <code>null</code> if none has
	 * been set
	 */
	public TargetNode getDefaultTarget() {
		if (targets == null) {
			// Lazily parse the file to populate the targets
			parseBuildFile();
		}
		return defaultTarget;
	}
	
	/**
	 * Sets this project's error node state
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
	
	/**
	 * Parses the given build file and populates the targets contained in the
	 * build file. If an error occurs while parsing the file, the error
	 * state will be set and a target error node will be added
	 */
	public void parseBuildFile() {
		clear();
		TargetInfo[] infos = null;
		try {
			infos = AntUtil.getTargets(buildFileName);
		} catch (CoreException e) {
			setErrorMessage("An exception occurred retrieving targets: " + e.getMessage());
			return;
		}
		if (infos.length < 1) {
			setErrorMessage("No targets found");
			return;
		}
		ProjectInfo projectInfo= infos[0].getProject();
		// Create Apache Ant objects
		Project project = new Project();
		if (projectInfo.getName() != null) {
			project.setName(projectInfo.getName());
		}
		
		for (int i = 0; i < infos.length; i++) {
			TargetInfo info = infos[i];
			if (info.isDefault()) {
				project.setDefault(info.getName());
			}
			Target target = new Target();
			target.setName(info.getName());
			String[] dependencies = info.getDependencies();
			StringBuffer depends = new StringBuffer();
			int numDependencies = dependencies.length;
			if (numDependencies > 0) {
				// Onroll the loop to avoid trailing comma
				depends.append(dependencies[0]);
			}
			for (int j = 1; j < numDependencies; j++) {
				depends.append(',').append(dependencies[j]);
			}
			target.setDepends(depends.toString());
			target.setDescription(info.getDescription());
			project.addTarget(target);
		}
		if (project.getDefaultTarget() == null) {
			setErrorMessage("No default target found");
			return;
		}
		// Set the project node data based on the Apache Ant data
		String projectName = project.getName();
		if (projectName == null) {
			projectName = "(unnamed)";
		}
		// Update the project name
		setName(projectName);
		setDescription(projectInfo.getDescription());
		Enumeration projTargets = project.getTargets().elements();
		while (projTargets.hasMoreElements()) {
			Target target = (Target) projTargets.nextElement();
			// Target Node -----------------
			Enumeration targetDependencies = target.getDependencies();
			TargetNode targetNode = new TargetNode(target.getName(), target.getDescription());
			while (targetDependencies.hasMoreElements()) {
				targetNode.addDependency((String) targetDependencies.nextElement());
			}
			addTarget(targetNode);
			if (targetNode.getName().equals(project.getDefaultTarget())) {
				setDefaultTarget(targetNode);
			}
			// Execution Path -------
			Vector topoSort = null;
			try {
				topoSort= project.topoSort(target.getName(), project.getTargets());
			} catch (BuildException be) {
				setErrorMessage(be.toString());
			}
			if (topoSort != null) {
				int n = topoSort.indexOf(target) + 1;
				while (topoSort.size() > n) {
					topoSort.remove(topoSort.size() - 1);
				}
				topoSort.trimToSize();
				ListIterator topoElements = topoSort.listIterator();
				while (topoElements.hasNext()) {
					int i = topoElements.nextIndex();
					Target topoTask = (Target) topoElements.next();
					targetNode.addToExecutionPath((i + 1) + ":" + topoTask.getName());
				}
			}
		}
	}
	
	/**
	 * Clear's this node's internally stored data
	 */
	private void clear() {
		targets= new ArrayList();
		setIsErrorNode(false);
		setDefaultTarget(null);
	}
	
	/**
	 * Sets the error message of this project and creates a new target child
	 * node with the message
	 *
	 * @param errorMessage the error message generated while parsing this
	 * project
	 */
	private void setErrorMessage(String errorMessage) {
		setName(getBuildFileName());
		setIsErrorNode(true);
		TargetNode target= new TargetNode(errorMessage, errorMessage);
		target.setIsErrorNode(true);
		addTarget(target);
	}

	/**
	 * Returns the name of this project, parsing the build file first if
	 * necessary.
	 * 
	 * @return String this project's name
	 */
	public String getName() {
		if (super.getName() == null) {
			parseBuildFile();
		}
		return super.getName();
	}

}
