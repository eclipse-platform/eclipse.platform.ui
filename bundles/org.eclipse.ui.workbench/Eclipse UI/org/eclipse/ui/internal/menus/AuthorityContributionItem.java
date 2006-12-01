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

import org.eclipse.jface.action.ContributionItem;

/**
 * @since 3.3
 * 
 */
public class AuthorityContributionItem extends ContributionItem {

	private IMenuActivation menuActivation;
	private boolean dirty = false;

	/**
	 * @param id
	 * @param visibleWhen
	 * @param auth
	 */
	public AuthorityContributionItem(String id) {
		super(id);
	}

	/**
	 * The activation relavent to this contribution item.
	 * 
	 * @param act
	 */
	public void setActivation(IMenuActivation act) {
		menuActivation = act;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.ContributionItem#dispose()
	 */
	public void dispose() {
		if (menuActivation != null) {
			menuActivation.dispose();
			menuActivation = null;
		}
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.ContributionItem#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		if (visible != isVisible()) {
			setDirty(true);
			super.setVisible(visible);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.ContributionItem#isDirty()
	 */
	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean d) {
		dirty = d;
	}
}
