/*******************************************************************************
 * Copyright (c) 2007 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.databinding.conformance.delegate;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;

/**
 * Abstract implementation of {@link IObservableValueContractDelegate}.
 * 
 * @since 1.1
 */
public abstract class AbstractObservableValueContractDelegate extends
		AbstractObservableContractDelegate implements
		IObservableValueContractDelegate {

	/**
	 * Invokes {@link #createObservableValue(Realm)}.
	 * @param realm 
	 * @return observable
	 */
	public final IObservable createObservable(Realm realm) {
		return createObservableValue(realm);
	}

	/**
	 * Default implementation returns <code>null</code>.
	 * @param observable 
	 * @return value type
	 */
	public Object getValueType(IObservableValue observable) {
		// no op
		return null;
	}
	
	/**
	 * Default implementation returns <code>null</code>.
	 * @param observable 
	 * @return value
	 */
	public Object createValue(IObservableValue observable) {
		//no op
		return null;
	}
}
