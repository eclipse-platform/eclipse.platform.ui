/**********************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.resources.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;

/**
 * Ant task which runs the platform's incremental build facilities.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 */

public class IncrementalBuild extends Task {
	private String builder;
	private String project;
	private int kind = IncrementalProjectBuilder.INCREMENTAL_BUILD;

	/**
	 * Unique identifier constant (value <code>"KIND_INCREMENTAL"</code>)
	 * indicating that an incremental build should be performed.
	 */
	public final static String KIND_INCREMENTAL = "incremental";
	
	/**
	 * Unique identifier constant (value <code>"KIND_FULL"</code>)
	 * indicating that a full build should be performed.
	 */
	public final static String KIND_FULL = "full";
	
	/**
	 * Unique identifier constant (value <code>"KIND_AUTO"</code>)
	 * indicating that an auto build should be performed.
	 */
	public final static String KIND_AUTO = "auto";
	
	
/**
 * Constructs an <code>IncrementalBuild</code> instance.
 */
public IncrementalBuild() {
	super();
}
/**
 * Executes this task.
 * 
 * @exception BuildException thrown if a problem occurs during execution
 */
public void execute() throws BuildException {
	try {
		if (project == null) {
			ResourcesPlugin.getWorkspace().build(kind, null);
		} else {
			IProject target = ResourcesPlugin.getWorkspace().getRoot().getProject(project);
			if (builder == null)
				target.build(kind, null);
			else
				target.build(kind, builder, null, null);
		}
	} catch (CoreException e) {
		throw new BuildException(e);
	}
}
/**
 * Sets the name of the receiver's builder.
 * 
 * @param value the name of the receiver's builder
 */
public void setBuilder(String value) {
	builder = value;
}
/**
 * Sets the receiver's kind> attribute.  This value must be one
 * of <code>IncrementalBuild.KIND_FULL</code>, <code>IncrementalBuild.KIND_AUTO</code>,
 * <code>IncrementalBuild.KIND_INCR</code>.
 * 
 * @param kind the receiver's kind attribute
 */
public void setKind(String value) {
	if (IncrementalBuild.KIND_FULL.equalsIgnoreCase(value))
		kind = IncrementalProjectBuilder.FULL_BUILD;
	else if (IncrementalBuild.KIND_AUTO.equalsIgnoreCase(value))
		kind = IncrementalProjectBuilder.AUTO_BUILD;
	else if (IncrementalBuild.KIND_INCREMENTAL.equalsIgnoreCase(value))
		kind = IncrementalProjectBuilder.INCREMENTAL_BUILD;
}
/**
 * Sets the receiver's target project.
 * 
 * @param value the receiver's target project
 */
public void setProject(String value) {
	project = value;
}
}
