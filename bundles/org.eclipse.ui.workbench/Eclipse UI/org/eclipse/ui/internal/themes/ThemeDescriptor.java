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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * Concrete implementation of a theme descriptor.
 *
 * @since 3.0
 */
public class ThemeDescriptor implements IThemeDescriptor {
	
	private String id;
	private String name;
	
	/* Theme */
	private static final String ATT_ID="id";//$NON-NLS-1$
	private static final String ATT_NAME="name";//$NON-NLS-1$
	private IConfigurationElement configElement;
	/* TabTheme*/
	public static final String TAG_TABTHEME="tabTheme";//$NON-NLS-1$	
	private ITabThemeDescriptor tabThemeDescriptor;
	/* ViewTheme */
	public static final String TAG_VIEWTHEME="viewTheme";//$NON-NLS-1$	
	private IViewThemeDescriptor viewThemeDescriptor;
	
	/**
	 * Create a new ThemeDescriptor for an extension.
	 */
	public ThemeDescriptor(IConfigurationElement e) throws CoreException {
		configElement = e;
		processExtension();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.IThemeDescriptor#getID()
	 */
	public String getID()  {
		return id;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.IThemeDescriptor#getName()
	 */
	public String getName() {
		return name;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.IThemeDescriptor#getTabLookNFeelDesc()
	 */
	public ITabThemeDescriptor getTabThemeDescriptor () {
		return tabThemeDescriptor;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.IThemeDescriptor#getViewThemeDescriptor()
	 */
	public IViewThemeDescriptor getViewThemeDescriptor () {
		return viewThemeDescriptor;
	}
	
	/*
	 * load a theme descriptor from the registry.
	 */
	private void processExtension() throws CoreException {
		id = configElement.getAttribute(ATT_ID);
		name = configElement.getAttribute(ATT_NAME);
		/* process children */
		IConfigurationElement [] children = configElement.getChildren();
		for (int nX = 0; nX < children.length; nX ++) {
			IConfigurationElement child = children[nX];
			String type = child.getName();
			/* TabTheme */
			if (type.equals(TAG_TABTHEME)) {
				tabThemeDescriptor = new TabThemeDescriptor(child);
			}
			/* ViewTheme */
			else if (type.equals(TAG_VIEWTHEME)) {
				viewThemeDescriptor = new ViewThemeDescriptor(child);
			}	
		}
	}	
	
}
