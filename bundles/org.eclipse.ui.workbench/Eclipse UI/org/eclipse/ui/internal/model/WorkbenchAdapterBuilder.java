package org.eclipse.ui.internal.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

/**
 * Registers the adapters on core constructs
 * used in the workbench UI.
 */
public class WorkbenchAdapterBuilder {
/**
 * Creates extenders and registers 
 */
public void registerAdapters() {
	IAdapterManager manager = Platform.getAdapterManager();
	IAdapterFactory factory = new WorkbenchAdapterFactory();
	manager.registerAdapters(factory, IWorkspace.class);
	manager.registerAdapters(factory, IWorkspaceRoot.class);
	manager.registerAdapters(factory, IProject.class);
	manager.registerAdapters(factory, IFolder.class);
	manager.registerAdapters(factory, IFile.class);
	manager.registerAdapters(factory, IMarker.class);
}
}
