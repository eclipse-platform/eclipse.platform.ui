package org.eclipse.ant.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import java.util.Vector;
import org.apache.tools.ant.*;
import org.apache.tools.ant.types.PatternSet;

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
public class RefreshLocal extends Task {
	protected IResource resource;
	protected int depth = IResource.DEPTH_INFINITE;
	private IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

public RefreshLocal() {
		super();
}

/**
 * Performs the refresh operation.
 */
public void execute() throws BuildException {
	// make sure we don't have an illegal set of options
	validateAttributes();

	// deal with the single resource
	if (resource != null) {
		if (!resource.exists())
			throw new BuildException("Resource " + resource + " not found");
		try {
			resource.refreshLocal(depth, null);
			return;
		} catch (CoreException e) {
			throw new BuildException(e);
		}
	}
}

protected void refreshResources(String[] resources) throws BuildException {
	for (int i = 0; i < resources.length; i++) {
		IResource target = root.findMember(resources[i]);
		if (target == null)
			throw new BuildException("Resource " + resources[i] + " not found");
		try {
			target.refreshLocal(depth, null);
		} catch (CoreException e) {
			throw new BuildException(e);
		}
	}
}

/**
 * Sets the depth of this task to one of <code>IResource.DEPTH_ZERO</code>,
 * <code>IResource.DEPTH_ONE</code> or <code>IResource.DEPTH_INFINITE</code>.
 */
public void setDepth(String value) {
	if ("zero".equalsIgnoreCase(value))
		depth = IResource.DEPTH_ZERO;
	else
		if ("one".equalsIgnoreCase(value))
			depth = IResource.DEPTH_ONE;
		else
			if ("infinite".equalsIgnoreCase(value))
				depth = IResource.DEPTH_INFINITE;
}

/**
 * Sets the root of the workspace resource tree to refresh.
 */
public void setResource(String value) {
	resource = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(value));
}

protected void validateAttributes() throws BuildException {
}
}
