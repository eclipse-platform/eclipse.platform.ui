/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
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
	@Override
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
	@Override
	public boolean isDirty() {
		if (loadedDynamicContribution != null) {
			return loadedDynamicContribution.isDirty();
		}
		return super.isDirty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.action.ContributionItem#fill(org.eclipse.swt.widgets
	 * .CoolBar, int)
	 */
	@Override
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
	@Override
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
	@Override
	public void update() {
		if (loadedDynamicContribution != null) {
			loadedDynamicContribution.update();
		}
	}

	@Override
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
	@Override
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
	@Override
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
	@Override
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
	@Override
	public Control createControl(Composite parent) {
		
		WorkbenchWindowControlContribution contributionItem = getContributionItem();
		if (contributionItem != null)
			return contributionItem.delegateCreateControl(parent);
		return null;
	}

}
