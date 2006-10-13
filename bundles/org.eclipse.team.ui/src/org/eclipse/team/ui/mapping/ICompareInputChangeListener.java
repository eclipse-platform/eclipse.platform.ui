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


/**
 * A listener that gets notified when the sate of a the synchronization compare input
 * changes.
 * <p>
 * This interface may be implemented by clients.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * @see ISynchronizationCompareAdapter#getChangeNotifier(org.eclipse.team.core.mapping.ISynchronizationContext, org.eclipse.compare.structuremergeviewer.ICompareInput)
 * @see ICompareInputChangeNotifier
 * @since 3.3
 */
public interface ICompareInputChangeListener {

	/**
	 * Notification that the state of one or more compare inputs have changed.
	 * @param event the event that describes the changes
	 */
	void compareInputsChanged(ICompareInputChangeEvent event);
	
}
