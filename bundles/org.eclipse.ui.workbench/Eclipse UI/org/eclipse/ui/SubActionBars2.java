/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.SubCoolBarManager;
import org.eclipse.ui.services.IServiceLocator;

/**
 * A implementation of the extended <code>IActionBars2</code> interface. This
 * sub class provides a sub cool bar manager for plugins to contribute multiple
 * cool items.
 * 
 * @since 3.0
 */
public class SubActionBars2 extends SubActionBars implements IActionBars2 {
	private SubCoolBarManager coolBarMgr = null;

	/**
	 * Constucts a sub action bars object using an IActionBars2 parent.
	 * 
	 * @param parent
	 *            the action bars to virtualize; must not be <code>null</code>.
	 */
	public SubActionBars2(final IActionBars2 parent) {
		this(parent, parent.getServiceLocator());
	}

	/**
	 * Constucts a sub action bars object using an IActionBars2 parent.
	 * 
	 * @param parent
	 *            the action bars to virtualize; must not be <code>null</code>.
	 * @param serviceLocator
	 *            The service locator for this action bar; must not be
	 *            <code>null</code>.
	 *            
	 * @since 3.2
	 */
	public SubActionBars2(final IActionBars2 parent,
			final IServiceLocator serviceLocator) {
		super(parent, serviceLocator);
	}

	/**
	 * Returns the casted parent of the sub action bars. This method can return
	 * an IActionBars2 since it can only accept IActionBars2 in the constructor.
	 * 
	 * @return the casted parent.
	 */
	protected IActionBars2 getCastedParent() {
		return (IActionBars2) getParent();
	}

	/**
	 * Returns a new sub coolbar manager.
	 * 
	 * @param parent
	 *            the parent coolbar manager
	 * @return the cool bar manager
	 */
	protected SubCoolBarManager createSubCoolBarManager(ICoolBarManager parent) {
		return new SubCoolBarManager(parent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionBars2#getCoolBarManager()
	 */
	public ICoolBarManager getCoolBarManager() {
		if (coolBarMgr == null) {
			coolBarMgr = createSubCoolBarManager(getCastedParent()
					.getCoolBarManager());
			coolBarMgr.setVisible(getActive());
		}
		return coolBarMgr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.SubActionBars#setActive(boolean)
	 */
	protected void setActive(boolean value) {
		super.setActive(value);
		if (coolBarMgr != null) {
			coolBarMgr.setVisible(value);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.SubActionBars#dispose()
	 */
	public void dispose() {
		super.dispose();
		if (coolBarMgr != null) {
			coolBarMgr.removeAll();
		}
	}
}
