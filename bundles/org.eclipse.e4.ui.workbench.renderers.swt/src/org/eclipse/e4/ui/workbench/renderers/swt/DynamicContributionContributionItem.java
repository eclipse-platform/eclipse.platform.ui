/*******************************************************************************
 * Copyright (c) 2013, 2014 MEDEVIT, FHV and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marco Descher <marco@descher.at> - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.renderers.swt;

import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.ui.model.application.ui.menu.MDynamicMenuContribution;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.activitysupport.IActivityManagerProxy;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

/**
 * This item currently serves as a placeholder to determine the correct location
 * of a dynamic menu contribution entry.
 */
class DynamicContributionContributionItem extends ContributionItem {
	private MDynamicMenuContribution model;

	private IMenuListener menuListener = IMenuManager::markDirty;

	private static final String BUNDLE_CLASS_PREFIX = "bundleclass://"; //$NON-NLS-1$

	IActivityManagerProxy activitySupportProxy;

	/**
	 * Create the item and associated model;
	 */
	public DynamicContributionContributionItem(MDynamicMenuContribution item) {
		super(item.getElementId());
		model = item;
		initializeAcivitySupportProxy();
	}

	@Override
	public boolean isDirty() {
		return true;
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

	/**
	 * @return the model element
	 */
	public MDynamicMenuContribution getModel() {
		return model;
	}

	@Override
	public void setParent(IContributionManager parent) {
		if (getParent() instanceof IMenuManager) {
			IMenuManager menuMgr = (IMenuManager) getParent();
			menuMgr.removeMenuListener(menuListener);
		}
		if (parent instanceof IMenuManager) {
			IMenuManager menuMgr = (IMenuManager) parent;
			menuMgr.addMenuListener(menuListener);
		}
		super.setParent(parent);
	}

	@Override
	public boolean isVisible() {
		if (this.activitySupportProxy != null) {
			// Contribution URI has the scheme bundleclass://. Ex:
			// bundleclass://org.eclipse.pde.spy.core/org.eclipse.pde.spy.core.SpyProcessor
			String contributionURI = this.getModel().getContributionURI();
			if (contributionURI.startsWith(BUNDLE_CLASS_PREFIX)) {
				return activitySupportProxy
						.isIdentifierEnabled(contributionURI.substring(BUNDLE_CLASS_PREFIX.length()));
			}
		}
		return true;
	}

	/**
	 * Initialize the Activity Support proxy from Platform Context
	 */
	private void initializeAcivitySupportProxy() {
		BundleContext context = FrameworkUtil.getBundle(Platform.class).getBundleContext();
		if (context != null) {
			ServiceTracker<IActivityManagerProxy, IActivityManagerProxy> tracker = new ServiceTracker<>(context,
					IActivityManagerProxy.class, null);
			if (tracker != null) {
				tracker.open();
				this.activitySupportProxy = tracker.getService();
				tracker.close();
			}
		}
	}
}
