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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The central manager for Theme descriptors.
 *
 * @since 3.0
 */
public class ThemeRegistry implements IThemeRegistry{

	private List themes;

	/**
	 * Create a new ThemeRegistry.
	 */
	public ThemeRegistry() {
		themes = new ArrayList();
	}

	/**
	 * Add a descriptor to the registry.
	 */
	void add(IThemeDescriptor desc) {
		themes.add(desc);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.IThemeRegistry#find(java.lang.String)
	 * 
	 * @issue lookup is O(N)
	 */
	public IThemeDescriptor find(String id) {
		Iterator enum = themes.iterator();
		while (enum.hasNext()) {
			IThemeDescriptor desc = (IThemeDescriptor) enum.next();
			if (id.equals(desc.getID())) {
				return desc;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.IThemeRegistry#getLookNFeels()
	 */
	public IThemeDescriptor [] getThemes() {
		int nSize = themes.size();
		IThemeDescriptor [] retArray = new IThemeDescriptor[nSize];
		themes.toArray(retArray);
		return retArray;
	}

}
