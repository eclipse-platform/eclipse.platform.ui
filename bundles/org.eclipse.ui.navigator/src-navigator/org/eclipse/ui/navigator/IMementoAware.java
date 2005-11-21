/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator;

import org.eclipse.ui.IMemento;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * @since 3.2 
 *
 */
public interface IMementoAware {

	/**
	 * <p>
	 * Restore the previous state of any actions using the flags in aMemento. This method allows the
	 * state of any actions that persist from session to session to be restored.
	 * </p> 
	 * 
	 * @param aMemento
	 *            A memento that was given to the view part to restore its state.
	 */
	public void restoreState(IMemento aMemento);

	/**
	 * <p>
	 * Save flags in aMemento to remember the state of any actions that persist from session to
	 * session.
	 * </p>
	 * <p>
	 * Extensions should qualify any keys stored in the memento with their plugin id
	 * </p>
	 * 
	 * @param aMemento
	 *            A memento that was given to the view part to save its state.
	 */
	public void saveState(IMemento aMemento);
 
}
