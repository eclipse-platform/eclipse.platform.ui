/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.core.internal.resources;

import org.eclipse.core.filesystem.FileSystemCore;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.internal.localstore.*;
import org.eclipse.core.internal.properties.*;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;

/**
 * This class provides the entry point to the backward compatibility fragment. 
 * 
 * @see ResourcesCompatibilityHelper
 */
public class ResourcesCompatibility {
	/**
	 * Creates a new history store.
	 * 
	 * @param location the base location for the history store 
	 * @param limit the number of buckets in the blob store
	 * @param newImpl whether should use the new implementation
	 * @param convert whether should convert the existing state (ignored if newImpl is false)
	 * @param rename whether should rename the existing index file after converting (ignored if newImpl or convert are false)
	 * @return a history store
	 */
	public static IHistoryStore createHistoryStore(IPath location, int limit, boolean newImpl, boolean convert, boolean rename) {
		Workspace workspace = (Workspace) ResourcesPlugin.getWorkspace();
		if (!newImpl)
			// keep using the old history store
			return new HistoryStore(workspace, location, limit);
		IFileStore store = FileSystemCore.getLocalFileSystem().getStore(location);
		HistoryStore2 newHistoryStore = new HistoryStore2(workspace, store, limit);
		if (!convert)
			// do not try to convert - return as it is
			return newHistoryStore;
		IStatus result = new HistoryStoreConverter().convertHistory(workspace, location, limit, newHistoryStore, rename);
		if (result.getSeverity() != IStatus.OK)
			// if we do anything (either we fail or succeed converting), a non-OK status is returned
			ResourcesPlugin.getPlugin().getLog().log(result);
		return newHistoryStore;
	}
	/**
	 * Creates a new property manager.
	 * 
	 * @param newImpl whether should use the new implementation
	 * @param convert whether should convert the existing state (ignored if newImpl is false)
	 * @return a history store
	 */
	public static IPropertyManager createPropertyManager(boolean newImpl, boolean convert) {
		Workspace workspace = (Workspace) ResourcesPlugin.getWorkspace();		
		if (!newImpl)
			// keep using the old history store
			return new PropertyManager(workspace);
		PropertyManager2 newPropertyManager = new PropertyManager2(workspace);
		if (!convert)
			// do not try to convert - return as it is
			return newPropertyManager;
		// try to convert the existing data now	
		IStatus result = new PropertyStoreConverter().convertProperties(workspace, newPropertyManager);
		if (result.getSeverity() != IStatus.OK)
			// if we do anything (either we fail or succeed converting), a non-OK status is returned
			ResourcesPlugin.getPlugin().getLog().log(result);
		return newPropertyManager;
	}
}
