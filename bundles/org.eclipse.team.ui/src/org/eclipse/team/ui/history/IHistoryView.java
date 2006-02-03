/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui.history;

import org.eclipse.team.core.history.IFileHistory;

/**
 * This interface provides a way for clients to request the History View
 * to display the history for a given object.
 *  
 * <p>
 * This interface is not intended to be implemented by clients.
 * 
 * @since 3.2
 * @see IFileHistory
 * @see IHistoryPage
 */
public interface IHistoryView {

	/**
	 * Returns the history page that is currently being displayed by the
	 * history view.
	 * TODO: Need to explain what page this is. Is it the visible page?
	 * @return
	 */
	public IHistoryPage getHistoryPage();
	
	/**
	 * Shows the history for the passed in object.
	 * @param object
	 */
	public void showHistoryFor(Object object);
	
}
