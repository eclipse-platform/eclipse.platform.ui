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
package org.eclipse.ui.intro;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;

/**
 * An intro part is a visual component within the workbench.  There may only be
 * one such intro part in the entire workbench, and it may only reside in
 * one window at a time.  This part is used to convey introduction or welcome
 * information to the user and is often launched at startup by the application
 * for first time users.
 * <p>
 * An intro is added to the workbench via the org.eclipse.ui.intro extension 
 * point.  This addition requires two seperate steps.  First, the intro itself
 * is defined and then it is bound to a specific product.  The workbench will
 * only ever utilize an introduction that matches the current product as 
 * described by {@link org.eclipse.core.runtime.Platform#getProduct Platform.getProduct()}.
 * </p>
 * 
 * @see org.eclipse.ui.IWorkbench#showIntro(IWorkbenchWindow, boolean)
 * @see org.eclipse.ui.part.intro.IntroPart
 * @since 3.0
 */
public interface IIntroPart extends IWorkbenchPart {

	/**
	 * Returns the <code>IIntroSite</code> the part was provided during 
	 * initialization.
	 * 
	 * @return the <code>IIntroSite</code>.
	 */
	IIntroSite getIntroSite();

	/**
	 * Primes the part with the <code>IIntroSite</code> object immediately after
	 * creation.
	 * 
	 * @param site the <code>IIntroSite</code>.
	 * @param memento the <code>IIntroPart</code> state or <code>null</code> if 
	 * there is no previous saved state
	 * @throws PartInitException thrown if the <code>IIntroPart</code> was not 
	 * initialized successfully.
	 */
	void init(IIntroSite site, IMemento memento) throws PartInitException;

	/**
	 * Notifies the part that the intro area has been made fullscreen or put on 
	 * standby mode . The <code>IIntroPart</code> should render itself 
	 * differently in the full and standby modes.  
	 * <p>
	 * Clients should not call this method (the workbench calls this method at
	 * appropriate times). To have the workbench change standby mode, use
	 * {@link org.eclipse.ui.IWorkbench#setIntroStandby(IIntroPart, boolean) IWorkbench.setIntroStandby(IIntroPart, boolean)} 
	 * instead.
	 * </p>
	 * @param standby the standby state
	 */
	void standbyStateChanged(boolean standby);

    /**
	 * Saves the object state within a memento.
	 *
	 * @param memento a memento to receive the object state
	 */
	void saveState(IMemento memento);
}
