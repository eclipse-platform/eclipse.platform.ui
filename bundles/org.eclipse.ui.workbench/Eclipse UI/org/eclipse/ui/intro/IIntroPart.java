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
 * The intro part is a visual component within the workbench responsible for
 * introducing the product to new users. The intro part is typically shown the
 * first time a product is started up.
 * <p>
 * The intro part implementation is contributed to the workbench via the
 * <code>org.eclipse.ui.intro</code> extension point.  There can be several
 * intro part implementations, and associations between intro part
 * implementations and products. The workbench will only make use of the intro
 * part implementation for the current product (as given by
 * {@link org.eclipse.core.runtime.Platform#getProduct()}. There is at most one
 * intro part instance in the entire workbench, and it resides in exactly one
 * workbench window at a time.
 * </p>
 * <p>
 * This interface in not intended to be directly implemented. Rather, clients
 * providing a intro part implementation should subclass 
 * {@link org.eclipse.ui.part.IntroPart}. 
 * </p>
 * 
 * @see org.eclipse.ui.intro.IIntroManager#showIntro(org.eclipse.ui.IWorkbenchWindow, boolean)
 * @since 3.0
 */
public interface IIntroPart extends IWorkbenchPart {

	/**
	 * Returns the site for this intro part.
	 * 
	 * @return the intro site
	 */
	IIntroSite getIntroSite();

	/**
	 * Initializes this intro part with the given intro site. A memento is
	 * passed to the part which contains a snapshot of the part state from a
	 * previous session. Where possible, the part should try to recreate that
	 * state.
	 * <p>
	 * This method is automatically called by the workbench shortly after
	 * part construction.  It marks the start of the intro's lifecycle. Clients
	 * must not call this method.
	 * </p>
	 *
	 * @param site the intro site
     * @param memento the intro part state or <code>null</code> if there is no previous
     * saved state
	 * @exception PartInitException if this part was not initialized
	 * successfully
	 */
	void init(IIntroSite site, IMemento memento) throws PartInitException;

	/**
	 * Sets the standby state of this intro part. An intro part should render
	 * itself differently in the full and standby modes. In standby mode, the
	 * part should be partially visible to the user but otherwise allow them
	 * to work. In full mode, the part should be fully visible and be the center
	 * of the user's attention. 
	 * <p>
	 * This method is automatically called by the workbench at appropriate
	 * times. Clients must not call this method directly (call
	 * {@link IIntroManager#setIntroStandby(IIntroPart, boolean)} instead.
	 * </p>
	 * 
	 * @param standby <code>true</code> to put this part in its partially
	 * visible standy mode, and <code>false</code> to make it fully visible
	 */
	void standbyStateChanged(boolean standby);

    /**
	 * Saves the object state within a memento.
	 * <p>
	 * This method is automatically called by the workbench at appropriate
	 * times. Clients must not call this method directly.
	 * </p>
	 *
	 * @param memento a memento to receive the object state
	 */
	void saveState(IMemento memento);
}
