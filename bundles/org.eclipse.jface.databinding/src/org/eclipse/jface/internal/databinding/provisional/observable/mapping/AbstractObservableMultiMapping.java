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

package org.eclipse.jface.internal.databinding.provisional.observable.mapping;


import org.eclipse.jface.databinding.observable.ObservableTracker;

/**
 * @since 1.0
 * 
 */
public abstract class AbstractObservableMultiMapping extends BaseObservableMapping
		implements IObservableMultiMapping {

	final public Object[] getMappingValues(Object element, int[] indices) {
		ObservableTracker.getterCalled(this);
		return doGetMappingValues(element, indices);
	}
	
	public void setMappingValues(Object element, int[] indices, Object[] values) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @param indices 
	 * @return the value of this mapping for the given element
	 */
	abstract protected Object[] doGetMappingValues(Object element, int[] indices);

	public boolean isStale() {
		return false;
	}
	
	public void dispose() {
		mappingChangeListeners = null;
		super.dispose();
	}
}
