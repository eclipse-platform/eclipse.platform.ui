/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.ui;

import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.action.IAction;

/**
 * Allows to specify an <code>ActionGroup</code> factory
 * which will be used by the Search view to create an
 * <code>ActionGroup</code> which is used to build the
 * actions bars and the context menu.
 * <p>
 * Note: Local tool bar contributions are not supported in 2.0.
 * </p>
 * 
 * Clients can implement this interface and pass an
 * instance to the search result view.
 * 
 * @see	org.eclipse.ui.actions.ActionGroup
 * @see	ISearchResultView#searchStarted(IActionGroupFactory, String, String, ImageDescriptor, String, ILabelProvider, IAction, IGroupByKeyComputer, IRunnableWithProgress)
 * @since 	2.0
 * @deprecated Part of the old ('classic') search result view. Since 3.0 clients can create their own search result view pages (see {@link ISearchResultPage}), leaving it up to the page 
 * how to create actions.
 */
public interface IActionGroupFactory {

	/**
	 * Creates an <code>ActionGroup</code> for a Search view.
	 *
	 * @param 	searchView the search result view for which the group is made
	 * @see	org.eclipse.ui.actions.ActionGroup
	 * @return an <code>ActionGroup</code> for a Search view
	 */
	ActionGroup createActionGroup(ISearchResultView searchView);
}
