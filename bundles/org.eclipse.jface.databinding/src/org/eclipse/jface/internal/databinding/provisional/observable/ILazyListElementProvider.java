/*******************************************************************************
 * Copyright (c) 2006 The Pampered Chef and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     The Pampered Chef - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.provisional.observable;

import org.eclipse.jface.internal.databinding.provisional.observable.list.IObservableList;

/**
 * Interface for Observables that can supply data lazily.
 * 
 * @since 3.3
 */
public interface ILazyListElementProvider extends IObservableList {
	/**
	 * @param position The 0-based position in the receiver's collection
	 * of the object to retrieve.
	 * 
	 * @return The requested object.
	 */
	abstract public Object get(int position);
}
