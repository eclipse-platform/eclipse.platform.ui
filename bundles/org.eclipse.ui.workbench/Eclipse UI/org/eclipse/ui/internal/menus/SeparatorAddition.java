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
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

/**
 * @since 3.3
 *
 */
public class SeparatorAddition extends AdditionBase {

	public SeparatorAddition(IConfigurationElement element) {
		super(element);
	}
	
	public boolean isVisible() {
		String val = element.getAttribute(IWorkbenchRegistryConstants.ATT_VISIBLE);
		return Boolean.valueOf(val).booleanValue();
	}
	
	public String toString() {
		return getClass().getName() + "()";   //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.menus.AdditionBase#getContributionItem()
	 */
	public IContributionItem getContributionItem() {
		return new ContributionItem(getId()) {
			
			/* (non-Javadoc)
			 * @see org.eclipse.ui.internal.menus.AdditionBase#isSeparator()
			 */
			public boolean isSeparator() {
				return true;
			}

			/* (non-Javadoc)
			 * @see org.eclipse.ui.internal.menus.AdditionBase#fill(org.eclipse.swt.widgets.Menu, int)
			 */
			public void fill(Menu parent, int index) {
				if (isVisible()) {
					new MenuItem(parent, SWT.SEPARATOR, index);
				}
			}

			/* (non-Javadoc)
			 * @see org.eclipse.ui.internal.menus.AdditionBase#fill(org.eclipse.swt.widgets.Menu, int)
			 */
			public void fill(ToolBar parent, int index) {
				if (isVisible()) {
					new ToolItem(parent, SWT.SEPARATOR, index);
				}
			}
		};
	}
}
