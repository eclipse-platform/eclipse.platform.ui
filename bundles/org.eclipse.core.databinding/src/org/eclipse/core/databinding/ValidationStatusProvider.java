/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boris Bokowski - initial API and implementation (bug 218269)
 *     Matthew Hall - bug 218269
 ******************************************************************************/

package org.eclipse.core.databinding;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;

/**
 * A validation status provider tracks the state of zero or more target
 * observables and zero or more model observables and produces a validation
 * result.
 * 
 * @since 1.1
 * 
 */
public abstract class ValidationStatusProvider {

	protected boolean disposed = false;

	/**
	 * @return an observable value containing the current validation status
	 */
	public abstract IObservableValue getValidationStatus();

	/**
	 * Returns the list of target observables (if any) that are being tracked by
	 * this validation status provider.
	 * 
	 * @return an observable list of target {@link IObservable}s (may be empty)
	 */
	public abstract IObservableList getTargets();

	/**
	 * Returns the model observables (if any) that are being tracked by this
	 * validation status provider.
	 * 
	 * @return an observable list of model {@link IObservable}s (may be empty)
	 */
	public abstract IObservableList getModels();

	/**
	 * Disposes of this ValidationStatusProvider. Subclasses may extend, but
	 * must call super.dispose().
	 */
	public void dispose() {
		disposed = true;
	}

	/**
	 * @return true if the binding has been disposed. false otherwise.
	 */
	public boolean isDisposed() {
		return disposed;
	}
}
