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
package org.eclipse.ui.internal.model;

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
