/*******************************************************************************
 * Copyright (c) 2008, 2017 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 237718)
 *     Matthew Hall - bugs 246626, 255734, 264925
 *     Boris Bokowski, IBM Corporation - bug 257112
 ******************************************************************************/

package org.eclipse.core.databinding.observable;

import java.util.Objects;

/**
 * An observable which decorates another observable
 *
 * @since 1.2
 *
 */
public class DecoratingObservable extends AbstractObservable implements
		IDecoratingObservable {

	private IObservable decorated;

	private IStaleListener staleListener;

	private boolean disposedDecoratedOnDispose;

	/**
	 * Constructs a DecoratingObservable which decorates the given observable.
	 *
	 * @param decorated
	 *            the observable being decorated.
	 * @param disposeDecoratedOnDispose
	 *            whether the decorated observable should be disposed when the
	 *            decorator is disposed
	 */
	public DecoratingObservable(IObservable decorated,
			boolean disposeDecoratedOnDispose) {
		super(decorated.getRealm());
		this.decorated = decorated;
		this.disposedDecoratedOnDispose = disposeDecoratedOnDispose;
		decorated.addDisposeListener(staleEvent -> dispose());
	}

	@Override
	public IObservable getDecorated() {
		return decorated;
	}

	@Override
	public boolean isStale() {
		getterCalled();
		return decorated.isStale();
	}

	protected void getterCalled() {
		ObservableTracker.getterCalled(this);
	}

	@Override
	protected void firstListenerAdded() {
		if (staleListener == null) {
			staleListener = staleEvent -> DecoratingObservable.this.handleStaleEvent(staleEvent);
		}
		decorated.addStaleListener(staleListener);
	}

	@Override
	protected void lastListenerRemoved() {
		if (staleListener != null) {
			decorated.removeStaleListener(staleListener);
			staleListener = null;
		}
	}

	/**
	 * Called whenever a StaleEvent is received from the decorated observable.
	 * By default, this method fires the stale event again, with the decorating
	 * observable as the event source. Subclasses may override to provide
	 * different behavior.
	 *
	 * @param event
	 *            the stale event received from the decorated observable
	 */
	protected void handleStaleEvent(StaleEvent event) {
		fireStale();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null)
			return false;
		if (getClass() == obj.getClass()) {
			DecoratingObservable other = (DecoratingObservable) obj;
			return Objects.equals(this.decorated, other.decorated);
		}
		return Objects.equals(decorated, obj);
	}

	@Override
	public int hashCode() {
		return decorated.hashCode();
	}

	@Override
	public synchronized void dispose() {
		if (decorated != null && staleListener != null) {
			decorated.removeStaleListener(staleListener);
		}
		if (decorated != null) {
			if (disposedDecoratedOnDispose)
				decorated.dispose();
			decorated = null;
		}
		staleListener = null;
		super.dispose();
	}
}
