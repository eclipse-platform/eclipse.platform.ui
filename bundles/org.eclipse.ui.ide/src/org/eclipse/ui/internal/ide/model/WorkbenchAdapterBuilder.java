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
package org.eclipse.ui.internal.ide.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;

/**
 * Registers the adapters on core constructs
 * used in the workbench UI.
 */
public final class WorkbenchAdapterBuilder {
    /**
     * Creates extenders and registers 
     */
    public static void registerAdapters() {
        IAdapterManager manager = Platform.getAdapterManager();
        IAdapterFactory factory = new WorkbenchAdapterFactory();
        manager.registerAdapters(factory, IWorkspace.class);
        manager.registerAdapters(factory, IWorkspaceRoot.class);
        manager.registerAdapters(factory, IProject.class);
        manager.registerAdapters(factory, IFolder.class);
        manager.registerAdapters(factory, IFile.class);
        manager.registerAdapters(factory, IMarker.class);

        // properties adapters
        IAdapterFactory paFactory = new StandardPropertiesAdapterFactory();
        manager.registerAdapters(paFactory, IWorkspace.class);
        manager.registerAdapters(paFactory, IWorkspaceRoot.class);
        manager.registerAdapters(paFactory, IProject.class);
        manager.registerAdapters(paFactory, IFolder.class);
        manager.registerAdapters(paFactory, IFile.class);
        manager.registerAdapters(paFactory, IMarker.class);
    }
}