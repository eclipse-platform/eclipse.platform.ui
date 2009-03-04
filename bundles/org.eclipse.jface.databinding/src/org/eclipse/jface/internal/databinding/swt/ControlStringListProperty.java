/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * @since 3.3
 * 
 */
public abstract class ControlStringListProperty extends WidgetListProperty {
	public Object getElementType() {
		return String.class;
	}

	protected void doSetList(Object source, List list, ListDiff diff) {
		String[] strings = (String[]) list.toArray(new String[list.size()]);
		doSetStringList((Control) source, strings, diff);
	}

	abstract void doSetStringList(Control control, String[] list, ListDiff diff);

	protected List doGetList(Object source) {
		String[] list = doGetStringList((Control) source);
		return Arrays.asList(list);
	}

	abstract String[] doGetStringList(Control control);

	public INativePropertyListener adaptListener(
			ISimplePropertyListener listener) {
		return null;
	}
}
