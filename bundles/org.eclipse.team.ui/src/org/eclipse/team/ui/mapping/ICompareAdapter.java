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
package org.eclipse.team.ui.mapping;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.core.mapping.ISynchronizationContext;

/**
 * The compare adapter provides compare support for the model objects
 * associated with a model provider.
 * <p>
 * Clients should not implement this interface but should subclass {@link AbstractCompareAdapter}
 * instead.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @since 3.2
 */
public interface ICompareAdapter {
	
	/**
	 * Prepare the context for use with the compare infrastructure.
	 * This usually involves the calculation of the synchronization states
	 * of the model objects that are in the scope of the context and
	 * caching the calculations with the context for later retrieval.
	 * @param context the synchronization context
	 * @param monitor a progress monitor
	 * @throws CoreException 
	 */
	void prepareContext(ISynchronizationContext context, IProgressMonitor monitor) throws CoreException;
	
	/**
	 * Return a compare input for the given model object. Creation of the input
	 * should be fast. Synchronization information calculations that ae longer
	 * running should be performed up front in the
	 * {@link #prepareContext(ISynchronizationContext, IProgressMonitor)}
	 * method. Clients should call this method once per context before obtaining
	 * any compare inputs from the adapter. A <code>null</code> should be
	 * returned if the model object is in-sync or otherwise cannot be compared.
	 * 
	 * @param context the synchronization context
	 * @param o the model object
	 * @return a compare input or <code>null</code> if the model object is
	 *         in-sync or otherwise cannot be compared.
	 */
	ICompareInput asCompareInput(ISynchronizationContext context, Object o);

	/**
	 * Return a structure viewer for viewing the structure of the given compare input
	 * @param parent the parent composite of the viewer
	 * @param oldViewer the current viewer which canbe returned if it is appropriate for use with the given input
	 * @param input the compare input to be viewed
	 * @param configuration the compare configuration information
	 * @return a viewer for viewing the structure of the given compare input
	 */ 
	Viewer findStructureViewer(Composite parent, Viewer oldViewer, ICompareInput input, CompareConfiguration configuration);

	/**
	 * Return a viewer for comparing the content of the given compare input.
	 * @param parent the parent composite of the viewer
	 * @param oldViewer the current viewer which can be returned if it is appropriate for use with the given input
	 * @param input the compare input to be viewed
	 * @param configuration the compare configuration information
	 * @return a viewer for comparing the content of the given compare input
	 */ 
	Viewer findContentViewer(Composite parent, Viewer oldViewer, ICompareInput input, CompareConfiguration configuration);

}
