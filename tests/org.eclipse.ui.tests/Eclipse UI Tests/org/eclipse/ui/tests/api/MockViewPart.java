/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;

public class MockViewPart extends MockWorkbenchPart implements IViewPart {	
	public static String ID = "org.eclipse.ui.tests.api.MockViewPart";
	public static String ID2 = ID + "2";
	public static String ID3 = ID + "3";
	public static String ID4 = ID + "4";
	public static String IDMULT = ID + "Mult";
	public static String NAME = "Mock View 1";

	public MockViewPart()
	{
		super();
	}
	
	/**
	 * @see IViewPart#getViewSite()
	 */
	public IViewSite getViewSite() {
		return (IViewSite)getSite();
	}

	/**
	 * @see IViewPart#init(IViewSite)
	 */
	public void init(IViewSite site) throws PartInitException {
		setSite(site);
		callTrace.add("init" );
		setSiteInitialized();
	}

	/**
	 * @see IViewPart#init(IViewSite, IMemento)
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		setSite(site);
		callTrace.add("init" );
		setSiteInitialized();
	}

	/**
	 * @see IViewPart#saveState(IMemento)
	 */
	public void saveState(IMemento memento) {
	}

    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.api.MockWorkbenchPart#getActionBars()
     */
    protected IActionBars getActionBars() {
        return getViewSite().getActionBars();
    }	
}

