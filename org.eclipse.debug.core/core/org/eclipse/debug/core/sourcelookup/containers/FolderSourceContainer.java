/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.core.sourcelookup.containers;

import org.eclipse.core.resources.IContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.internal.core.sourcelookup.SourceLookupUtils;
import org.eclipse.debug.internal.core.sourcelookup.containers.FolderSourceContainerType;

/**
 * A folder in the workspace. Source elements are searched
 * for within a folder and its nested folders.
 * 
 * @since 3.0
 */
public class FolderSourceContainer extends ContainerSourceContainer {

	/**
	 * Constructs a source container on the given folder. 
	 * 
	 * @param folder the folder to search for source in
	 * @param subfolders whether to search nested folders
	 */
	public FolderSourceContainer(IContainer folder, boolean subfolders) {
		super(folder, subfolders);
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#getType()
	 */
	public ISourceContainerType getType() {
		return SourceLookupUtils.getSourceContainerType(FolderSourceContainerType.TYPE_ID);
	}
	
}
