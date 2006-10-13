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

import org.eclipse.compare.structuremergeviewer.ICompareInput;

/**
 * A change event fired from an {@link ICompareInputChangeNotifier} to any registered
 * {@link ICompareInputChangeListener} instances that are registered with the notifier.
 * <p>
 * This interface is not intended to be implemented by clients.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * @see ICompareInputChangeNotifier
 * @see ICompareInputChangeListener
 * @since 3.3
 */
public interface ICompareInputChangeEvent {

	/**
	 * Return the complete list of changed inputs.
	 * @return the complete list of changed inputs
	 */
	ICompareInput[] getChangedInputs();
	
	/**
	 * Return whether the given compare input has changed. This
	 * method returns <code>true</code> for compare inputs that have become
	 * in-sync (see {@link #isInSync(ICompareInput)}.
	 * @param input the compare input being tested
	 * @return whether the given compare input has changed
	 */
	boolean hasChanged(ICompareInput input);
	
	/**
	 * Return whether the given input is included in the list of
	 * changes for this event and is in-sync;
	 * @param input the compare input being tested
	 * @return whether the given input is included in the list of
	 * changes for this event and is in-sync
	 */
	boolean isInSync(ICompareInput input);

}
