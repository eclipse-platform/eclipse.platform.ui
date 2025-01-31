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

import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.ui.model.application.ui.menu.MDynamicMenuContribution;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;

/**
 * This item currently serves as a placeholder to determine the correct location
 * of a dynamic menu contribution entry.
 */
class DynamicContributionContributionItem extends ContributionItem {
	private MDynamicMenuContribution model;

	private IMenuListener menuListener = IMenuManager::markDirty;

	private IContributionFactory factory;

	/**
	 * Create the item and associated model;
	 */
	public DynamicContributionContributionItem(MDynamicMenuContribution item, IContributionFactory factory) {
		super(item.getElementId());
		model = item;
		this.factory = factory;
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
	public boolean isVisible() {
		if (factory.isEnabled(model.getContributionURI())) {
			return super.isVisible();
		}
		return false;
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
}
