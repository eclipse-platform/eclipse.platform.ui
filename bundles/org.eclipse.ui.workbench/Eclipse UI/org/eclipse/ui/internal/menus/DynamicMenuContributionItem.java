/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
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
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;

/**
 * A contribution item which proxies a dynamic menu or tool contribution.
 * <p>
 * It currently supports placement in menus.
 * </p>
 * <p>
 * 
 * @author Prakash G.R.
 * 
 * @since 3.5
 * 
 */
public class DynamicMenuContributionItem extends ContributionItem {

	private final IConfigurationElement dynamicAddition;
	private final IServiceLocator locator;
	private boolean alreadyFailed;
	private ContributionItem loadedDynamicContribution;

	/**
	 * Creates a DynamicMenuContributionItem
	 * 
	 * @param id
	 *            - Id of the menu item
	 * @param locator
	 *            - The Service Locator
	 * @param dynamicAddition
	 *            - The Configuration Element defined in the plugin.xml
	 * 
	 */
	public DynamicMenuContributionItem(String id, IServiceLocator locator,
			IConfigurationElement dynamicAddition) {
		super(id);

		this.locator = locator;
		this.dynamicAddition = dynamicAddition;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.ContributionItem#isDynamic()
	 */
	public boolean isDynamic() {
		if (loadedDynamicContribution != null) {
			return loadedDynamicContribution.isDynamic();
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.ContributionItem#isDirty()
	 */
	public boolean isDirty() {
		if (loadedDynamicContribution != null) {
			return loadedDynamicContribution.isDirty();
		}
		return super.isDirty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.ContributionItem#isEnabled()
	 */
	public boolean isEnabled() {
		if (loadedDynamicContribution != null) {
			return loadedDynamicContribution.isEnabled();
		}
		return super.isEnabled();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.ContributionItem#isGroupMarker()
	 */
	public boolean isGroupMarker() {
		if (loadedDynamicContribution != null) {
			return loadedDynamicContribution.isGroupMarker();
		}
		return super.isGroupMarker();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.ContributionItem#isSeparator()
	 */
	public boolean isSeparator() {
		if (loadedDynamicContribution != null) {
			return loadedDynamicContribution.isSeparator();
		}
		return super.isSeparator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.ContributionItem#isVisible()
	 */
	public boolean isVisible() {
		if (loadedDynamicContribution != null) {
			return loadedDynamicContribution.isVisible();
		}
		return super.isVisible();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.ContributionItem#saveWidgetState()
	 */
	public void saveWidgetState() {
		if (loadedDynamicContribution != null) {
			loadedDynamicContribution.saveWidgetState();
		}
		super.saveWidgetState();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.ContributionItem#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		if (loadedDynamicContribution != null) {
			loadedDynamicContribution.setVisible(visible);
		}
		super.setVisible(visible);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.action.ContributionItem#fill(org.eclipse.swt.widgets
	 * .Composite)
	 */
	public void fill(Composite parent) {
		IContributionItem contributionItem = getContributionItem();
		if (contributionItem != null)
			contributionItem.fill(parent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.action.ContributionItem#fill(org.eclipse.swt.widgets
	 * .CoolBar, int)
	 */
	public void fill(CoolBar parent, int index) {
		IContributionItem contributionItem = getContributionItem();
		if (contributionItem != null)
			contributionItem.fill(parent, index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.action.ContributionItem#fill(org.eclipse.swt.widgets
	 * .Menu, int)
	 */
	public void fill(Menu menu, int index) {
		IContributionItem contributionItem = getContributionItem();
		if (contributionItem != null)
			contributionItem.fill(menu, index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.action.ContributionItem#fill(org.eclipse.swt.widgets
	 * .ToolBar, int)
	 */
	public void fill(ToolBar parent, int index) {
		IContributionItem contributionItem = getContributionItem();
		if (contributionItem != null)
			contributionItem.fill(parent, index);
	}

	private IContributionItem getContributionItem() {
		if (loadedDynamicContribution == null && !alreadyFailed)
			createContributionItem();
		return loadedDynamicContribution;
	}

	private void createContributionItem() {

		loadedDynamicContribution = (ContributionItem) Util
				.safeLoadExecutableExtension(dynamicAddition,
						IWorkbenchRegistryConstants.ATT_CLASS,
						ContributionItem.class);

		if (loadedDynamicContribution == null) {
			alreadyFailed = true;
			return;
		}

		loadedDynamicContribution.setId(getId());
		loadedDynamicContribution.setParent(getParent());
		if (loadedDynamicContribution instanceof IWorkbenchContribution) {
			((IWorkbenchContribution) loadedDynamicContribution)
					.initialize(locator);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.ContributionItem#dispose()
	 */
	public void dispose() {
		if (loadedDynamicContribution != null) {
			loadedDynamicContribution.dispose();
			loadedDynamicContribution = null;
		}
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.ContributionItem#update()
	 */
	public void update() {
		if (loadedDynamicContribution != null) {
			loadedDynamicContribution.update();
		}
	}

	public void update(String id) {
		if (loadedDynamicContribution != null) {
			loadedDynamicContribution.update(id);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.action.ContributionItem#setParent(org.eclipse.jface
	 * .action.IContributionManager)
	 */
	public void setParent(IContributionManager parent) {
		super.setParent(parent);
		if (loadedDynamicContribution != null) {
			loadedDynamicContribution.setParent(parent);
		}
	}

}
