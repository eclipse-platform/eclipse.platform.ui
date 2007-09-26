/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.databinding.conformance.delegate;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.Realm;

/**
 * Abstract implementation of {@link IObservableCollectionContractDelegate}.
 * 
 * @since 3.2
 */
public abstract class AbstractObservableCollectionContractDelegate extends
		AbstractObservableContractDelegate implements
		IObservableCollectionContractDelegate {

	/**
	 * Invokes {@link IObservableCollectionContractDelegate#createObservableCollection(Realm, int)}.
	 * @param realm 
	 * @return observable
	 */
	public final IObservable createObservable(Realm realm) {
		return createObservableCollection(realm, 0);
	}
	
	public Object createElement(IObservableCollection collection) {
		//no op
		return null;
	}

	public Object getElementType(IObservableCollection collection) {
		//no op
		return null;
	}
}
