/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 237718)
 ******************************************************************************/

package org.eclipse.core.databinding.observable;

/**
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
	 * @param disposedDecoratedOnDispose
	 *            whether the decorated observable should be disposed when the
	 *            decorator is disposed
	 */
	public DecoratingObservable(IObservable decorated,
			boolean disposedDecoratedOnDispose) {
		super(decorated.getRealm());
		this.decorated = decorated;
		this.disposedDecoratedOnDispose = disposedDecoratedOnDispose;
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
					fireStale();
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
