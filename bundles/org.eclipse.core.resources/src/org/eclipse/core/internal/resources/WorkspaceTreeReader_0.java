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
package org.eclipse.core.internal.resources;

/**
 * This class is temporary only. It is only a hack because some old files do not have the
 * version number in it. It should be removed when we guarantee that all users of the platform
 * are running with versioned files.
 * Its going to happen at the same time we fix the version number of the files from the
 * "random numbers" to 1, 2, 3, 4...
 **/
public class WorkspaceTreeReader_0 extends WorkspaceTreeReader {
public WorkspaceTreeReader_0(Workspace workspace) {
	super(workspace);
}
}
