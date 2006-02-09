/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.navigator;

/**
 * 
 * Performs calculations that are necessary to determine the correct children to
 * render in the viewer.
 * 
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @see INavigatorContentService#getPipelineService()
 * @see PipelinedShapeModification
 * @see PipelinedViewerUpdate
 * @see IPipelinedTreeContentProvider
 * @since 3.2
 */
public interface INavigatorPipelineService {
	
	/**
	 * Intercept calls to viewer <code>refresh()</code> methods.
	 * 
	 * <p>
	 * Clients may modify the given update to add or remove the elements to be
	 * refreshed. Clients may return the same instance that was passed in for
	 * the next downstream extension.
	 * </p>
	 * 
	 * <p>
	 * <b>Clients should not call any of the add, remove, refresh, or update
	 * methods on the viewer from this method or any code invoked by the
	 * implementation of this method.</b>
	 * </p>
	 * 
	 * @param aRefreshSynchronization
	 *            The (current) refresh update to execute against the viewer.
	 * @return The (potentially reshaped) refresh to execute against the viewer.
	 */
	boolean interceptRefresh(PipelinedViewerUpdate aRefreshSynchronization);

}
