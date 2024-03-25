/*******************************************************************************
 * Copyright (c) 2008, 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Christoph Läubrich - handle runtime exceptions produced by dynamic contribution
 ******************************************************************************/

package org.eclipse.ui.internal.menus;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ILog;
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
 *
 * @author Prakash G.R.
 *
 * @since 3.5
 */
public class DynamicMenuContributionItem extends ContributionItem {

	private final IConfigurationElement dynamicAddition;
	private final IServiceLocator locator;
	private boolean alreadyFailed;
	private ContributionItem loadedDynamicContribution;

	/**
	 * Creates a DynamicMenuContributionItem
	 *
	 * @param id              - Id of the menu item
	 * @param locator         - The Service Locator
	 * @param dynamicAddition - The Configuration Element defined in the plugin.xml
	 */
	public DynamicMenuContributionItem(String id, IServiceLocator locator, IConfigurationElement dynamicAddition) {
		super(id);

		this.locator = locator;
		this.dynamicAddition = dynamicAddition;
	}

	@Override
	public boolean isDynamic() {
		if (loadedDynamicContribution != null) {
			try {
				return loadedDynamicContribution.isDynamic();
			} catch (RuntimeException e) {
				reportErrorForContribution(loadedDynamicContribution, e);
			}
		}
		return true;
	}

	@Override
	public boolean isDirty() {
		if (loadedDynamicContribution != null) {
			try {
				return loadedDynamicContribution.isDirty();
			} catch (RuntimeException e) {
				reportErrorForContribution(loadedDynamicContribution, e);
			}
		}
		return super.isDirty();
	}

	@Override
	public boolean isEnabled() {
		if (loadedDynamicContribution != null) {
			try {
				return loadedDynamicContribution.isEnabled();
			} catch (RuntimeException e) {
				reportErrorForContribution(loadedDynamicContribution, e);
			}
		}
		return super.isEnabled();
	}

	@Override
	public boolean isGroupMarker() {
		if (loadedDynamicContribution != null) {
			try {
				return loadedDynamicContribution.isGroupMarker();
			} catch (RuntimeException e) {
				reportErrorForContribution(loadedDynamicContribution, e);
			}
		}
		return super.isGroupMarker();
	}

	@Override
	public boolean isSeparator() {
		if (loadedDynamicContribution != null) {
			try {
				return loadedDynamicContribution.isSeparator();
			} catch (RuntimeException e) {
				reportErrorForContribution(loadedDynamicContribution, e);
			}
		}
		return super.isSeparator();
	}

	@Override
	public boolean isVisible() {
		if (loadedDynamicContribution != null) {
			try {
				return loadedDynamicContribution.isVisible();
			} catch (RuntimeException e) {
				reportErrorForContribution(loadedDynamicContribution, e);
			}
		}
		return super.isVisible();
	}

	@Override
	public void saveWidgetState() {
		if (loadedDynamicContribution != null) {
			try {
				loadedDynamicContribution.saveWidgetState();
			} catch (RuntimeException e) {
				reportErrorForContribution(loadedDynamicContribution, e);
			}
		}
		super.saveWidgetState();
	}

	@Override
	public void setVisible(boolean visible) {
		if (loadedDynamicContribution != null) {
			try {
				loadedDynamicContribution.setVisible(visible);
			} catch (RuntimeException e) {
				reportErrorForContribution(loadedDynamicContribution, e);
			}
		}
		super.setVisible(visible);
	}

	@Override
	public void fill(Composite parent) {
		IContributionItem contributionItem = getContributionItem();
		if (contributionItem != null) {
			try {
				contributionItem.fill(parent);
			} catch (RuntimeException e) {
				reportErrorForContribution(contributionItem, e);
			}
		}
	}

	@Override
	public void fill(CoolBar parent, int index) {
		IContributionItem contributionItem = getContributionItem();
		if (contributionItem != null){
			try {
				contributionItem.fill(parent, index);
			} catch (RuntimeException e) {
				reportErrorForContribution(contributionItem, e);
			}
		}
	}

	@Override
	public void fill(Menu menu, int index) {
		IContributionItem contributionItem = getContributionItem();
		if (contributionItem != null) {
			try {
				contributionItem.fill(menu, index);
			} catch (RuntimeException e) {
				reportErrorForContribution(contributionItem, e);
			}
		}
	}

	@Override
	public void fill(ToolBar parent, int index) {
		IContributionItem contributionItem = getContributionItem();
		if (contributionItem != null) {
			try {
				contributionItem.fill(parent, index);
			} catch (RuntimeException e) {
				reportErrorForContribution(contributionItem, e);
			}
		}
	}

	private IContributionItem getContributionItem() {
		if (loadedDynamicContribution == null && !alreadyFailed)
			createContributionItem();
		return loadedDynamicContribution;
	}

	private void createContributionItem() {

		loadedDynamicContribution = (ContributionItem) Util.safeLoadExecutableExtension(dynamicAddition,
				IWorkbenchRegistryConstants.ATT_CLASS, ContributionItem.class);

		if (loadedDynamicContribution == null) {
			alreadyFailed = true;
			return;
		}

		loadedDynamicContribution.setId(getId());
		loadedDynamicContribution.setParent(getParent());
		if (loadedDynamicContribution instanceof IWorkbenchContribution) {
			((IWorkbenchContribution) loadedDynamicContribution).initialize(locator);
		}
	}

	@Override
	public void dispose() {
		if (loadedDynamicContribution != null) {
			try {
				loadedDynamicContribution.dispose();
			} catch (RuntimeException e) {
				reportErrorForContribution(loadedDynamicContribution, e);
			}
			loadedDynamicContribution = null;
		}
		super.dispose();
	}

	@Override
	public void update() {
		if (loadedDynamicContribution != null) {
			try {
				loadedDynamicContribution.update();
			} catch (RuntimeException e) {
				reportErrorForContribution(loadedDynamicContribution, e);
			}
		}
	}

	@Override
	public void update(String id) {
		if (loadedDynamicContribution != null) {
			try {
				loadedDynamicContribution.update(id);
			} catch (RuntimeException e) {
				reportErrorForContribution(loadedDynamicContribution, e);
			}
		}
	}

	@Override
	public void setParent(IContributionManager parent) {
		super.setParent(parent);
		if (loadedDynamicContribution != null) {
			try {
				loadedDynamicContribution.setParent(parent);
			} catch (RuntimeException e) {
				reportErrorForContribution(loadedDynamicContribution, e);
			}
		}
	}

	private static void reportErrorForContribution(IContributionItem contributionItem, RuntimeException e) {
		String message = String.format("Dynamic menu contribution '%s' threw an unexpected exception", //$NON-NLS-1$
				contributionItem);
		ILog.get().error(message, e);
	}

}
