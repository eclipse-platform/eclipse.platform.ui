/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 237718)
 *     Matthew Hall - bug 246626
 *******************************************************************************/

package org.eclipse.core.databinding.observable.set;

import org.eclipse.core.databinding.observable.DecoratingObservableCollection;

/**
 * An observable set which decorates another observable set.
 * 
 * @since 1.2
 */
public class DecoratingObservableSet extends DecoratingObservableCollection
		implements IObservableSet {

	private IObservableSet decorated;

	private ISetChangeListener setChangeListener;

	/**
	 * Constructs a DecoratingObservableSet which decorates the given
	 * observable.
	 * 
	 * @param decorated
	 *            the observable set being decorated
	 * @param disposeDecoratedOnDispose
	 */
	public DecoratingObservableSet(IObservableSet decorated,
			boolean disposeDecoratedOnDispose) {
		super(decorated, disposeDecoratedOnDispose);
		this.decorated = decorated;
	}

	public void clear() {
		getterCalled();
		decorated.clear();
	}

	public synchronized void addSetChangeListener(ISetChangeListener listener) {
		addListener(SetChangeEvent.TYPE, listener);
	}

	public synchronized void removeSetChangeListener(ISetChangeListener listener) {
		removeListener(SetChangeEvent.TYPE, listener);
	}

	protected void fireSetChange(SetDiff diff) {
		// fire general change event first
		super.fireChange();
		fireEvent(new SetChangeEvent(this, diff));
	}

	protected void fireChange() {
		throw new RuntimeException(
				"fireChange should not be called, use fireSetChange() instead"); //$NON-NLS-1$
	}

	protected void firstListenerAdded() {
		if (setChangeListener == null) {
			setChangeListener = new ISetChangeListener() {
				public void handleSetChange(SetChangeEvent event) {
					DecoratingObservableSet.this.handleSetChange(event);
				}
			};
		}
		decorated.addSetChangeListener(setChangeListener);
		super.firstListenerAdded();
	}

	protected void lastListenerRemoved() {
		super.lastListenerRemoved();
		if (setChangeListener != null) {
			decorated.removeSetChangeListener(setChangeListener);
			setChangeListener = null;
		}
	}

	/**
	 * Called whenever a SetChangeEvent is received from the decorated
	 * observable. By default, this method fires the set change event again,
	 * with the decorating observable as the event source. Subclasses may
	 * override to provide different behavior.
	 * 
	 * @param event
	 *            the change event received from the decorated observable
	 */
	protected void handleSetChange(final SetChangeEvent event) {
		fireSetChange(event.diff);
	}

	public synchronized void dispose() {
		if (decorated != null && setChangeListener != null) {
			decorated.removeSetChangeListener(setChangeListener);
		}
		decorated = null;
		setChangeListener = null;
		super.dispose();
	}
}
