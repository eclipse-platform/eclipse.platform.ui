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
 *     Matthew Hall - bug 195222, 263413
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.jface.databinding.viewers.ViewerValueProperty;
import org.eclipse.jface.viewers.Viewer;

/**
 * @param <S> type of the source object
 * @param <T> type of the value of the property
 *
 * @since 3.3
 *
 */
public class ViewerInputProperty<S, T> extends ViewerValueProperty<S, T> {
	private final Object valueType;

	/**
	 * @param valueType The value type of the property
	 */
	public ViewerInputProperty(Object valueType) {
		this.valueType = valueType;
	}

	/**
	 * Creates a property with a null value type.
	 */
	public ViewerInputProperty() {
		this(null);
	}

	@Override
	public Object getValueType() {
		return valueType;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected T doGetValue(S source) {
		return (T) ((Viewer) source).getInput();
	}

	@Override
	protected void doSetValue(S source, T value) {
		((Viewer) source).setInput(value);
	}

	@Override
	public INativePropertyListener<S> adaptListener(ISimplePropertyListener<S, ValueDiff<? extends T>> listener) {
		return null;
	}

	protected void doAddListener(Viewer source, INativePropertyListener<Viewer> listener) {
	}

	protected void doRemoveListener(Viewer source, INativePropertyListener<Viewer> listener) {
	}

	@Override
	public String toString() {
		return "Viewer.input"; //$NON-NLS-1$
	}
}
