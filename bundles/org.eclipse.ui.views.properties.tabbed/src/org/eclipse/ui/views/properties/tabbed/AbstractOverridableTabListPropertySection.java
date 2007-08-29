/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.properties.tabbed;

/**
 * An abstract implementation of a section in a tab that overrides the tabs that
 * are provided by the tabbed property registry with a new list of tabs.
 * 
 * @author Anthony Hunter
 * @since 3.4
 */
public class AbstractOverridableTabListPropertySection
	extends AbstractPropertySection
	implements IOverridableTabList {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.IOverridableTabList#getTabs()
	 */
	public ITabItem[] getTabs() {
		return new ITabItem[] {};
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.IOverridableTabList#selectTab(int)
	 */
	public void selectTab(int tab) {
		/* no default implementation */
	}
}
