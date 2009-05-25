/*******************************************************************************
 * Copyright (c) 2008, 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 237718)
 *     Matthew Hall - bugs 246626, 255734, 264925
 *     Boris Bokowski, IBM Corporation - bug 257112
 ******************************************************************************/

package org.eclipse.core.databinding.observable;

import org.eclipse.core.internal.databinding.observable.Util;

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
		decorated.addDisposeListener(new IDisposeListener() {
			public void handleDispose(DisposeEvent staleEvent) {
				dispose();
			}
		});
	}

	public IObservable getDecorated() {
		return decorated;
	}

	public boolean isStale() {
		getterCalled();
		return decorated.isStale();
	}

	protected void getterCalled() {
		ObservableTracker.getterCalled(this);
	}

	protected void firstListenerAdded() {
		if (staleListener == null) {
			staleListener = new IStaleListener() {
				public void handleStale(StaleEvent staleEvent) {
					DecoratingObservable.this.handleStaleEvent(staleEvent);
				}
			};
		}
		decorated.addStaleListener(staleListener);
	}

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

	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null)
			return false;
		if (getClass() == obj.getClass()) {
			DecoratingObservable other = (DecoratingObservable) obj;
			return Util.equals(this.decorated, other.decorated);
		}
		return Util.equals(decorated, obj);
	}

	public int hashCode() {
		return decorated.hashCode();
	}

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
