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
package org.eclipse.team.ui.compare;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.ui.synchronize.ISynchronizePage;

/**
 * Interface used to prepare the compare input for display. 
 * It is used by sites that show an {@link ISynchronizePage}
 * to show selected diffs in a compare viewer or editor.
 * <p>
 * This interface will be obtained from compare inputs
 * using the adaptable mechanism. It is used to give compare inputs
 * a chance to perform potentially long running operations before the
 * input is shown (in order to ensure that all necessary data is cached
 * once the viewer is shown). It also gives compare inputs a chance to
 * configure the viewer through the {@link CompareConfiguration}.
 * <p>
 * Clients may implement this interface.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @since 3.2
 */
public interface IPrepareCompareInputAdapter {
	
	/**
	 * Prepare the compare input for display using the compare configuration. 
	 * @param input the compare input to be displayed
	 * @param configuration the compare configuration for the editor that will display the input
	 * @param monitor a progress monitor
	 */
	void prepareInput(ICompareInput input, CompareConfiguration configuration, IProgressMonitor monitor) throws CoreException;

}
