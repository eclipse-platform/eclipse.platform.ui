/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;
import org.eclipse.ui.services.IServiceLocator;

/**
 * A contribution item which proxies a dynamic tool contribution.
 * <p>
 * It currently supports placement in menus.
 * </p>
 * <p>
 * 
 * @author Prakash G.R.
 * 
 * @since 3.6
 * 
 */
public class DynamicToolBarContributionItem extends WorkbenchWindowControlContribution {

	private final IConfigurationElement dynamicAddition;
	private final IServiceLocator locator;
	private boolean alreadyFailed;
	private WorkbenchWindowControlContribution loadedDynamicContribution;

	/**
	 * Creates a DynamicToolBarContributionItem
	 * 
	 * @param id
	 *            - Id of the menu item
	 * @param locator
	 *            - The Service Locator
	 * @param dynamicAddition
	 *            - The Configuration Element defined in the plugin.xml
	 * 
	 */
	public DynamicToolBarContributionItem(String id, IServiceLocator locator,
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
	 * @see
	 * org.eclipse.jface.action.ContributionItem#fill(org.eclipse.swt.widgets
	 * .CoolBar, int)
	 */
	public void fill(CoolBar parent, int index) {
		IContributionItem contributionItem = getContributionItem();
		if (contributionItem != null)
			contributionItem.fill(parent, index);
	}

	private WorkbenchWindowControlContribution getContributionItem() {
		if (loadedDynamicContribution == null && !alreadyFailed)
			createContributionItem();
		return loadedDynamicContribution;
	}

	private void createContributionItem() {

		loadedDynamicContribution = (WorkbenchWindowControlContribution) Util
				.safeLoadExecutableExtension(dynamicAddition,
						IWorkbenchRegistryConstants.ATT_CLASS,
						WorkbenchWindowControlContribution.class);

		if (loadedDynamicContribution == null) {
			alreadyFailed = true;
			return;
		}

		loadedDynamicContribution.setId(getId());
		loadedDynamicContribution.setParent(getParent());
		loadedDynamicContribution.setWorkbenchWindow(getWorkbenchWindow());
		loadedDynamicContribution.setCurSide(getCurSide());
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.menus.InternalControlContribution#setWorkbenchWindow
	 * (org.eclipse.ui.IWorkbenchWindow)
	 */
	public void setWorkbenchWindow(IWorkbenchWindow wbw) {
		super.setWorkbenchWindow(wbw);
		if (loadedDynamicContribution != null) {
			loadedDynamicContribution.setWorkbenchWindow(wbw);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.menus.InternalControlContribution#setCurSide(int)
	 */
	public void setCurSide(int curSide) {
		super.setCurSide(curSide);
		if (loadedDynamicContribution != null) {
			loadedDynamicContribution.setCurSide(curSide);
		}
	}
	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.jface.action.ControlContribution#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public Control createControl(Composite parent) {
		
		WorkbenchWindowControlContribution contributionItem = getContributionItem();
		if (contributionItem != null)
			return contributionItem.delegateCreateControl(parent);
		return null;
	}

}
