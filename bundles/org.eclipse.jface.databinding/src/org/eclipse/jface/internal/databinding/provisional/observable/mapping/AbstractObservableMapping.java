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
public abstract class AbstractObservableMapping extends BaseObservableMapping
		implements IObservableMapping {

	final public Object getMappingValue(Object element) {
		ObservableTracker.getterCalled(this);
		return doGetMappingValue(element);
	}
	
	public void setMappingValue(Object element, Object value) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return the value of this mapping for the given element
	 */
	abstract protected Object doGetMappingValue(Object element);

	public boolean isStale() {
		return false;
	}
	
	public void dispose() {
		mappingChangeListeners = null;
		super.dispose();
	}
}
