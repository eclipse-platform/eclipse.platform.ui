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

/**
 * Marker interface for Observables that can supply data lazily.
 * 
 * @since 3.3
 */
public interface ILazyDataSupplier {
	/**
	 * Returns an object that can perform inserts and/or deletes inside the
	 * lazy collection by implementing the LazyInsertDeleteProvider API.
	 * 
	 * @return LazyInsertDeleteProvider the provider or null if none is implemented.
	 */
	LazyInsertDeleteProvider getLazyInsertDeleteProvider();
}
