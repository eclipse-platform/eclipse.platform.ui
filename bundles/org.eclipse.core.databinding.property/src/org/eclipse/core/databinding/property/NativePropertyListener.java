/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bugs 265561, 262287)
 ******************************************************************************/

package org.eclipse.core.databinding.property;

import org.eclipse.core.databinding.observable.IDiff;

/**
 * Abstract INativePropertyListener implementation
 * 
 * @since 1.2
 */
public abstract class NativePropertyListener implements INativePropertyListener {
	private final IProperty property;
	private final ISimplePropertyListener listener;

	/**
	 * Constructs a NativePropertyListener with the specified arguments
	 * 
	 * @param property
	 *            the property that this listener listens to
	 * @param listener
	 *            the listener to receive property change notifications
	 */
	public NativePropertyListener(IProperty property,
			ISimplePropertyListener listener) {
		this.property = property;
		this.listener = listener;
	}

	public final void addTo(Object source) {
		if (source != null)
			doAddTo(source);
	}

	protected abstract void doAddTo(Object source);

	public final void removeFrom(Object source) {
		if (source != null)
			doRemoveFrom(source);
	}

	protected abstract void doRemoveFrom(Object source);

	/**
	 * Notifies the listener that a property change occured on the source
	 * object.
	 * 
	 * @param source
	 *            the source object whose property changed
	 * @param diff
	 *            a diff describing the change in state
	 */
	protected void fireChange(Object source, IDiff diff) {
		listener.handleEvent(new SimplePropertyEvent(
				SimplePropertyEvent.CHANGE, source, property, diff));
	}

	/**
	 * Notifies the listener that the property became stale on the source
	 * object.
	 * 
	 * @param source
	 *            the source object whose property became stale
	 */
	protected void fireStale(Object source) {
		listener.handleEvent(new SimplePropertyEvent(SimplePropertyEvent.STALE,
				source, property, null));
	}
}
