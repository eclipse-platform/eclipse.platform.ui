/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bug 262287
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 ******************************************************************************/

package org.eclipse.core.databinding.property;

import java.util.EventObject;
import java.util.Objects;

import org.eclipse.core.databinding.observable.IDiff;

/**
 * Event object events in the properties API
 *
 * @param <D>
 *            type of the diff handled by this event
 * @param <S>
 *            type of the source object handled by this event
 * @since 1.2
 */
public final class SimplePropertyEvent<S, D extends IDiff> extends EventObject {
	private static final long serialVersionUID = 1L;

	/**
	 * Event type constant indicating that the property changed
	 */
	public static final int CHANGE = notInlined(1);

	/**
	 * Event type constant indicating that the property became stale
	 */
	public static final int STALE = notInlined(2);

	private static int notInlined(int i) {
		return i;
	}

	/**
	 * The type of property event that occured
	 */
	public final int type;

	/**
	 * The property on which the event took place
	 */
	public final IProperty property;

	/**
	 * If event == CHANGE, a diff object describing the change in state, or null
	 * for an unknown change.
	 */
	public final D diff;

	/**
	 * Constructs a PropertyChangeEvent with the given attributes
	 *
	 * @param type
	 *            the property type
	 * @param source
	 *            the property source
	 * @param property
	 *            the property that changed on the source
	 * @param diff
	 *            a diff describing the change in state, or null if the change
	 *            is unknown or not applicable.
	 */
	public SimplePropertyEvent(int type, S source, IProperty property, D diff) {
		super(source);
		this.type = type;
		this.property = property;
		this.diff = diff;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		SimplePropertyEvent<?, ?> that = (SimplePropertyEvent<?, ?>) obj;
		return Objects.equals(getSource(), that.getSource()) && Objects.equals(this.property, that.property)
				&& Objects.equals(this.diff, that.diff);
	}

	@Override
	public int hashCode() {
		int hash = 17;
		hash = hash * 37 + getSource().hashCode();
		hash = hash * 37 + property.hashCode();
		hash = hash * 37 + Objects.hashCode(diff);
		return hash;
	}
}
