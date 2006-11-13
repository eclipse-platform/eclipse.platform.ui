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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

/**
 * Wrapper for a ConfigurationElement defining a Menu or
 * Toolbar 'item' addition.
 * 
 * @since 3.3
 *
 */
public class MenuItemContribution extends CommonMenuAddition {

	private boolean iconDefined = false;
	private Image icon = null;
	
	public MenuItemContribution(IConfigurationElement element) {
		super(element);
	}
	
	public String getCommandId() {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_COMMAND_ID);
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
			String iconPath = getIconPath();
			System.out.println("Icon path: " + iconPath); //$NON-NLS-1$
			// TODO: Load the image
			icon = loadIcon(iconPath);
			
			iconDefined = true;
		}
		return icon;
	}
	
	public int getStyle() {
		// TODO: Check the command type to determine the 'style'
		// (Push, Check, Radio)
		return SWT.PUSH;
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

	public boolean isVisible() {
		// TODO: evaluate the 'visibleWhen' expression
		return true;
	}
	
	public boolean isEnabled() {
		// TODO: evaluate the 'enabledWhen' expression
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.menus.CommonMenuAddition#fill(org.eclipse.swt.widgets.Menu, int)
	 */
	public void fill(Menu parent, int index) {
		super.fill(parent, index);
		
		MenuItem newItem = new MenuItem(parent, getStyle(), index);
		newItem.setText(getLabel());
		newItem.setImage(getIcon());
		newItem.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// Execute through the command service
			}

			public void widgetSelected(SelectionEvent e) {
				// Execute through the command service
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.menus.CommonMenuAddition#fill(org.eclipse.swt.widgets.Menu, int)
	 */
	public void fill(ToolBar parent, int index) {
		super.fill(parent, index);
		
		ToolItem newItem = new ToolItem(parent, getStyle(), index);
		newItem.setText(getLabel());
		newItem.setImage(getIcon());
		newItem.setToolTipText(getTooltip());
		
		newItem.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// Execute through the command service
			}

			public void widgetSelected(SelectionEvent e) {
				// Execute through the command service
			}
		});
	}
	
	public String toString() {
		return getClass().getName() + "(" + getLabel() + ":" + getTooltip() + ") " + getIconPath();   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
	}
}
