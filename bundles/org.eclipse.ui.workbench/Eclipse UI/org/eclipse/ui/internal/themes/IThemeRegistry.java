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
package org.eclipse.ui.internal.themes;

/**
 * Registry of theme descriptors.
 *
 * @since 3.0
 */
public interface IThemeRegistry {
	
	/**
	 * Return a lnf descriptor with the given extension ID.  If no lnf exists
	 * with the ID return null.
	 */
	public IThemeDescriptor find(String id);

	/**
	 * Return a list of themes defined in the registry.
	 */
	public IThemeDescriptor [] getThemes();

}
