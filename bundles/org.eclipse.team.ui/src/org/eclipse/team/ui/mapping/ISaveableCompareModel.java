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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.*;

/**
 * A saveable compare model is used to buffer changes made when comparing
 * or merging a model. A compare model can be shared between multiple
 * typed elements within a comparison. The compare model is used by the comparison
 * container in order to determine when a save is required.
 * <p>
 * This interface is not intended to be implemented by clients. Clients
 * can instead subclass {@link SaveableCompareModel}
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @since 3.2
 */
public interface ISaveableCompareModel extends ISaveableModel {

    /**
     * The property id for <code>isDirty</code>.
     */
    public static final int PROP_DIRTY = IWorkbenchPartConstants.PROP_DIRTY;

	/**
	 * Revert any changes in the buffer back to the last saved state.
	 * @param monitor a progress monitor on <code>null</code>
	 * if progress feedback is not required
	 */
	void doRevert(IProgressMonitor monitor);
	
	/**
	 * Add a property change listener. Adding a listener
	 * that is allready registered has no effect.
	 * @param listener the listener
	 */
	void addPropertyListener(IPropertyListener listener); 

	/**
	 * Remove a property change listener. Removing a listener
	 * that is not registered has no effect.
	 * @param listener the listener
	 */
	void removePropertyListener(IPropertyListener listener); 
}
