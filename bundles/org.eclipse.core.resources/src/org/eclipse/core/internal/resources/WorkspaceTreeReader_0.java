package org.eclipse.core.internal.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

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
