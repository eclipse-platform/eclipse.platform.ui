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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

/**
 * @since 3.3
 *
 */
public class MenuAddition extends AdditionBase {

	// Cache sub-additions
	private List additions = new ArrayList();
	
	private boolean iconDefined = false;
	private Image icon = null;

	public MenuAddition(IConfigurationElement element) {
		super(element);
	}
	
	public void readAdditions(IConfigurationElement addition, int insertionIndex) {
			IConfigurationElement[] items = addition.getChildren();
			for (int i = 0; i < items.length; i++) {
				String itemType = items[i].getName();

				if (IWorkbenchRegistryConstants.TAG_ITEM.equals(itemType)) {
					additions.add(insertionIndex++, new ItemAddition(items[i]));
				} else if (IWorkbenchRegistryConstants.TAG_WIDGET.equals(itemType)) {
					additions.add(insertionIndex++, new WidgetAddition(items[i]));
				} else if (IWorkbenchRegistryConstants.TAG_MENU.equals(itemType)) {
					MenuAddition newCache = new MenuAddition(items[i]);
					newCache.readAdditions(items[i], 0);
					additions.add(insertionIndex++, newCache);
				} else if (IWorkbenchRegistryConstants.TAG_SEPARATOR.equals(itemType)) {
					additions.add(insertionIndex++, new SeparatorAddition(items[i]));
				}
			}
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
	 * @see org.eclipse.ui.internal.menus.AdditionBase#getContributionItem()
	 */
	public IContributionItem getContributionItem() {
		return new MenuManager(getLabel(), getId()) {

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
		};
	}

	public void populateMenuManager(ContributionManager mgr) {
		for (Iterator additionIter = additions.iterator(); additionIter.hasNext();) {
			Object o = (AdditionBase) additionIter.next();
			AdditionBase addition = (AdditionBase) o;
			IContributionItem ci = addition.getContributionItem();
			
			// Populate the sub-items of menus 
			if (addition instanceof MenuAddition) {
				((MenuAddition)addition).populateMenuManager((MenuManager) ci);
			}
			
			// Add the item to the manager
			mgr.add(ci);
		}
	}
	
	/**
	 * @param additionId The id of the addition to find
	 * @return the index of the given addition
	 */
	public int indexOf(String additionId) {
		return 0;
	}
}
