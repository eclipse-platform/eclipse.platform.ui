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
	 * Return whether their is a compare input associated with the given object.
	 * In otherwords, return <code>true</code> if {@link #asCompareInput(Object) }
	 * would return a value and <code>false</code> if it would return <code>null</code>.
	 * @param context the synchronization context
	 * @param object the object.
	 * @return whether their is a compare input associated with the given object
	 */
	boolean hasCompareInput(ISynchronizationContext context, Object object);
	
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
	 * Prepare the compare input for display using the compare configuration. 
	 * @param input the compare input to be displayed
	 * @param configuration the compare configuration for the editor that will display the input
	 * @param monitor a progress monitor
	 */
	void prepareInput(ICompareInput input, CompareConfiguration configuration, IProgressMonitor monitor);

}
