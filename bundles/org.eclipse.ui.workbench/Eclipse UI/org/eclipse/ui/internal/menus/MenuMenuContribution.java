/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.menus;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

/**
 * @since 3.3
 *
 */
public class MenuMenuContribution extends MenuManager {

	protected IConfigurationElement element;

	private boolean iconDefined = false;
	private Image icon = null;

	public MenuMenuContribution(IConfigurationElement element) {
		super();
		this.element = element;
	}
	
	public String getMnemonic() {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_MNEMONIC);
	}
	
	public String getLabel() {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_LABEL);
	}
	
	public String getTooltip() {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_TOOLTIP);
	}
	
	public Image getIcon() {
		// Stall loading the icon until first access
		if (!iconDefined) {
			icon = loadIcon(getIconPath());			
			iconDefined = true;
		}
		return icon;
	}
	
	private String getIconPath() {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_ICON);
	}

	/**
	 * @param iconPath
	 * @return
	 */
	private Image loadIcon(String iconPath) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String toString() {
		return getClass().getName() + "(" + getLabel() + ":" + getTooltip() + ") " + getIconPath();   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.MenuManager#getMenuText()
	 */
	public String getMenuText() {
		return getLabel();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.MenuManager#isVisible()
	 */
	public boolean isVisible() {
		return true;
	}

/* (non-Javadoc)
 * @see org.eclipse.jface.action.MenuManager#isEnabled()
 */
public boolean isEnabled() {
	return true;
}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.MenuManager#fill(org.eclipse.swt.widgets.Menu, int)
	 */
	public void fill(Menu parent, int index) {
		super.fill(parent, index);
	}
}
