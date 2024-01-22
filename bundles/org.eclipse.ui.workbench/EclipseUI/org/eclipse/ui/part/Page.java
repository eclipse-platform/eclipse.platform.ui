/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.part;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;

/**
 * Abstract base superclass for pages in a pagebook view.
 * <p>
 * This class should be subclassed by clients wishing to define new types of
 * pages for multi-page views.
 * </p>
 * <p>
 * Subclasses must implement the following methods:
 * </p>
 * <ul>
 * <li><code>createControl</code> - to create the page's control</li>
 * <li><code>getControl</code> - to retrieve the page's control</li>
 * </ul>
 * <p>
 * Subclasses may extend or reimplement the following methods as required:
 * </p>
 * <ul>
 * <li><code>dispose</code> - extend to provide additional cleanup</li>
 * <li><code>setFocus</code> - reimplement to accept focus</li>
 * <li><code>setActionBars</code> - reimplement to make contributions</li>
 * <li><code>makeContributions</code> - this method exists to support previous
 * versions</li>
 * <li><code>setActionBars</code> - this method exists to support previous
 * versions</li>
 * <li><code>init</code> - extend to provide additional setup</li>
 * </ul>
 *
 * @see PageBookView
 */
public abstract class Page implements IPageBookViewPage {
	/**
	 * The site which contains this page
	 */
	private IPageSite site;

	/*
	 * Creates a new page for a pagebook view.
	 */
	protected Page() {
	}

	@Override
	public abstract void createControl(Composite parent);

	/**
	 * The <code>Page</code> implementation of this <code>IPage</code> method
	 * disposes of this page's control (if it has one and it has not already been
	 * disposed). Subclasses may extend.
	 */
	@Override
	public void dispose() {
		Control ctrl = getControl();
		if (ctrl != null && !ctrl.isDisposed()) {
			ctrl.dispose();
		}
		site = null;
	}

	/**
	 * The <code>Page</code> implementation of this <code>IPage</code> method
	 * returns <code>null</code>. Subclasses must reimplement.
	 */
	@Override
	public abstract Control getControl();

	/**
	 * This method exists for backward compatibility. Subclasses should reimplement
	 * <code>init</code>.
	 */
	public void makeContributions(IMenuManager menuManager, IToolBarManager toolBarManager,
			IStatusLineManager statusLineManager) {
	}

	/**
	 * This method exists for backward compatibility. Subclasses should reimplement
	 * <code>init</code>.
	 */
	@Override
	public void setActionBars(IActionBars actionBars) {
		makeContributions(actionBars.getMenuManager(), actionBars.getToolBarManager(),
				actionBars.getStatusLineManager());
	}

	/**
	 * The <code>Page</code> implementation of this <code>IPageBookViewPage</code>
	 * method stores a reference to the supplied site (the site which contains this
	 * page).
	 * <p>
	 * Subclasses may extend.
	 * </p>
	 *
	 * @since 2.0
	 */
	@Override
	public void init(IPageSite pageSite) {
		site = pageSite;
	}

	/**
	 * Returns the site which contains this page.
	 *
	 * @return the site which contains this page
	 */
	@Override
	public IPageSite getSite() {
		return site;
	}

	/**
	 * The <code>Page</code> implementation of this <code>IPage</code> method does
	 * nothing. Subclasses must implement.
	 */
	@Override
	public abstract void setFocus();
}
