/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.preferences;

import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IExportedPreferences;
import org.osgi.service.prefs.BackingStoreException;

/**
 * @since 3.0
 */
public class ExportedPreferences extends EclipsePreferences implements IExportedPreferences {

	private boolean isExportRoot = false;
	private String version;

	ExportedPreferences(IEclipsePreferences parent, String name) {
		super(parent, name);
	}

	/*
	 * @see org.eclipse.core.runtime.preferences.IExportedPreferences#isExportRoot()
	 */
	public boolean isExportRoot() {
		return isExportRoot;
	}

	/*
	 * Internal method called only by the import/export mechanism.
	 */
	public void setExportRoot() {
		isExportRoot = true;
	}

	/*
	 * Internal method called only by the import/export mechanism to
	 * validate bundle versions.
	 */
	public String getVersion() {
		return version;
	}

	/*
	 * Internal method called only by the import/export mechanism to
	 * validate bundle versions.
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/*
	 * @see org.eclipse.core.runtime.preferences.IScope#create(org.eclipse.core.runtime.preferences.IEclipsePreferences, java.lang.String)
	 */
	public IEclipsePreferences create(IEclipsePreferences nodeParent, String nodeName, Plugin context) {
		return addChild(nodeName, new ExportedPreferences(nodeParent, nodeName));
	}

	/*
	 * @see org.osgi.service.prefs.Preferences#remove(java.lang.String)
	 */
	public void remove(String key) {
		super.remove(key);
		if (properties == null)
			try {
				removeNode();
			} catch (BackingStoreException e) {
				String message = Policy.bind("preferences.removeExported", absolutePath()); //$NON-NLS-1$
				IStatus status = new Status(IStatus.ERROR, Platform.PI_RUNTIME, IStatus.ERROR, message, e);
				log(status);
			}
	}

	/*
	 * Return a string representation of this object. To be used for 
	 * debugging purposes only.
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		if (isExportRoot)
			buffer.append("* "); //$NON-NLS-1$
		buffer.append(absolutePath());
		if (version != null)
			buffer.append(" (" + version + ')'); //$NON-NLS-1$
		return buffer.toString();
	}
}