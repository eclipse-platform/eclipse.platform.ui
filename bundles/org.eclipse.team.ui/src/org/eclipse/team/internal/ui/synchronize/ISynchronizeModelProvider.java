/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.team.core.synchronize.SyncInfoSet;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;

/**
 * This class represents provisional API. A provider is not required to
 * implement this API. Implementers, and those who reference it, do so with the
 * awareness that this class may be removed or substantially changed at future
 * times without warning.
 */
public interface ISynchronizeModelProvider {
	/**
	 * Property constant for the expansion state for the elements displayed by the page. The
	 * expansion state is a List of resource paths.
	 */
	public static final String P_VIEWER_EXPANSION_STATE = TeamUIPlugin.ID  + ".P_VIEWER_EXPANSION_STATE"; //$NON-NLS-1$
	
	/**
	 * Property constant for the selection state for the elements displayed by the page. The
	 * selection state is a List of resource paths.
	 */
	public static final String P_VIEWER_SELECTION_STATE = TeamUIPlugin.ID  + ".P_VIEWER_SELECTION_STATE"; //$NON-NLS-1$
	
	/**
	 * Returns the sync set this model provider is showing.
	 * @return the sync set this model provider is showing.
	 */
	public abstract SyncInfoSet getSyncInfoSet();

	/**
	 * Returns the description for this model provider.
	 * @return the description for this model provider.
	 */
	public ISynchronizeModelProviderDescriptor getDescriptor();
	
	/**
	 * Return the <code>AbstractTreeViewer</code> asociated with this content
	 * provider or <code>null</code> if the viewer is not of the proper type.
	 * @return
	 */
	public abstract StructuredViewer getViewer();

	/**
	 * Installed the viewer to be used to display the model.
	 * @param viewer the viewer in which to diplay the model.
	 */
	public abstract void setViewer(StructuredViewer viewer);

	/**
	 * Builds the viewer model based on the contents of the sync set.
	 * @return the root element of the generated model.
	 */
	public abstract ISynchronizeModelElement prepareInput(IProgressMonitor monitor);

	/**
	 * The provider can try and return a mapping for the provided object. Providers often use mappings
	 * to store the source of a logical element they have created. For example, when displaying resource
	 * based logical elements, a provider will cache the resource -> element mapping for quick retrieval
	 * of the element when resource based changes are made.
	 * 
	 * @param object the object to query for a mapping
	 * @return an object created by this provider that would be shown in a viewer, or <code>null</code>
	 * if the provided object is not mapped by this provider.
	 */
	public abstract Object getMapping(Object object);

	/**
	 * Dispose of the builder
	 */
	public abstract void dispose();

	/**
	 * Returns the input created by this controller or <code>null</code> if 
	 * {@link #prepareInput(IProgressMonitor)} hasn't been called on this object yet.
	 * @return
	 */
	public abstract ISynchronizeModelElement getModelRoot();

	/**
	 * Returns the sorter for this model.
	 * @return the sorter for this model.
	 */
	public abstract ViewerSorter getViewerSorter();

	/**
	 * Allows the provider to save state. Is usually called before provider is disposed and it
	 * is safe to access the viewer.
	 */
	public abstract void saveState();
}
