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
package org.eclipse.update.core;

import org.eclipse.update.core.model.PluginEntryModel;
import org.eclipse.update.internal.core.UpdateCore;

/**
 * Convenience implementation of plug-in entry.
 * <p>
 * This class may be instantiated or subclassed by clients.
 * </p> 
 * @see org.eclipse.update.core.IPluginEntry
 * @see org.eclipse.update.core.model.PluginEntryModel
 * @since 2.0
 */
public class PluginEntry extends PluginEntryModel implements IPluginEntry {
	
	// PERF: new instance variable
	private VersionedIdentifier versionId;

	/**
	 * Plug-in entry default constructor
	 */
	public PluginEntry() {
		super();
	}

	/**
	 * Returns the identifier of this plugin entry
	 * 
	 * @see IPluginEntry#getIdentifier()
	 * @since 2.0
	 */
	public VersionedIdentifier getVersionedIdentifier() {
		if (versionId != null)
			return versionId;

		String id = getPluginIdentifier();
		String ver = getPluginVersion();
		if (id != null && ver != null) {
			try {
				versionId = new VersionedIdentifier(id, ver);
				return versionId;
			} catch (Exception e) {
				UpdateCore.warn("Unable to create versioned identifier:" + id + ":" + ver);
			}
		}

		versionId = new VersionedIdentifier("",null);
		return versionId;
	}

	/**
	 * Sets the identifier of this plugin entry. 
	 * 
	 * @see IPluginEntry#setVersionedIdentifier(VersionedIdentifier)
	 * @since 2.0
	 */
	public void setVersionedIdentifier(VersionedIdentifier identifier) {
		setPluginIdentifier(identifier.getIdentifier());
		setPluginVersion(identifier.getVersion().toString());
	}	

	/**
	 * Compares two plugin entries for equality
	 * 
	 * @param object plugin entry object to compare with
	 * @return <code>true</code> if the two entries are equal, 
	 * <code>false</code> otherwise
	 * @since 2.0
	 */
	public boolean equals(Object object) {
		if (!(object instanceof IPluginEntry))
			return false;
		IPluginEntry e = (IPluginEntry) object;
		return getVersionedIdentifier().equals(e.getVersionedIdentifier());
	}

}
