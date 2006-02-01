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
package org.eclipse.team.ui.mapping;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This interface defines extensions to the {@link ICompareInput}
 * interface that are used for compae inputs that are returned from the 
 * {@link ICompareAdapter#asCompareInput(org.eclipse.team.core.mapping.ISynchronizationContext, Object)}
 * method.
 * <p>
 * Clients may implement this interface.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @see ICompareAdapter#asCompareInput(org.eclipse.team.core.mapping.ISynchronizationContext, Object)
 * 
 * @since 3.2
 */
public interface IModelCompareInput extends ICompareInput {
	
	/**
	 * Return the saveable compare model for this compare input or
	 * <code>null</code> if the input is saved directly to disk when the
	 * compare editor input is changed or the compare editor is closed.
	 * @return the saveable compare model for this compare input
	 */
	ISaveableCompareModel getCompareModel();
	
	/**
	 * Prepare the compare input associated with a model element for display using 
	 * the compare configuration. 
	 * @param configuration the compare configuration for the editor that will display the input
	 * @param monitor a progress monitor
	 */
	void prepareInput(CompareConfiguration configuration, IProgressMonitor monitor) throws CoreException;

}
