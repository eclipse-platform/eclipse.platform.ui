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
package org.eclipse.ui.part.intro;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.part.WorkbenchPart;

/**
 * Abstract base implementation of an intro part.
 * <p>
 * Subclasses must implement the following methods:
 * <ul>
 *   <li><code>createPartControl</code> - to create the intro controls </li>
 *   <li><code>setFocus</code> - to accept focus</li>
 * 	 <li><code>setStandbyStateChanged</code> - to change the standby mode</li>
 * </ul>
 * </p>
 * <p>
 * Subclasses may extend or reimplement the following methods as required:
 * <ul>
 *   <li><code>setInitializationData</code> - extend to provide additional 
 *       initialization when the intro extension is instantiated</li>
 *   <li><code>init(IIntroSite, IMemento)</code> - extend to provide additional
 *       initialization when intro is assigned its site</li>
 *   <li><code>dispose</code> - extend to provide additional cleanup</li>
 *   <li><code>getAdapter</code> - reimplement to make their intro adaptable</li>
 * </ul>
 * </p>
 * 
 * @since 3.0
 */
public abstract class IntroPart extends WorkbenchPart implements IIntroPart {


	/* (non-Javadoc)
	 * @see org.eclipse.ui.intro.IIntroPart#getIntroSite()
	 */
	public IIntroSite getIntroSite() {
		return (IIntroSite) getSite();
	}
		
	/* (non-Javadoc)
	 * Initializes this intro with the given intro site.  A memento is passed to
	 * the intro which contains a snapshot of the intro state from a previous
	 * session.  Where possible, the intro should try to recreate that state
	 * within the part controls.
	 * <p>
	 * This implementation will ignore the memento and initialize the intro in
	 * a fresh state.  Subclasses may override the implementation to perform any
	 * state restoration as needed.
	 */
	public void init(IIntroSite site,IMemento memento) throws PartInitException {
		setSite(site);
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.intro.IIntroPart#saveState(org.eclipse.ui.IMemento)
	 */
	public void saveState(IMemento memento){
	    //no-op
	}	
}
