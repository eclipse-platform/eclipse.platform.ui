/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
package org.eclipse.ui.externaltools.internal.ant.view.elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Representation of an ant build project.
 */
public class ProjectNode extends AntNode {

	private List targets= new ArrayList();
	private TargetNode defaultTarget= null;
	private String buildFileName;
	
	/**
	 * Creates a new project node with the given name, the given parent, and the
	 * given build file name.
	 * 
	 * @param name the project's name or <code>null</code> if none
	 * @param buildFileName
	 */
	public ProjectNode(String name, String buildFileName) {
		super(name);
		this.buildFileName= buildFileName;
	}

	/**
	 * Returns the targets in this project
	 * 
	 * @return TargetNode[] the targets in this project
	 */
	public TargetNode[] getTargets() {
		return (TargetNode[])targets.toArray(new TargetNode[targets.size()]);
	}
	
	/**
	 * Adds the given target to this project
	 * 
	 * @param target the target to add
	 */
	public void addTarget(TargetNode target) {
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
		return defaultTarget;
	}

}
