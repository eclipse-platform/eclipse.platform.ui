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

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IExportedPreferences;

/**
 * @since 3.0
 */
public class ExportedPreferences extends EclipsePreferences implements IExportedPreferences {

	private boolean isExportRoot = false;

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
	 * @see org.eclipse.core.runtime.preferences.IScope#create(org.eclipse.core.runtime.preferences.IEclipsePreferences, java.lang.String)
	 */
	public IEclipsePreferences create(IEclipsePreferences nodeParent, String nodeName) {
		return new ExportedPreferences(nodeParent, nodeName);
	}
}
