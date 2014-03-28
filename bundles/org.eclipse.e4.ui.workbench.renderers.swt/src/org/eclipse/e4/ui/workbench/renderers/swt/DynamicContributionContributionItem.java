/*******************************************************************************
 * Copyright (c) 2013 MEDEVIT, FHV and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Descher <marco@descher.at> - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.renderers.swt;

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

	private IMenuListener menuListener = new IMenuListener() {
		@Override
		public void menuAboutToShow(IMenuManager manager) {
			manager.markDirty();
		}
	};

	/**
	 * Create the item and associated model;
	 * 
	 * @param item
	 */
	public DynamicContributionContributionItem(MDynamicMenuContribution item) {
		super(item.getElementId());
		model = item;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.ContributionItem#isDirty()
	 */
	@Override
	public boolean isDirty() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.ContributionItem#isDynamic()
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.action.ContributionItem#setParent(org.eclipse.jface
	 * .action.IContributionManager)
	 */
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
