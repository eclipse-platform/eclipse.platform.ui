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
package org.eclipse.core.runtime.preferences;

/**
 * Represents a node in the preference hierarchy which is used in
 * the import/export mechanism.
 * 
 * @since 3.0
 */
public interface IExportedPreferences extends IEclipsePreferences {

	/**
	 * Return <code>true</code> if this node was an export root
	 * when the preferences were exported, and <code>false</code>
	 * otherwise.
	 * 
	 * @return <code>true</code> if this node is an export root
	 */
	public boolean isExportRoot();

}
