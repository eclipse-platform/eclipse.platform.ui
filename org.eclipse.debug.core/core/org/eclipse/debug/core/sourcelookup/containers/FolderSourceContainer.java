/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.core.sourcelookup.containers;

import org.eclipse.core.resources.IContainer;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.internal.core.sourcelookup.containers.*;

/**
 * A folder in the workspace.
 * <p>
 * Clients may instantiate this class. This class is not intended to
 * be subclassed.
 * </p>
 * @since 3.0
 */
public class FolderSourceContainer extends ContainerSourceContainer {

	/**
	 * Unique identifier for the folder source container type
	 * (value <code>org.eclipse.debug.core.containerType.folder</code>).
	 */
	public static final String TYPE_ID = DebugPlugin.getUniqueIdentifier() + ".containerType.folder";	 //$NON-NLS-1$


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
	 * @see org.eclipse.debug.core.sourcelookup.ISourceContainer#getType()
	 */
	public ISourceContainerType getType() {
		return getSourceContainerType(TYPE_ID);
	}
	
}
