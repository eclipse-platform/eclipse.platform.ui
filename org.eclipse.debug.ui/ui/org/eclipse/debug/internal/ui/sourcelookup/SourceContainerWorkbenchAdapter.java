/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.sourcelookup;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.sourcelookup.containers.ArchiveSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.DirectorySourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.ExternalArchiveSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.FolderSourceContainer;
import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * Workbench adapter for standard source containers.
 * 
 * @since 3.0
 */
public class SourceContainerWorkbenchAdapter implements IWorkbenchAdapter {
	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object o) {
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
	 */
	public String getLabel(Object o) {
		if (o instanceof DirectorySourceContainer) {
			DirectorySourceContainer container = (DirectorySourceContainer) o;
			File file = container.getDirectory();
			IPath path = new Path(file.getAbsolutePath());
			return SourceElementWorkbenchAdapter.getQualifiedName(path);
		}
		if (o instanceof FolderSourceContainer) {
			FolderSourceContainer container = (FolderSourceContainer) o;
			return SourceElementWorkbenchAdapter.getQualifiedName(container.getContainer().getFullPath());
		}
		if (o instanceof ArchiveSourceContainer) {
			ArchiveSourceContainer container = (ArchiveSourceContainer)o;
			return SourceElementWorkbenchAdapter.getQualifiedName(container.getFile().getFullPath());
		}		
		if (o instanceof ExternalArchiveSourceContainer) {
			ExternalArchiveSourceContainer container = (ExternalArchiveSourceContainer)o;
			IPath path = new Path(container.getName());
			return SourceElementWorkbenchAdapter.getQualifiedName(path);
		}		
		return IInternalDebugCoreConstants.EMPTY_STRING;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
	 */
	public Object getParent(Object o) {
		return null;
	}
}
