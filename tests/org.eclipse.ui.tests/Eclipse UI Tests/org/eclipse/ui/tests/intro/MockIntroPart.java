/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.intro;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.tests.api.MockWorkbenchPart;


/**
 * @since 3.0
 */
public class MockIntroPart extends MockWorkbenchPart implements IIntroPart {

    /**
     * 
     */
    public MockIntroPart() {
        super();
    }

	
	public IIntroSite getIntroSite() {
		return (IIntroSite)getSite();
	}

	public void init(IIntroSite site, IMemento memento) throws PartInitException {
		setSite(site);
		callTrace.add("init" );
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
}
