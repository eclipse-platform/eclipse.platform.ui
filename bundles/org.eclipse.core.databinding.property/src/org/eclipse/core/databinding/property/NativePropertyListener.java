/*******************************************************************************
 * Copyright (c) 2009, 2015 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bugs 265561, 262287)
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 ******************************************************************************/

package org.eclipse.core.databinding.property;

import org.eclipse.core.databinding.observable.IDiff;

/**
 * Abstract INativePropertyListener implementation
 *
 * @param <S>
 *            type of the source object
 * @param <D>
 *            type of the diff handled by this listener
 * @since 1.2
 */
public abstract class NativePropertyListener<S, D extends IDiff> implements INativePropertyListener<S> {
	private final IProperty property;
	private final ISimplePropertyListener<S, D> listener;

	/**
	 * Constructs a NativePropertyListener with the specified arguments
	 *
	 * @param property
	 *            the property that this listener listens to
	 * @param listener
	 *            the listener to receive property change notifications
	 */
	public NativePropertyListener(IProperty property, ISimplePropertyListener<S, D> listener) {
		this.property = property;
		this.listener = listener;
	}

	@Override
	public final void addTo(S source) {
		if (source != null) {
			doAddTo(source);
		}
	}

	protected abstract void doAddTo(S source);

	@Override
	public final void removeFrom(S source) {
		if (source != null) {
			doRemoveFrom(source);
		}
	}

	protected abstract void doRemoveFrom(S source);

	/**
	 * Notifies the listener that a property change occurred on the source object.
	 *
	 * @param source
	 *            the source object whose property changed
	 * @param diff   a diff describing the change in state, or <code>null</code> for
	 *               an auto-generated diff
	 */
	protected void fireChange(S source, D diff) {
		listener.handleEvent(new SimplePropertyEvent<>(
				SimplePropertyEvent.CHANGE, source, property, diff));
	}

	/**
	 * Notifies the listener that the property became stale on the source
	 * object.
	 *
	 * @param source
	 *            the source object whose property became stale
	 */
	protected void fireStale(S source) {
		listener.handleEvent(new SimplePropertyEvent<>(
				SimplePropertyEvent.STALE, source, property, null));
	}
}
