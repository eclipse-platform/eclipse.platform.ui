/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.intro;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.tests.api.MockPart;

/**
 * @since 3.0
 */
public class MockIntroPart extends MockPart implements IIntroPart {

    private IIntroSite site;

    /**
     * 
     */
    public MockIntroPart() {
        super();
    }

    public IIntroSite getIntroSite() {
        return site;
    }

    public void init(IIntroSite site, IMemento memento)
            throws PartInitException {
        setSite(site);
        callTrace.add("init");
    }

    /**
     * @param site
     */
    private void setSite(IIntroSite site) {
        this.site = site;
    }

    /**
     * @see IViewPart#saveState(IMemento)
     */
    public void saveState(IMemento memento) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.intro.IIntroPart#standbyStateChanged(boolean)
     */
    public void standbyStateChanged(boolean standby) {
        callTrace.add("standbyStateChanged");
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.intro.IIntroPart#getTitle()
	 */
	public String getTitle() {
		return "Mock intro title";
	}
}
