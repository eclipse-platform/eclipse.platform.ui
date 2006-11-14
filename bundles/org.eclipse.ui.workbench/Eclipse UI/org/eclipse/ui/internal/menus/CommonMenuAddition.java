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
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

/**
 * Base class for all new-style menu additions
 * 
 * @since 3.3
 *
 */
public class CommonMenuAddition implements IContributionItem {

	protected IConfigurationElement element;

	public CommonMenuAddition(IConfigurationElement element) {
		this.element = element;
	}
	
	public String getId() {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionItem#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionItem#fill(org.eclipse.swt.widgets.Composite)
	 */
	public void fill(Composite parent) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionItem#fill(org.eclipse.swt.widgets.Menu, int)
	 */
	public void fill(Menu parent, int index) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionItem#fill(org.eclipse.swt.widgets.ToolBar, int)
	 */
	public void fill(ToolBar parent, int index) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionItem#fill(org.eclipse.swt.widgets.CoolBar, int)
	 */
	public void fill(CoolBar parent, int index) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionItem#isDirty()
	 */
	public boolean isDirty() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionItem#isDynamic()
	 */
	public boolean isDynamic() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionItem#isEnabled()
	 */
	public boolean isEnabled() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionItem#isGroupMarker()
	 */
	public boolean isGroupMarker() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionItem#isSeparator()
	 */
	public boolean isSeparator() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionItem#isVisible()
	 */
	public boolean isVisible() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionItem#saveWidgetState()
	 */
	public void saveWidgetState() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionItem#setParent(org.eclipse.jface.action.IContributionManager)
	 */
	public void setParent(IContributionManager parent) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionItem#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionItem#update()
	 */
	public void update() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionItem#update(java.lang.String)
	 */
	public void update(String id) {
	}
}
