/**********************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.resources;

import java.io.DataInputStream;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.internal.watson.ElementTree;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

/**
 * Default tree reader that does not read anything. This is used in cases
 * where the tree format is unknown (for example when opening a workspace
 * from a future version).
 */
public abstract class WorkspaceTreeReader {
	/**
	 * Returns the tree reader associated with the given tree version number
	 */
	public static WorkspaceTreeReader getReader(Workspace workspace, int version) throws CoreException {
		switch (version) {
			case ICoreConstants.WORKSPACE_TREE_VERSION_1 :
				return new WorkspaceTreeReader_1(workspace);
			case ICoreConstants.WORKSPACE_TREE_VERSION_2 :
				return new WorkspaceTreeReader_2(workspace);
			default :
				// Unknown tree version - fail to read the tree
				throw new ResourceException(IResourceStatus.FAILED_READ_METADATA, null, Policy.bind("resources.format"), null); //$NON-NLS-1$
		}
	}

	/**
	 * Returns a snapshot from the stream. This default implementation does nothing.
	 */
	public abstract ElementTree readSnapshotTree(DataInputStream input, ElementTree complete, IProgressMonitor monitor) throws CoreException;

	/**
	 * Reads all workspace trees from the stream. This default implementation does nothing.
	 */
	public abstract void readTree(DataInputStream input, IProgressMonitor monitor) throws CoreException;

	/**
	 * Reads a project's trees from the stream. This default implementation does nothing.
	 */
	public abstract void readTree(IProject project, DataInputStream input, IProgressMonitor monitor) throws CoreException;
}