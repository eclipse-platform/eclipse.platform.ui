package org.eclipse.ui.externaltools.model;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

/**
 * Defines the changes within the external tool storage that
 * a listeners can be notified of.
 * <p>
 * This interface is not to be extended by clients, but can be 
 * implemented by clients.
 * </p>
 */
public interface IStorageListener {
	/**
	 * Notifies this listener that an external tool has
	 * been removed from the registry and storage.
	 * 
	 * @param tool the external tool that was removed
	 */
	public void toolDeleted(ExternalTool tool);
	
	/**
	 * Notifies this listener that a new external tool has
	 * been added to the registry and storage.
	 * 
	 * @param tool the external tool that was created
	 */
	public void toolCreated(ExternalTool tool);

	/**
	 * Notifies this listener that an existing external tool
	 * in the registry has been modified and saved to storage.
	 * 
	 * @param tool the external tool that was modified
	 */
	public void toolModified(ExternalTool tool);
	
	/**
	 * Notifies this listener that the entire external tool
	 * registry was refreshed from storage.
	 */
	public void toolsRefreshed();
}
