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
package org.eclipse.ui.part;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.intro.IIntroSite;

/**
 * Abstract base implementation of an intro part.
 * <p>
 * Subclasses must implement the following methods:
 * <ul>
 *   <li><code>createPartControl</code> - to create the intro part's
 *     controls</li>
 *   <li><code>setFocus</code> - to accept focus</li>
 * 	 <li><code>standbyStateChanged</code> - to change the standby mode</li>
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
 * @since 3.0
 */
public abstract class IntroPart extends WorkbenchPart implements IIntroPart {

	/**
	 * Creates a new intro part.
	 */
	protected IntroPart() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.intro.IIntroPart#getIntroSite()
	 */
	public final IIntroSite getIntroSite() {
		return (IIntroSite) getSite();
	}
		
	/**
	 * The base implementation of this {@link IIntroPart} method
	 * ignores the memento and initializes the part in a fresh state. 
	 * Subclasses may extend to perform any state restoration, but must call
	 * the super method.
	 *
	 * @param site the intro site
     * @param memento the intro part state or <code>null</code> if there is no previous
     * saved state
	 * @exception PartInitException if this part was not initialized
	 * successfully
	 */
	public void init(IIntroSite site, IMemento memento) throws PartInitException {
		setSite(site);
	}	
	
	/** 
	 * The base implementation of this {@link IIntroPart} method does nothing.
	 * Subclasses may override.
	 * 
	 * @param memento a memento to receive the object state
	 */
	public void saveState(IMemento memento){
	    //no-op
	}
}
