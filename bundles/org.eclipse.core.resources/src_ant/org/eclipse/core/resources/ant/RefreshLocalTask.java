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
import org.eclipse.core.runtime.*;

/**
 * An Ant task which refreshes the Eclipse Platform's view of the local filesystem.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 * @see IResource#refreshLocal
 */
public class RefreshLocalTask extends Task {
	/**
	 * Unique identifier constant (value <code>"DEPTH_ZERO"</code>)
	 * indicating that refreshes should be performed only on the target
	 * resource itself
	 */
	public static final String DEPTH_ZERO = "zero";
	
	/**
	 * Unique identifier constant (value <code>"DEPTH_ONE"</code>)
	 * indicating that refreshes should be performed on the target
	 * resource and its children
	 */
	public static final String DEPTH_ONE = "one";
	
	/**
	 * Unique identifier constant (value <code>"DEPTH_INFINITE"</code>)
	 * indicating that refreshes should be performed on the target
	 * resource and all of its recursive children
	 */
	public static final String DEPTH_INFINITE = "infinite";
	
	/**
	 * The resource to refresh.
	 */
	protected IResource resource;
	
	/**
	 * The depth to refresh to.
	 */
	protected int depth = IResource.DEPTH_INFINITE;
	
	private IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

/**
 * Constructs a new <code>RefreshLocal</code> instance.
 */
public RefreshLocalTask() {
	super();
}

/**
 * Performs the refresh operation.
 * 
 * @exception BuildException thrown if a problem occurs during execution.
 */
public void execute() throws BuildException {
	if (resource == null)
		throw new BuildException("exception.resourceNotSpecified");
	try {
		resource.refreshLocal(depth, null);
	} catch (CoreException e) {
		throw new BuildException(e);
	}
}

/**
 * Sets the depth of this task appropriately.  The specified argument must
 * by one of <code>RefreshLocal.DEPTH_ZERO</code>, <code>RefreshLocal.DEPTH_ONE</code>
 * or <code>RefreshLocal.DEPTH_INFINITE</code>.
 * 
 * @param value the depth to refresh to		
 */
public void setDepth(String value) {
	if (DEPTH_ZERO.equalsIgnoreCase(value))
		depth = IResource.DEPTH_ZERO;
	else
		if (DEPTH_ONE.equalsIgnoreCase(value))
			depth = IResource.DEPTH_ONE;
		else
			if (DEPTH_INFINITE.equalsIgnoreCase(value))
				depth = IResource.DEPTH_INFINITE;
}

/**
 * Sets the root of the workspace resource tree to refresh.
 * 
 * @param value the root value
 */
public void setResource(String value) {
	IPath path = new Path(value);
	resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
	// if it does not exist we guess it is a folder
	if (resource == null)
		resource = ResourcesPlugin.getWorkspace().getRoot().getFolder(path);
}

}
