/*******************************************************************************
 * Copyright (c) 2008, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boris Bokowski - initial API and implementation (bug 218269)
 *     Matthew Hall - bugs 218269, 146906
 ******************************************************************************/

package org.eclipse.core.databinding;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.IStatus;

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
	 * @return an {@link IObservableValue} containing the current validation
	 *         status
	 */
	public abstract IObservableValue<IStatus> getValidationStatus();

	/**
	 * Returns an {@link IObservableList} containing the target observables (if
	 * any) that are being tracked by this validation status provider.
	 *
	 * @return an {@link IObservableList} (may be empty)
	 */
	public abstract IObservableList<IObservable> getTargets();

	/**
	 * Returns an {@link IObservableList} containing the model observables (if
	 * any) that are being tracked by this validation status provider.
	 *
	 * @return an {@link IObservableList} (may be empty)
	 */
	public abstract IObservableList<IObservable> getModels();

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
