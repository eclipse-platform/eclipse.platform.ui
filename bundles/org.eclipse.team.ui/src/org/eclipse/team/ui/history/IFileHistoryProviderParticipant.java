/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.ui.history;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.team.core.history.IFileHistoryProvider;
import org.eclipse.ui.part.Page;

/**
 *  IFileHistoryProviderParticipant is the visual component of an 
 *  {@link IFileHistoryProvider}. It is used by the HistoryView to associate
 *  an IFileHistoryProvider with its corresponding Page. Clients should provide an 
 *  implementation for all IFileHistory types they wish to display in the history
 *  view.
 *  
 *  An IFileHistoryProviderParticipant is associated with its IFileHistoryProvider through
 *  the {@link IAdaptable} mechanism. 
 *  
 * @see IFileHistoryProvider
 * @since 3.2
 */
public interface IFileHistoryProviderParticipant {
	/**
	 * Called by the HistoryView to create the page for this IFileHistoryProvider. The
	 * page must implement {@link IHistoryPage}.
	 * 
	 * @see IHistoryPage
	 * @return a Page that implements IHistoryPage
	 */
	public Page createPage();
}
