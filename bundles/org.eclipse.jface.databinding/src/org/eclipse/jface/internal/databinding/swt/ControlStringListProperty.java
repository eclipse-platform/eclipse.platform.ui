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
 *     Matthew Hall - bugs 195222, 251611, 263413, 265561
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.jface.databinding.swt.WidgetListProperty;
import org.eclipse.swt.widgets.Control;

/**
 * @param <S> type of the source object
 *
 * @since 3.3
 */
public abstract class ControlStringListProperty<S extends Control> extends WidgetListProperty<S, String> {
	@Override
	public Object getElementType() {
		return String.class;
	}

	@Override
	protected void doSetList(S source, List<String> list, ListDiff<String> diff) {
		doUpdateList(source, diff);
	}

	@Override
	protected void doUpdateList(S source, ListDiff<String> diff) {
		doUpdateStringList(source, diff);
	}

	abstract void doUpdateStringList(S control, ListDiff<String> diff);

	@Override
	protected List<String> doGetList(S source) {
		String[] list = doGetStringList(source);
		return Arrays.asList(list);
	}

	abstract String[] doGetStringList(S control);

	@Override
	public INativePropertyListener<S> adaptListener(ISimplePropertyListener<S, ListDiff<String>> listener) {
		return null;
	}
}
