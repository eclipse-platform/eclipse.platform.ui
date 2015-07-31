/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 *     Stefan Xenos <sxenos@gmail.com> - Bug 474065
 ******************************************************************************/

package org.eclipse.core.databinding.observable.set;

import org.eclipse.core.databinding.observable.IObservablesListener;
import org.eclipse.core.databinding.observable.ObservableEvent;

/**
 * List change event describing an incremental change of an
 * {@link IObservableSet} object.
 *
 * @param <E>
 *            the type of elements in the change event
 *
 * @since 1.0
 *
 */
public class SetChangeEvent<E> extends ObservableEvent {

	/**
	 *
	 */
	private static final long serialVersionUID = 7436547103857482256L;
	static final Object TYPE = new Object();

	/**
	 * Description of the change to the source observable set. Listeners must
	 * not change this field.
	 */
	public SetDiff<E> diff;

	/**
	 * Creates a new set change event.
	 *
	 * @param source
	 *            the source observable set
	 * @param diff
	 *            the set change
	 */
	public SetChangeEvent(IObservableSet<E> source, SetDiff<E> diff) {
		super(source);
		this.diff = diff;
	}

	/**
	 * Returns the observable set from which this event originated.
	 *
	 * @return the observable set from which this event originated
	 */
	@SuppressWarnings("unchecked")
	public IObservableSet<E> getObservableSet() {
		return (IObservableSet<E>) getSource();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void dispatch(IObservablesListener listener) {
		((ISetChangeListener<E>) listener).handleSetChange(this);
	}

	@Override
	protected Object getListenerType() {
		return TYPE;
	}

}
